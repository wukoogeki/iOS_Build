package org.project.data

enum class WorkMode {
    NORMAL,
    DEHUMIDIFY,
    HEAT,
    VENTILATE,
    ALARM,
    OFFLINE
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
    val alarmMessage: String = "normal",
    val deviceState: DeviceState = DeviceState()
)

/**
 * 设备执行机构状态
 * 依据统一通信协议：加热器、风扇、雾化器、制冷器、蜂鸣器
 */
data class DeviceState(
    val heater: DeviceStatus = DeviceStatus.OFF,
    val fan: DeviceStatus = DeviceStatus.OFF,
    val atomizer: DeviceStatus = DeviceStatus.OFF,
    val cooling: DeviceStatus = DeviceStatus.OFF,
    val buzzer: DeviceStatus = DeviceStatus.OFF
)

/**
 * 单个告警码信息，依据 alarm_code表与说明.md
 */
data class AlarmCodeInfo(
    val code: Int,
    val category: String,
    val meaning: String,
    val severity: AlarmSeverity
)

/**
 * 告警码定义表 (0-16位bitmask)
 * 位0: 正常
 * 位1-3: 环境异常 (warning)
 * 位4-6: 传感器故障 (4/5 critical, 6 warning)
 * 位7-8: 执行机构故障 (critical)
 * 位9: 制冷模块故障 (critical)
 * 位10: 天气模拟故障 (warning)
 * 位11: 蜂鸣器故障 (warning)
 * 位12: WiFi故障 (warning) -> 设备离线
 * 位13: MQTT故障 (critical) -> 设备离线
 * 位14: 主控故障 (critical)
 * 位15: 电源故障 (critical)
 * 位16: OLED故障 (warning)
 */
object AlarmCodeTable {
    private val table = mapOf(
        0 to AlarmCodeInfo(0, "-", "正常", AlarmSeverity.NORMAL),
        1 to AlarmCodeInfo(1, "环境异常", "温度异常", AlarmSeverity.WARNING),
        2 to AlarmCodeInfo(2, "环境异常", "湿度异常", AlarmSeverity.WARNING),
        3 to AlarmCodeInfo(3, "环境异常", "光照异常", AlarmSeverity.WARNING),
        4 to AlarmCodeInfo(4, "传感器故障", "温度传感器故障", AlarmSeverity.CRITICAL),
        5 to AlarmCodeInfo(5, "传感器故障", "湿度传感器故障", AlarmSeverity.CRITICAL),
        6 to AlarmCodeInfo(6, "传感器故障", "光照传感器故障", AlarmSeverity.WARNING),
        7 to AlarmCodeInfo(7, "执行机构故障", "风扇故障", AlarmSeverity.CRITICAL),
        8 to AlarmCodeInfo(8, "执行机构故障", "加热片故障", AlarmSeverity.CRITICAL),
        9 to AlarmCodeInfo(9, "执行机构故障", "制冷模块故障", AlarmSeverity.CRITICAL),
        10 to AlarmCodeInfo(10, "天气模拟故障", "雾化器故障", AlarmSeverity.WARNING),
        11 to AlarmCodeInfo(11, "执行机构故障", "蜂鸣器故障", AlarmSeverity.WARNING),
        12 to AlarmCodeInfo(12, "通信故障", "WiFi故障", AlarmSeverity.WARNING),
        13 to AlarmCodeInfo(13, "通信故障", "MQTT故障", AlarmSeverity.CRITICAL),
        14 to AlarmCodeInfo(14, "主控故障", "主控异常", AlarmSeverity.CRITICAL),
        15 to AlarmCodeInfo(15, "电源故障", "电源异常", AlarmSeverity.CRITICAL),
        16 to AlarmCodeInfo(16, "显示故障", "OLED故障", AlarmSeverity.WARNING)
    )

    fun getInfo(code: Int): AlarmCodeInfo = table[code] ?: AlarmCodeInfo(code, "未知", "未知故障", AlarmSeverity.WARNING)

    /**
     * 从 bitmask 解析所有告警码
     * 注意：bitmask 中位0表示正常，如果只有位0被设置则返回空列表
     */
    fun parseFromBitmask(bitmask: Int): List<AlarmCodeInfo> {
        if (bitmask == 0) return emptyList()
        val codes = mutableListOf<AlarmCodeInfo>()
        for (bit in 1..16) {
            if ((bitmask and (1 shl bit)) != 0) {
                codes.add(getInfo(bit))
            }
        }
        // 如果没有任何位被设置（只有位0），返回空列表
        return codes.ifEmpty { emptyList() }
    }

    /**
     * 获取 bitmask 中最严重的级别
     */
    fun getMaxSeverity(bitmask: Int): AlarmSeverity {
        val codes = parseFromBitmask(bitmask)
        return when {
            codes.isEmpty() -> AlarmSeverity.NORMAL
            codes.any { it.severity == AlarmSeverity.CRITICAL } -> AlarmSeverity.CRITICAL
            codes.any { it.severity == AlarmSeverity.WARNING } -> AlarmSeverity.WARNING
            else -> AlarmSeverity.NORMAL
        }
    }

    /**
     * 判断设备是否离线（WiFi故障 或 MQTT故障）
     */
    fun isOffline(bitmask: Int): Boolean {
        return (bitmask and (1 shl 12)) != 0 || (bitmask and (1 shl 13)) != 0
    }

    /**
     * 判断是否有任何异常（非正常的告警码）
     */
    fun hasAnyAlarm(bitmask: Int): Boolean {
        return parseFromBitmask(bitmask).isNotEmpty()
    }
}

data class AlarmInfo(
    val code: Int = 0,
    val message: String = "normal"
) {
    /**
     * 解析后的告警码列表（从 bitmask）
     */
    val alarmCodes: List<AlarmCodeInfo>
        get() = AlarmCodeTable.parseFromBitmask(code)

    /**
     * 是否有异常
     */
    val isAbnormal: Boolean
        get() = AlarmCodeTable.hasAnyAlarm(code)

    /**
     * 设备是否离线
     */
    val isOffline: Boolean
        get() = AlarmCodeTable.isOffline(code)

    /**
     * 最严重的告警级别
     */
    val severity: AlarmSeverity
        get() = AlarmCodeTable.getMaxSeverity(code)

    /**
     * 获取所有告警的描述文本
     */
    val alarmDescriptions: String
        get() = if (alarmCodes.isEmpty()) "正常" else alarmCodes.joinToString(", ") { it.meaning }
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
