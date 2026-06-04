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

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private fun Throwable.toUserMessage(): String {
        val msg = message ?: "未知错误"
        return when {
            msg.contains("Connection refused", ignoreCase = true) -> "服务器拒绝连接"
            msg.contains("connect timed out", ignoreCase = true) ||
                msg.contains("timeout", ignoreCase = true) ||
                msg.contains("timed out", ignoreCase = true) -> "连接超时，请检查网络"
            msg.contains("Unable to resolve host", ignoreCase = true) ||
                msg.contains("Name or service not known", ignoreCase = true) ||
                msg.contains("nodename nor servname", ignoreCase = true) -> "网络不可达，请检查网络连接"
            msg.contains("401", ignoreCase = true) -> "用户名或密码错误"
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

    init {
        if (ApiConfig.isLoggedIn()) {
            val savedUsername = ApiConfig.getUsername() ?: "用户"
            state = state.copy(isLoggedIn = true, currentUser = savedUsername)
        }
    }

    fun clearError() {
        errorMessage = null
    }

    fun configureApi(baseUrl: String) {
        ApiConfig.configure(baseUrl)
    }

    fun onLoginSuccess(username: String) {
        ApiConfig.saveUsername(username)
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
                    state = state.copy(
                        devices = devices,
                        selectedDevice = devices.firstOrNull()
                    )
                    devices.firstOrNull()?.let { device ->
                        selectDevice(device)
                    }
                }
                .onFailure {
                    errorMessage = it.toUserMessage()
                }
            isLoading = false
        }
    }

    private fun loadDeviceData(deviceId: String) {
        scope.launch {
            repository.getDeviceData(deviceId)
                .onSuccess { data ->
                    state = state.copy(
                        currentData = data,
                        currentMode = determineWorkMode(data)
                    )
                }
                .onFailure {
                    errorMessage = it.toUserMessage()
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
                    errorMessage = it.toUserMessage()
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
                    errorMessage = it.toUserMessage()
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
            val fan = if (device == "fan") status else null
            val heater = if (device == "heater") status else null
            val dehumidifier = if (device == "dehumidifier") status else null

            repository.controlDevice(deviceId, fan, heater, dehumidifier)
                .onSuccess { newState ->
                    state = state.copy(deviceState = newState)
                }
                .onFailure {
                    errorMessage = it.toUserMessage()
                }
        }
    }

    fun refreshData() {
        state.selectedDevice?.let { device ->
            loadDeviceData(device.id)
            loadDeviceHistory(device.id)
        }
    }

    private fun determineWorkMode(data: EnvironmentData): WorkMode {
        return when {
            data.humidity > 85f -> WorkMode.ALARM
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
