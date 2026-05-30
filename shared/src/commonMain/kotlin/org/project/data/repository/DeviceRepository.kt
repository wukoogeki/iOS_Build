package org.project.data.repository

import org.project.data.api.ApiService
import org.project.data.CabinetDevice
import org.project.data.DeviceState
import org.project.data.DeviceStatus
import org.project.data.EnvironmentData
import org.project.data.WorkMode

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

    suspend fun getDeviceById(deviceId: String): Result<CabinetDevice> {
        return apiService.getDeviceById(deviceId)
    }

    suspend fun getDeviceData(deviceId: String): Result<EnvironmentData> {
        return apiService.getDeviceData(deviceId)
    }

    suspend fun getDeviceHistory(deviceId: String): Result<List<EnvironmentData>> {
        return apiService.getDeviceHistory(deviceId)
    }

    suspend fun controlDevice(
        deviceId: String,
        fan: DeviceStatus? = null,
        heater: DeviceStatus? = null,
        dehumidifier: DeviceStatus? = null
    ): Result<DeviceState> {
        return apiService.controlDevice(deviceId, fan, heater, dehumidifier)
    }

    companion object {
        val instance: DeviceRepository by lazy { DeviceRepository() }
    }
}