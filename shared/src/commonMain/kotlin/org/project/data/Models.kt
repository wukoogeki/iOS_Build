package org.project.data

enum class WorkMode {
    NORMAL,
    DEHUMIDIFY,
    HEAT,
    VENTILATE,
    ALARM
}

enum class DeviceStatus {
    OFF,
    ON,
    AUTO
}

data class EnvironmentData(
    val temperature: Float,
    val humidity: Float,
    val dewPoint: Float,
    val timestamp: Long,
    val lightLx: Int = 0,
    val alarmCode: Int = 0,
    val alarmMessage: String = "normal"
)

data class DeviceState(
    val fan: DeviceStatus = DeviceStatus.OFF,
    val heater: DeviceStatus = DeviceStatus.OFF,
    val dehumidifier: DeviceStatus = DeviceStatus.OFF
)

data class AlarmInfo(
    val code: Int = 0,
    val message: String = "normal"
) {
    val isAbnormal: Boolean
        get() = code != 0

    /**
     * 异常严重程度，依据 alarm_code表与说明.md:
     * 0=正常, 1-3=环境异常(warning), 4-6=传感器故障(4/5 critical, 6 warning),
     * 7-8=执行机构故障(critical), 9-10=warning,
     * 11=WiFi故障(warning), 12=MQTT故障(critical), 13=主控故障(critical),
     * 14=电源故障(critical), 15=OLED故障(warning)
     */
    val severity: AlarmSeverity
        get() = when (code) {
            0 -> AlarmSeverity.NORMAL
            in 1..3 -> AlarmSeverity.WARNING
            4, 5 -> AlarmSeverity.CRITICAL
            6 -> AlarmSeverity.WARNING
            7, 8 -> AlarmSeverity.CRITICAL
            9, 10 -> AlarmSeverity.WARNING
            11 -> AlarmSeverity.WARNING
            12, 13, 14 -> AlarmSeverity.CRITICAL
            15 -> AlarmSeverity.WARNING
            else -> AlarmSeverity.WARNING
        }
}

enum class AlarmSeverity {
    NORMAL,
    WARNING,
    CRITICAL
}

data class WeatherInfo(
    val weatherType: String = "晴天",
    val startTime: String = ""
)

data class CabinetDevice(
    val id: String,
    val name: String,
    val location: String,
    val x: Float = 0f,
    val y: Float = 0f,
    val isOnline: Boolean = true,
    val currentData: EnvironmentData = EnvironmentData(25.0f, 60.0f, 18.0f, 0L),
    val currentMode: WorkMode = WorkMode.NORMAL,
    val deviceState: DeviceState = DeviceState(),
    val wifiStatus: Int = 1,
    val workStatus: Int = 1,
    val alarm: AlarmInfo = AlarmInfo()
)

data class SystemState(
    val isLoggedIn: Boolean = false,
    val currentUser: String = "",
    val currentMode: WorkMode = WorkMode.NORMAL,
    val deviceState: DeviceState = DeviceState(),
    val currentData: EnvironmentData = EnvironmentData(25.0f, 60.0f, 18.0f, 0L),
    val historyData: List<EnvironmentData> = emptyList(),
    val selectedDevice: CabinetDevice? = null,
    val devices: List<CabinetDevice> = emptyList()
)
