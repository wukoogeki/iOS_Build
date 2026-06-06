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

    suspend fun getDeviceLatest(deviceId: String): Result<CabinetDevice> = runCatching {
        val response: ApiDeviceLatest = client.get(
            "$baseUrl${ApiRoutes.DEVICE_LATEST.replace("{id}", deviceId)}"
        ).body()
        response.toModel()
    }

    suspend fun getDeviceHistory(deviceId: String, hours: Int = 24): Result<List<EnvironmentData>> = runCatching {
        val response: DeviceHistoryResponse = client.get(
            "$baseUrl${ApiRoutes.DEVICE_HISTORY.replace("{id}", deviceId)}?hours=$hours"
        ).body()
        response.history.map { it.toModel() }
    }

    suspend fun sendCommand(
        deviceId: String,
        command: String,
        issuedBy: String = "app"
    ): Result<Unit> = runCatching {
        val response: CommandResponse = client.post(
            "$baseUrl${ApiRoutes.DEVICE_COMMAND.replace("{id}", deviceId)}"
        ) {
            setBody(CommandRequest(command, issuedBy))
        }.body()
        if (!response.success) {
            throw Exception(response.message ?: "Command failed")
        }
    }

    suspend fun getWeather(): Result<WeatherInfo> = runCatching {
        val response: WeatherResponse = client.get("$baseUrl${ApiRoutes.WEATHER}").body()
        response.toModel()
    }

    fun close() {
        client.close()
    }

    companion object {
        val instance: ApiService by lazy { ApiService() }
    }
}
