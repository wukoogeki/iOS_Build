package org.project.data.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.project.data.*

class ApiService private constructor() {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = LogLevel.INFO
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 15000
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
            header("ngrok-skip-browser-warning", "true")
            ApiConfig.getAuthToken()?.let { token ->
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }

    private val baseUrl: String
        get() = ApiConfig.getBaseUrl()

    suspend fun login(username: String, password: String): Result<String> = runCatching {
        val response: LoginResponse = client.post("$baseUrl${ApiRoutes.LOGIN}") {
            setBody(LoginRequest(username, password))
        }.body()
        if (response.success && response.token != null) {
            ApiConfig.setAuthToken(response.token)
            response.token
        } else {
            throw Exception(response.message ?: "Login failed")
        }
    }

    suspend fun logout(): Result<Unit> = runCatching {
        val response: LogoutResponse = client.post("$baseUrl${ApiRoutes.LOGOUT}") {
        }.body()
        ApiConfig.setAuthToken(null)
        if (!response.success) {
            throw Exception(response.message ?: "Logout failed")
        }
    }

    suspend fun getDevices(): Result<List<CabinetDevice>> = runCatching {
        val response: DeviceListResponse = client.get("$baseUrl${ApiRoutes.DEVICES}").body()
        if (response.success) {
            response.devices.map { it.toModel() }
        } else {
            throw Exception(response.message ?: "Failed to get devices")
        }
    }

    suspend fun getDeviceById(deviceId: String): Result<CabinetDevice> = runCatching {
        val response: DeviceDetailResponse = client.get(
            "$baseUrl${ApiRoutes.DEVICE_BY_ID.replace("{id}", deviceId)}"
        ).body()
        if (response.success && response.device != null) {
            response.device.toModel()
        } else {
            throw Exception(response.message ?: "Failed to get device")
        }
    }

    suspend fun getDeviceData(deviceId: String): Result<EnvironmentData> = runCatching {
        val response: DeviceDataResponse = client.get(
            "$baseUrl${ApiRoutes.DEVICE_DATA.replace("{id}", deviceId)}"
        ).body()
        if (response.success && response.data != null) {
            response.data.toModel()
        } else {
            throw Exception(response.message ?: "Failed to get device data")
        }
    }

    suspend fun getDeviceHistory(deviceId: String): Result<List<EnvironmentData>> = runCatching {
        val response: DeviceHistoryResponse = client.get(
            "$baseUrl${ApiRoutes.DEVICE_HISTORY.replace("{id}", deviceId)}"
        ).body()
        if (response.success) {
            response.history.map { it.toModel() }
        } else {
            throw Exception(response.message ?: "Failed to get history")
        }
    }

    suspend fun controlDevice(
        deviceId: String,
        fan: DeviceStatus? = null,
        heater: DeviceStatus? = null,
        dehumidifier: DeviceStatus? = null
    ): Result<DeviceState> = runCatching {
        val request = DeviceControlRequest(
            fan = fan?.name,
            heater = heater?.name,
            dehumidifier = dehumidifier?.name
        )
        val response: DeviceControlResponse = client.post(
            "$baseUrl${ApiRoutes.DEVICE_CONTROL.replace("{id}", deviceId)}"
        ) {
            setBody(request)
        }.body()
        if (response.success && response.deviceState != null) {
            response.deviceState.toModel()
        } else {
            throw Exception(response.message ?: "Control failed")
        }
    }

    suspend fun getSystemStatus(): Result<SystemStatusResponse> = runCatching {
        client.get("$baseUrl${ApiRoutes.SYSTEM_STATUS}").body()
    }

    fun close() {
        client.close()
    }

    companion object {
        val instance: ApiService by lazy { ApiService() }
    }
}
