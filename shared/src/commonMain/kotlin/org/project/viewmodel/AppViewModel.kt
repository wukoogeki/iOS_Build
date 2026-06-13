package org.project.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import org.project.data.*
import org.project.data.api.ApiConfig
import org.project.data.repository.DeviceRepository
import org.project.data.WorkMode
import org.project.data.DeviceStatus

class AppViewModel {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val repository = DeviceRepository.instance

    var state by mutableStateOf(SystemState())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isTokenExpired by mutableStateOf(false)
        private set

    // 异常告警弹窗：进入时为 null，正常时不弹；存在严重告警时填入严重设备数
    var abnormalAlertCount by mutableStateOf(0)
        private set
    var abnormalAlertVisible by mutableStateOf(false)
        private set
    // 超过 10 秒未处理时为 true，App 端用于切换淡红色背景/声音/震动
    var abnormalAlertUrgent by mutableStateOf(false)
        private set

    private var abnormalAlertJob: Job? = null
    private var previousCriticalCount: Int = 0

    private fun Throwable.toUserMessage(): String {
        val msg = message ?: "未知错误"
        // Ktor 在收到非 2xx 响应时抛 ResponseException，
        // 其 message 通常包含 "401"/"Unauthorized" 等；这里只用于友好提示。
        return when {
            msg.contains("Connection refused", ignoreCase = true) -> "服务器拒绝连接"
            msg.contains("connect timed out", ignoreCase = true) ||
                msg.contains("timeout", ignoreCase = true) ||
                msg.contains("timed out", ignoreCase = true) -> "连接超时，请检查网络"
            msg.contains("Unable to resolve host", ignoreCase = true) ||
                msg.contains("Name or service not known", ignoreCase = true) ||
                msg.contains("nodename nor servname", ignoreCase = true) -> "网络不可达，请检查网络连接"
            msg.contains("403", ignoreCase = true) -> "没有权限访问"
            msg.contains("404", ignoreCase = true) -> "请求的资源不存在"
            msg.contains("500", ignoreCase = true) -> "服务器内部错误"
            msg.contains("502", ignoreCase = true) -> "网关错误"
            msg.contains("503", ignoreCase = true) -> "服务暂不可用"
            msg.contains("SSLHandshakeException", ignoreCase = true) ||
                msg.contains("SSL", ignoreCase = true) -> "安全连接失败"
            msg.contains("Fail to fetch", ignoreCase = true) -> "网络请求失败，请检查网络连接"
            else -> msg
        }
    }

    private fun Throwable.isTokenExpiredError(): Boolean {
        // 优先通过 Ktor 异常类型 / HTTP 状态码判断 token 失效；
        // 字符串匹配只作为兜底（处理错误响应体里直接写明 Token 无效的情况）。
        if (this is io.ktor.client.plugins.ClientRequestException) {
            val status = response.status.value
            if (status == 401 || status == 403) {
                // 401/403 在已登录态下 = token 失效；在登录接口时也会命中，
                // 但登录接口走 LoginResponse.message 自行抛出，不会落到此 catch。
                return true
            }
        }
        val msg = message ?: return false
        return msg.contains("Token无效或已过期", ignoreCase = true) ||
                msg.contains("Token 已过期", ignoreCase = true) ||
                msg.contains("invalid token", ignoreCase = true) ||
                msg.contains("token expired", ignoreCase = true)
    }

    init {
        if (ApiConfig.isLoggedIn()) {
            val savedUsername = ApiConfig.getUsername() ?: "用户"
            state = state.copy(isLoggedIn = true, currentUser = savedUsername)
        }
    }

    fun clearError() {
        errorMessage = null
    }

    fun onTokenExpired() {
        ApiConfig.clearAuth()
        isTokenExpired = true
    }

    private fun handleError(e: Throwable) {
        if (e.isTokenExpiredError()) {
            onTokenExpired()
        } else {
            errorMessage = e.toUserMessage()
        }
    }

    fun configureApi(baseUrl: String) {
        ApiConfig.configure(baseUrl)
    }

    fun onLoginSuccess(username: String) {
        ApiConfig.saveUsername(username)
        isTokenExpired = false
        state = state.copy(isLoggedIn = true, currentUser = username)
        loadDevices()
    }

    fun selectDevice(device: CabinetDevice?) {
        state = state.copy(selectedDevice = device)
        device?.let {
            state = state.copy(
                currentData = it.currentData,
                currentMode = it.currentMode,
                deviceState = it.deviceState
            )
            loadDeviceData(it.id)
            loadDeviceHistory(it.id)
        }
    }

    fun loadDevices() {
        scope.launch {
            isLoading = true
            errorMessage = null
            repository.getDevices()
                .onSuccess { devices ->
                    // 保留用户已选设备：仅在没选过 / 选过的设备已下线/被删时才回退到第一个
                    val currentId = state.selectedDevice?.id
                    val stillExists = currentId != null && devices.any { it.id == currentId }
                    val target = if (stillExists) {
                        state.selectedDevice
                    } else {
                        devices.firstOrNull()
                    }
                    state = state.copy(
                        devices = devices,
                        selectedDevice = target
                    )
                    target?.let { device -> selectDevice(device) }
                    // 检测严重告警变化
                    checkAbnormalAlert(devices)
                }
                .onFailure {
                    handleError(it)
                }
            isLoading = false
        }
    }

