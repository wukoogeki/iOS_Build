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

    fun clearError() {
        errorMessage = null
    }

    fun configureApi(baseUrl: String) {
        ApiConfig.configure(baseUrl)
    }

    fun onLoginSuccess(username: String) {
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
                .onFailure { e ->
                    errorMessage = e.message ?: "加载设备列表失败"
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
                .onFailure { e ->
                    errorMessage = e.message
                }
        }
    }

    private fun loadDeviceHistory(deviceId: String) {
        scope.launch {
            repository.getDeviceHistory(deviceId)
                .onSuccess { history ->
                    state = state.copy(historyData = history)
                }
                .onFailure { e ->
                    errorMessage = e.message
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
                .onFailure { e ->
                    errorMessage = e.message ?: "登录失败"
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
                .onFailure { e ->
                    errorMessage = e.message
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
