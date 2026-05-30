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
    val timestamp: Long
)

data class DeviceState(
    val fan: DeviceStatus = DeviceStatus.OFF,
    val heater: DeviceStatus = DeviceStatus.OFF,
    val dehumidifier: DeviceStatus = DeviceStatus.OFF
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
    val deviceState: DeviceState = DeviceState()
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
