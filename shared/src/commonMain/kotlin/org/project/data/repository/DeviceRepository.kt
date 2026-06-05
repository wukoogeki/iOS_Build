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

    suspend fun getDeviceLatest(deviceId: String): Result<EnvironmentData> {
        return apiService.getDeviceLatest(deviceId)
    }

    suspend fun getDeviceHistory(deviceId: String, hours: Int = 24): Result<List<EnvironmentData>> {
        return apiService.getDeviceHistory(deviceId, hours)
    }

    suspend fun controlFan(deviceId: String, status: DeviceStatus): Result<Unit> {
        val command = when (status) {
            DeviceStatus.ON -> "FAN_ON"
            DeviceStatus.OFF -> "FAN_OFF"
            DeviceStatus.AUTO -> "FAN_AUTO"
        }
        return apiService.sendCommand(deviceId, command)
    }

    suspend fun controlHeater(deviceId: String, status: DeviceStatus): Result<Unit> {
        val command = when (status) {
            DeviceStatus.ON -> "HEATER_ON"
            DeviceStatus.OFF -> "HEATER_OFF"
            DeviceStatus.AUTO -> "HEATER_AUTO"
        }
        return apiService.sendCommand(deviceId, command)
    }

    suspend fun controlDehumidifier(deviceId: String, status: DeviceStatus): Result<Unit> {
        val command = when (status) {
            DeviceStatus.ON -> "DEHUMIDIFIER_ON"
            DeviceStatus.OFF -> "DEHUMIDIFIER_OFF"
            DeviceStatus.AUTO -> "DEHUMIDIFIER_AUTO"
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
