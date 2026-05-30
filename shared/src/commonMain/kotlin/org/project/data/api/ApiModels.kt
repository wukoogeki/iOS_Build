package org.project.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.project.data.CabinetDevice
import org.project.data.DeviceState
import org.project.data.DeviceStatus
import org.project.data.EnvironmentData
import org.project.data.WorkMode

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val token: String? = null,
    val message: String? = null,
    val username: String? = null
)

@Serializable
data class LogoutResponse(
    val success: Boolean,
    val message: String? = null
)

@Serializable
data class DeviceListResponse(
    val success: Boolean,
    val devices: List<ApiCabinetDevice> = emptyList(),
    val message: String? = null
)

@Serializable
data class DeviceDetailResponse(
    val success: Boolean,
    val device: ApiCabinetDevice? = null,
    val message: String? = null
)

@Serializable
data class DeviceDataResponse(
    val success: Boolean,
    val data: ApiEnvironmentData? = null,
    val mode: String? = null,
    val message: String? = null
)

@Serializable
data class DeviceHistoryResponse(
    val success: Boolean,
    @SerialName("device_id") val deviceId: String? = null,
    val history: List<ApiEnvironmentData> = emptyList(),
    val message: String? = null
)

@Serializable
data class DeviceControlRequest(
    val fan: String? = null,
    val heater: String? = null,
    val dehumidifier: String? = null
)

@Serializable
data class DeviceControlResponse(
    val success: Boolean,
    @SerialName("device_state") val deviceState: ApiDeviceState? = null,
    val message: String? = null
)

@Serializable
data class SystemStatusResponse(
    val success: Boolean,
    @SerialName("current_mode") val currentMode: String? = null,
    @SerialName("device_state") val deviceState: ApiDeviceState? = null,
    @SerialName("current_data") val currentData: ApiEnvironmentData? = null,
    val message: String? = null
)

@Serializable
data class ApiError(
    val success: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val code: Int? = null
)

@Serializable
data class ApiEnvironmentData(
    val temperature: Float = 0f,
    val humidity: Float = 0f,
    @SerialName("dew_point") val dewPoint: Float = 0f,
    val timestamp: Long = 0L
) {
    fun toModel(): EnvironmentData = EnvironmentData(
        temperature = temperature,
        humidity = humidity,
        dewPoint = dewPoint,
        timestamp = timestamp
    )
}

@Serializable
data class ApiDeviceState(
    val fan: String = "OFF",
    val heater: String = "OFF",
    val dehumidifier: String = "OFF"
) {
    fun toModel(): DeviceState = DeviceState(
        fan = fan.toDeviceStatus(),
        heater = heater.toDeviceStatus(),
        dehumidifier = dehumidifier.toDeviceStatus()
    )
}

@Serializable
data class ApiCabinetDevice(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val x: Float = 0f,
    val y: Float = 0f,
    @SerialName("is_online") val isOnline: Boolean = true,
    @SerialName("current_data") val currentData: ApiEnvironmentData? = null,
    @SerialName("current_mode") val currentMode: String = "NORMAL",
    @SerialName("device_state") val deviceState: ApiDeviceState? = null
) {
    fun toModel(): CabinetDevice = CabinetDevice(
        id = id,
        name = name,
        location = location,
        x = x,
        y = y,
        isOnline = isOnline,
        currentData = currentData?.toModel() ?: EnvironmentData(25f, 60f, 18f, 0L),
        currentMode = currentMode.toWorkMode(),
        deviceState = deviceState?.toModel() ?: DeviceState()
    )
}

private fun String.toDeviceStatus(): DeviceStatus = when (this) {
    "ON" -> DeviceStatus.ON
    "AUTO" -> DeviceStatus.AUTO
    else -> DeviceStatus.OFF
}

private fun String.toWorkMode(): WorkMode = when (this) {
    "DEHUMIDIFY" -> WorkMode.DEHUMIDIFY
    "HEAT" -> WorkMode.HEAT
    "VENTILATE" -> WorkMode.VENTILATE
    "ALARM" -> WorkMode.ALARM
    else -> WorkMode.NORMAL
}
