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
import org.project.data.AlarmCodeTable

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
    @SerialName("deviceState") val deviceState: ApiDeviceState? = null,
    @SerialName("alarm") val alarm: ApiAlarm? = null
) {
    fun toModel(): CabinetDevice {
        val alarmInfo = alarm?.toModel() ?: AlarmInfo()
        // 是否离线只由后端 isOnline 字段判断，不再用 alarm bitmask 覆盖
        val effectiveIsOnline = isOnline
        val effectiveLocation = location.takeIf { it.isNotBlank() } ?: "未知地址"
        return CabinetDevice(
            id = id,
            name = name,
            location = effectiveLocation,
            x = x,
            y = y,
            isOnline = effectiveIsOnline,
            currentData = currentData?.toModel(alarmCode = alarmInfo.code, alarmMessage = alarmInfo.message)
                ?: EnvironmentData(25f, 60f, 18f, 0L, alarmCode = alarmInfo.code, alarmMessage = alarmInfo.message),
            currentMode = if (!effectiveIsOnline) WorkMode.OFFLINE
                          else if (alarmInfo.isAbnormal) WorkMode.ALARM
                          else WorkMode.NORMAL,
            deviceState = deviceState?.toModel() ?: DeviceState(),
            alarm = alarmInfo
        )
    }
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
    @SerialName("fanStatus") val fanStatus: Int = 0,
    @SerialName("atomizerStatus") val atomizerStatus: Int = 0,
    @SerialName("coolingStatus") val coolingStatus: Int = 0,
    @SerialName("buzzerStatus") val buzzerStatus: Int = 0
) {
    fun toModel(): DeviceState = DeviceState(
        heater = if (heatStatus == 1) DeviceStatus.ON else DeviceStatus.OFF,
        fan = if (fanStatus == 1) DeviceStatus.ON else DeviceStatus.OFF,
        atomizer = if (atomizerStatus == 1) DeviceStatus.ON else DeviceStatus.OFF,
        cooling = if (coolingStatus == 1) DeviceStatus.ON else DeviceStatus.OFF,
        buzzer = if (buzzerStatus == 1) DeviceStatus.ON else DeviceStatus.OFF
    )
}

@Serializable
data class ApiEnvironmentData(
    @SerialName("light_lx") val lightLx: Int = 0,
    @SerialName("temperature_c") val temperatureC: Float = 0f,
    @SerialName("humidity_percent") val humidityPercent: Float = 0f,
    val timestamp: String = "",
    val weather: ApiWeather? = null
) {
    fun toModel(alarmCode: Int = 0, alarmMessage: String = "normal"): EnvironmentData {
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
data class ApiDeviceLatest(
    val id: String = "",
    @SerialName("isOnline") val isOnline: Boolean = true,
    @SerialName("currentData") val currentData: ApiEnvironmentData? = null,
    @SerialName("deviceState") val deviceState: ApiDeviceState? = null,
    @SerialName("alarm") val alarm: ApiAlarm? = null,
    val weather: ApiWeather? = null
) {
    fun toModel(): CabinetDevice {
        val alarmInfo = alarm?.toModel() ?: AlarmInfo()
        val effectiveIsOnline = isOnline
        val envData = currentData?.toModel(alarmCode = alarmInfo.code, alarmMessage = alarmInfo.message)
            ?: EnvironmentData(25f, 60f, 18f, 0L, alarmCode = alarmInfo.code, alarmMessage = alarmInfo.message)
        val ds = deviceState?.toModel() ?: DeviceState()
        return CabinetDevice(
            id = id,
            name = id,
            location = "未知地址",
            isOnline = effectiveIsOnline,
            currentData = envData,
            currentMode = if (!effectiveIsOnline) WorkMode.OFFLINE
                          else if (alarmInfo.isAbnormal) WorkMode.ALARM
                          else WorkMode.NORMAL,
            deviceState = ds,
            alarm = alarmInfo
        )
    }
}

@Serializable
data class ApiHistoryEntry(
    @SerialName("currentData") val currentData: ApiEnvironmentData? = null,
    @SerialName("deviceState") val deviceState: ApiDeviceState? = null,
    @SerialName("alarm") val alarm: ApiAlarm? = null
) {
    fun toModel(): EnvironmentData {
        val envData = currentData?.toModel() ?: EnvironmentData(25f, 60f, 18f, 0L)
        val alarmInfo = alarm?.toModel() ?: AlarmInfo()
        val ds = deviceState?.toModel() ?: DeviceState()
        return envData.copy(
            alarmCode = alarmInfo.code,
            alarmMessage = alarmInfo.message,
            deviceState = ds
        )
    }
}

@Serializable
data class DeviceHistoryResponse(
    @SerialName("device_id") val deviceId: String? = null,
    val history: List<ApiHistoryEntry> = emptyList(),
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
