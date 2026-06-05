package org.project.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.project.data.AlarmInfo
import org.project.data.CabinetDevice
import org.project.data.DeviceState
import org.project.data.DeviceStatus
import org.project.data.EnvironmentData
import org.project.data.WeatherInfo
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
data class ApiCabinetDevice(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val x: Float = 0f,
    val y: Float = 0f,
    @SerialName("isOnline") val isOnline: Boolean = true,
    @SerialName("currentData") val currentData: ApiEnvironmentData? = null,
    @SerialName("workStatus") val workStatus: Int = 1,
    @SerialName("deviceState") val deviceState: ApiDeviceState? = null,
    @SerialName("wifiStatus") val wifiStatus: Int = 1,
    @SerialName("alarm") val alarm: ApiAlarm? = null
) {
    fun toModel(): CabinetDevice = CabinetDevice(
        id = id,
        name = name,
        location = location,
        x = x,
        y = y,
        isOnline = isOnline,
        currentData = currentData?.toModel() ?: EnvironmentData(25f, 60f, 18f, 0L),
        currentMode = if (alarm != null && alarm.code != 0) WorkMode.ALARM else WorkMode.NORMAL,
        deviceState = deviceState?.toModel() ?: DeviceState(),
        wifiStatus = wifiStatus,
        workStatus = workStatus,
        alarm = alarm?.toModel() ?: AlarmInfo()
    )
}

@Serializable
data class ApiAlarm(
    val code: Int = 0,
    val message: String = "normal"
) {
    fun toModel(): AlarmInfo = AlarmInfo(
        code = code,
        message = message
    )
}

@Serializable
data class ApiDeviceState(
    @SerialName("heatStatus") val heatStatus: Int = 0,
    @SerialName("dehumidifyStatus") val dehumidifyStatus: Int = 0,
    @SerialName("buzzerStatus") val buzzerStatus: Int = 0
) {
    fun toModel(): DeviceState = DeviceState(
        fan = if (dehumidifyStatus == 1) DeviceStatus.ON else DeviceStatus.OFF,
        heater = if (heatStatus == 1) DeviceStatus.ON else DeviceStatus.OFF,
        dehumidifier = if (dehumidifyStatus == 1) DeviceStatus.ON else DeviceStatus.OFF
    )
}

@Serializable
data class ApiEnvironmentData(
    @SerialName("light_lx") val lightLx: Int = 0,
    @SerialName("temperature_c") val temperatureC: Float = 0f,
    @SerialName("humidity_percent") val humidityPercent: Float = 0f,
    @SerialName("work_status") val workStatus: Int = 1,
    @SerialName("heat_status") val heatStatus: Int = 0,
    @SerialName("dehumidify_status") val dehumidifyStatus: Int = 0,
    @SerialName("buzzer_status") val buzzerStatus: Int = 0,
    @SerialName("wifi_status") val wifiStatus: Int = 1,
    @SerialName("alarm_code") val alarmCode: Int = 0,
    @SerialName("alarm_message") val alarmMessage: String = "normal",
    val timestamp: String = "",
    val weather: ApiWeather? = null
) {
    fun toModel(): EnvironmentData {
        val dewPoint = computeDewPoint(temperatureC, humidityPercent)
        val ts = parseTimestamp(timestamp)
        return EnvironmentData(
            temperature = temperatureC,
            humidity = humidityPercent,
            dewPoint = dewPoint,
            timestamp = ts,
            lightLx = lightLx,
            alarmCode = alarmCode,
            alarmMessage = alarmMessage
        )
    }
}

@Serializable
data class ApiWeather(
    @SerialName("weatherType") val weatherType: String = "晴天",
    @SerialName("startTime") val startTime: String = ""
) {
    fun toModel(): WeatherInfo = WeatherInfo(
        weatherType = weatherType,
        startTime = startTime
    )
}

@Serializable
data class DeviceHistoryResponse(
    @SerialName("device_id") val deviceId: String? = null,
    val history: List<ApiEnvironmentData> = emptyList(),
    val message: String? = null
)

@Serializable
data class WeatherResponse(
    @SerialName("weatherType") val weatherType: String = "晴天",
    @SerialName("startTime") val startTime: String = ""
) {
    fun toModel(): WeatherInfo = WeatherInfo(
        weatherType = weatherType,
        startTime = startTime
    )
}

@Serializable
data class CommandRequest(
    val command: String,
    val issuedBy: String = "app"
)

@Serializable
data class CommandResponse(
    val success: Boolean,
    val message: String? = null
)

@Serializable
data class ApiError(
    val success: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val code: Int? = null
)

private fun computeDewPoint(tempC: Float, humidity: Float): Float {
    if (humidity <= 0f) return tempC
    val a = 17.27f
    val b = 237.7f
    val rh = humidity / 100f
    val gamma = (a * tempC) / (b + tempC) + kotlin.math.ln(rh.toDouble()).toFloat()
    return (b * gamma) / (a - gamma)
}

private fun parseTimestamp(ts: String): Long {
    if (ts.isBlank()) return 0L
    return try {
        val instant = kotlinx.datetime.Instant.parse(ts)
        instant.toEpochMilliseconds()
    } catch (e: Exception) {
        0L
    }
}
