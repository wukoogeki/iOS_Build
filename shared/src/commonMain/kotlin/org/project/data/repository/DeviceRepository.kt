package org.project.data.repository

import org.project.data.CabinetDevice
import org.project.data.DeviceStatus
import org.project.data.EnvironmentData
import org.project.data.WeatherInfo
import org.project.data.api.ApiService

class DeviceRepository private constructor() {
    private val apiService = ApiService.instance

    suspend fun login(username: String, password: String): Result<String> {
        return apiService.login(username, password)
    }

    suspend fun logout(): Result<Unit> {
        return apiService.logout()
    }

    suspend fun getDevices(): Result<List<CabinetDevice>> {
        return apiService.getDevices()
    }

    suspend fun getDeviceLatest(deviceId: String): Result<CabinetDevice> {
        return apiService.getDeviceLatest(deviceId)
    }

    suspend fun getDeviceHistory(deviceId: String, hours: Int = 24): Result<List<EnvironmentData>> {
        return apiService.getDeviceHistory(deviceId, hours)
    }

    suspend fun controlHeater(deviceId: String, status: DeviceStatus): Result<Unit> {
        val command = when (status) {
            DeviceStatus.ON -> "HEATER_ON"
            DeviceStatus.OFF -> "HEATER_OFF"
            DeviceStatus.AUTO -> "HEATER_AUTO"
        }
        return apiService.sendCommand(deviceId, command)
    }

    suspend fun controlFan(deviceId: String, status: DeviceStatus): Result<Unit> {
        val command = when (status) {
            DeviceStatus.ON -> "FAN_ON"
            DeviceStatus.OFF -> "FAN_OFF"
            DeviceStatus.AUTO -> "FAN_AUTO"
        }
        return apiService.sendCommand(deviceId, command)
    }

    suspend fun controlAtomizer(deviceId: String, status: DeviceStatus): Result<Unit> {
        val command = when (status) {
            DeviceStatus.ON -> "ATOMIZER_ON"
            DeviceStatus.OFF -> "ATOMIZER_OFF"
            DeviceStatus.AUTO -> "ATOMIZER_AUTO"
        }
        return apiService.sendCommand(deviceId, command)
    }

    suspend fun controlCooling(deviceId: String, status: DeviceStatus): Result<Unit> {
        val command = when (status) {
            DeviceStatus.ON -> "COOLING_ON"
            DeviceStatus.OFF -> "COOLING_OFF"
            DeviceStatus.AUTO -> "COOLING_AUTO"
        }
        return apiService.sendCommand(deviceId, command)
    }

    suspend fun controlBuzzer(deviceId: String, status: DeviceStatus): Result<Unit> {
        val command = when (status) {
            DeviceStatus.ON -> "BUZZER_ON"
            DeviceStatus.OFF -> "BUZZER_OFF"
            DeviceStatus.AUTO -> "BUZZER_AUTO"
        }
        return apiService.sendCommand(deviceId, command)
    }

    suspend fun getWeather(): Result<WeatherInfo> {
        return apiService.getWeather()
    }

    companion object {
        val instance: DeviceRepository by lazy { DeviceRepository() }
    }
}