    /**
     * 检测严重告警数量变化：
     * - 新增严重告警时弹出弹窗
     * - 10 秒内未点击，则标记为紧急（用于背景变红/声音/震动）
     * - 用户点击或严重告警减少时清空
     */
    private fun checkAbnormalAlert(devices: List<CabinetDevice>) {
        val critical = devices.count {
            it.isOnline && it.alarm.severity == AlarmSeverity.CRITICAL
        }

        if (critical > previousCriticalCount && critical > 0) {
            // 出现新的严重告警
            abnormalAlertCount = critical
            abnormalAlertVisible = true
            abnormalAlertUrgent = false
            abnormalAlertJob?.cancel()
            abnormalAlertJob = scope.launch {
                delay(10_000)
                if (abnormalAlertVisible) {
                    abnormalAlertUrgent = true
                }
            }
        } else if (critical == 0 && abnormalAlertVisible) {
            // 严重告警已处理
            dismissAbnormalAlert()
        }
        previousCriticalCount = critical
    }

    fun dismissAbnormalAlert() {
        abnormalAlertVisible = false
        abnormalAlertUrgent = false
        abnormalAlertCount = 0
        abnormalAlertJob?.cancel()
        abnormalAlertJob = null
    }

    private fun loadDeviceData(deviceId: String) {
        scope.launch {
            repository.getDeviceLatest(deviceId)
                .onSuccess { device ->
                    // 离线设备不根据 stale 0/0 温度推导工作模式
                    val mode = if (!device.isOnline) WorkMode.OFFLINE
                               else determineWorkMode(device.currentData)
                    state = state.copy(
                        currentData = device.currentData,
                        currentMode = mode,
                        deviceState = device.deviceState
                    )
                }
                .onFailure {
                    handleError(it)
                }
        }
    }

    private fun loadDeviceHistory(deviceId: String) {
        scope.launch {
            repository.getDeviceHistory(deviceId)
                .onSuccess { history ->
                    state = state.copy(historyData = history)
                }
                .onFailure {
                    handleError(it)
                }
        }
    }

    fun login(username: String, password: String, onSuccess: () -> Unit = {}) {
        scope.launch {
            isLoading = true
            errorMessage = null
            repository.login(username, password)
                .onSuccess {
                    onLoginSuccess(username)
                    onSuccess()
                }
                .onFailure {
                    handleError(it)
                }
            isLoading = false
        }
    }

    fun logout() {
        state = state.copy(
            isLoggedIn = false,
            currentUser = "",
            devices = emptyList(),
            selectedDevice = null,
            historyData = emptyList()
        )
        ApiConfig.reset()
        scope.launch {
            repository.logout()
        }
    }

    fun setDeviceStatus(device: String, status: DeviceStatus) {
        val deviceId = state.selectedDevice?.id ?: return

        scope.launch {
            val result = when (device) {
                "heater" -> repository.controlHeater(deviceId, status)
                "fan" -> repository.controlFan(deviceId, status)
                "atomizer" -> repository.controlAtomizer(deviceId, status)
                "cooling" -> repository.controlCooling(deviceId, status)
                "buzzer" -> repository.controlBuzzer(deviceId, status)
                else -> return@launch
            }

            result.onSuccess {
                val currentHeater = if (device == "heater") status else state.deviceState.heater
                val currentFan = if (device == "fan") status else state.deviceState.fan
                val currentAtomizer = if (device == "atomizer") status else state.deviceState.atomizer
                val currentCooling = if (device == "cooling") status else state.deviceState.cooling
                val currentBuzzer = if (device == "buzzer") status else state.deviceState.buzzer
                state = state.copy(
                    deviceState = DeviceState(
                        heater = currentHeater,
                        fan = currentFan,
                        atomizer = currentAtomizer,
                        cooling = currentCooling,
                        buzzer = currentBuzzer
                    )
                )
            }.onFailure {
                handleError(it)
            }
        }
    }

    fun refreshData() {
        state.selectedDevice?.let { device ->
            loadDeviceData(device.id)
            loadDeviceHistory(device.id)
        }
    }

    /**
     * 用户下拉松手触发的刷新：
     * - 设备列表 + 当前设备的最新数据 + 历史数据 一起拉取
     * - 设置 isRefreshing 让 PullToRefresh 指示器有结束时机
     */
    fun refreshAll() {
        if (isRefreshing) return
        scope.launch {
            isRefreshing = true
            // 设备列表（内部会触发 selectDevice -> loadDeviceData + loadDeviceHistory）
            repository.getDevices()
                .onSuccess { devices ->
                    val currentId = state.selectedDevice?.id
                    val stillExists = currentId != null && devices.any { it.id == currentId }
                    val target = if (stillExists) {
                        state.selectedDevice
                    } else {
                        devices.firstOrNull()
                    }
                    state = state.copy(
                        devices = devices,
                        selectedDevice = target
                    )
                    // 主动拉一次当前设备的最新数据 + 历史（保证下拉后看到新数据）
                    target?.let { device ->
                        launch { loadDeviceData(device.id) }
                        launch { loadDeviceHistory(device.id) }
                    }
                    checkAbnormalAlert(devices)
                }
                .onFailure {
                    handleError(it)
                }
            // 给予下拉指示器最短可见时间，避免一闪而过
            kotlinx.coroutines.delay(600)
            isRefreshing = false
        }
    }

    private fun determineWorkMode(data: EnvironmentData): WorkMode {
        return when {
            data.alarmCode != 0 -> WorkMode.ALARM
            data.humidity > 85f -> WorkMode.DEHUMIDIFY
            data.humidity > 75f -> WorkMode.DEHUMIDIFY
            data.temperature < 5f -> WorkMode.HEAT
            data.temperature > 35f -> WorkMode.VENTILATE
            else -> WorkMode.NORMAL
        }
    }

    fun dispose() {
        scope.cancel()
    }
}
