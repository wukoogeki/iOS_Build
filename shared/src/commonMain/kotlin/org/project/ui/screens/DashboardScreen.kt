package org.project.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import org.project.data.DeviceStatus
import org.project.data.EnvironmentData
import org.project.data.WorkMode
import org.project.viewmodel.AppViewModel
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun DashboardScreen(viewModel: AppViewModel) {
    val state = viewModel.state
    val selectedDevice = state.selectedDevice

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        DeviceOverviewCard(state.devices)
        Spacer(Modifier.height(16.dp))

        if (selectedDevice == null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "请先选择设备",
                        style = MiuixTheme.textStyles.title2,
                        color = MiuixTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "点击底部导航栏「设备」选择要查看的环网柜",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onBackground
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                SelectedDeviceCard(selectedDevice)
                Spacer(Modifier.height(16.dp))
                WorkModeCard(state.currentMode, selectedDevice.isOnline == false)
                Spacer(Modifier.height(16.dp))
            }

            item {
                EnvironmentDataCard(state.currentData)
                Spacer(Modifier.height(16.dp))
            }

            item {
                HistoryChartCard(state.historyData)
                Spacer(Modifier.height(16.dp))
            }

            item {
                DeviceStatusCard(
                    deviceState = state.deviceState,
                    currentMode = state.currentMode,
                    isOnline = selectedDevice.isOnline == false
                )
                Spacer(Modifier.height(16.dp))
            }

            item {
                ControlCard(
                    deviceState = state.deviceState,
                    onFanChange = { viewModel.setDeviceStatus("fan", it) },
                    onHeaterChange = { viewModel.setDeviceStatus("heater", it) },
                    onDehumidifierChange = { viewModel.setDeviceStatus("dehumidifier", it) }
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun DeviceOverviewCard(devices: List<org.project.data.CabinetDevice>) {
    val total = devices.size
    val normal = devices.count { it.isOnline && !it.alarm.isAbnormal }
    val offline = devices.count { !it.isOnline }
    val warning = devices.count { it.isOnline && it.alarm.severity == org.project.data.AlarmSeverity.WARNING }
    val critical = devices.count { it.isOnline && it.alarm.severity == org.project.data.AlarmSeverity.CRITICAL }
    val abnormal = offline + warning + critical

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "设备总览",
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OverviewItem(
                    label = "设备总数",
                    value = total.toString(),
                    color = MiuixTheme.colorScheme.primary
                )
                OverviewItem(
                    label = "正常运行",
                    value = normal.toString(),
                    color = Color(0xFF2E7D32)
                )
                OverviewItem(
                    label = "异常设备",
                    value = abnormal.toString(),
                    color = if (abnormal > 0) Color(0xFFC62828) else Color(0xFF9E9E9E)
                )
            }
            if (abnormal > 0) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OverviewItem(
                        label = "离线",
                        value = offline.toString(),
                        color = Color(0xFF9E9E9E)
                    )
                    OverviewItem(
                        label = "一般告警",
                        value = warning.toString(),
                        color = Color(0xFFFFA726)
                    )
                    OverviewItem(
                        label = "严重告警",
                        value = critical.toString(),
                        color = Color(0xFFC62828)
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MiuixTheme.textStyles.title1,
            color = color
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MiuixTheme.textStyles.footnote2,
            color = MiuixTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun WorkModeCard(mode: WorkMode, isOffline: Boolean = false) {
    val (modeText, modeColor, bgColor) = if (isOffline) {
        Triple("设备离线", Color(0xFF9E9E9E), Color(0xFFF5F5F5))
    } else {
        when (mode) {
            WorkMode.NORMAL -> Triple("正常运行", Color(0xFF2E7D32), Color(0xFFE8F5E9))
            WorkMode.DEHUMIDIFY -> Triple("除湿模式", Color(0xFF1565C0), Color(0xFFE3F2FD))
            WorkMode.HEAT -> Triple("加热模式", Color(0xFFEF6C00), Color(0xFFFFF3E0))
            WorkMode.VENTILATE -> Triple("通风模式", Color(0xFF6A1B9A), Color(0xFFF3E5F5))
            WorkMode.ALARM -> Triple("凝露警报", Color(0xFFC62828), Color(0xFFFFEBEE))
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .background(bgColor)
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = modeColor.copy(alpha = 0.3f),
                        radius = size.minDimension / 2
                    )
                    drawCircle(
                        color = modeColor,
                        radius = size.minDimension / 4,
                        style = Stroke(width = 4f)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = "当前工作模式",
                    style = MiuixTheme.textStyles.footnote1,
                    color = modeColor.copy(alpha = 0.8f)
                )
                Text(
                    text = modeText,
                    style = MiuixTheme.textStyles.title1,
                    color = modeColor
                )
            }
        }
    }
}

@Composable
private fun SelectedDeviceCard(device: org.project.data.CabinetDevice) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MiuixTheme.textStyles.title3,
                    color = MiuixTheme.colorScheme.onBackground
                )
                Text(
                    text = device.location,
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            if (device.isOnline) {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "在线",
                        style = MiuixTheme.textStyles.footnote2,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "离线",
                        style = MiuixTheme.textStyles.footnote2,
                        color = Color(0xFFF44336),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EnvironmentDataCard(data: EnvironmentData) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "实时环境数据",
                style = MiuixTheme.textStyles.title3,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CircularGauge(
                    value = data.temperature,
                    minValue = 0f,
                    maxValue = 50f,
                    label = "温度",
                    unit = "°C",
                    color = Color(0xFFFF9800)
                )
                CircularGauge(
                    value = data.humidity,
                    minValue = 0f,
                    maxValue = 100f,
                    label = "湿度",
                    unit = "%",
                    color = Color(0xFF2196F3)
                )
                CircularGauge(
                    value = data.dewPoint,
                    minValue = 0f,
                    maxValue = 30f,
                    label = "露点",
                    unit = "°C",
                    color = Color(0xFF9C27B0)
                )
            }

            if (data.lightLx > 0) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "光照强度",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${data.lightLx} lx",
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.onBackground
                    )
                }
            }

            if (data.alarmCode != 0) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "告警 (${data.alarmCode}): ${data.alarmMessage}",
                        style = MiuixTheme.textStyles.footnote1,
                        color = Color(0xFFC62828),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CircularGauge(
    value: Float,
    minValue: Float,
    maxValue: Float,
    label: String,
    unit: String,
    color: Color
) {
    val progress = ((value - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 8f
                val diameter = size.minDimension - strokeWidth
                val topLeft = Offset(
                    (size.width - diameter) / 2,
                    (size.height - diameter) / 2
                )

                drawArc(
                    color = color.copy(alpha = 0.15f),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                drawArc(
                    color = color,
                    startAngle = 135f,
                    sweepAngle = 270f * progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = value.format(1),
                    style = MiuixTheme.textStyles.title3,
                    color = MiuixTheme.colorScheme.onBackground
                )
                Text(
                    text = unit,
                    style = MiuixTheme.textStyles.footnote2,
                    color = MiuixTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onBackground
        )
    }
}

private fun Float.format(digits: Int): String {
    var multiplier = 1.0
    repeat(digits) { multiplier *= 10 }
    val rounded = kotlin.math.round(this * multiplier) / multiplier
    return if (digits == 0) {
        rounded.toInt().toString()
    } else {
        val s = rounded.toString()
        if (!s.contains(".")) "$s.${"0".repeat(digits)}"
        else {
            val parts = s.split(".")
            val decimal = parts[1].padEnd(digits, '0').take(digits)
            "${parts[0]}.$decimal"
        }
    }
}

@Composable
private fun HistoryChartCard(historyData: List<EnvironmentData>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "24小时趋势",
                style = MiuixTheme.textStyles.title3,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (historyData.size >= 2) {
                LineChart(historyData)
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "数据收集中...",
                        color = MiuixTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun LineChart(data: List<EnvironmentData>) {
    val temperatures = data.map { it.temperature }
    val humidities = data.map { it.humidity }

    Column {
        Text(
            text = "温度 (°C)",
            style = MiuixTheme.textStyles.footnote2,
            color = Color(0xFFFF9800)
        )
        Spacer(Modifier.height(4.dp))
        SimpleLineChart(
            values = temperatures,
            color = Color(0xFFFF9800),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "湿度 (%)",
            style = MiuixTheme.textStyles.footnote2,
            color = Color(0xFF2196F3)
        )
        Spacer(Modifier.height(4.dp))
        SimpleLineChart(
            values = humidities,
            color = Color(0xFF2196F3),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        )
    }
}

@Composable
private fun SimpleLineChart(
    values: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    val maxValue = values.maxOrNull() ?: 1f
    val minValue = values.minOrNull() ?: 0f
    val range = maxValue - minValue

    Row(modifier = modifier) {
        Column(
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = maxValue.format(1),
                style = MiuixTheme.textStyles.footnote2,
                color = MiuixTheme.colorScheme.secondary
            )
            Text(
                text = ((maxValue + minValue) / 2).format(1),
                style = MiuixTheme.textStyles.footnote2,
                color = MiuixTheme.colorScheme.secondary
            )
            Text(
                text = minValue.format(1),
                style = MiuixTheme.textStyles.footnote2,
                color = MiuixTheme.colorScheme.secondary
            )
        }

        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            if (values.size < 2) return@Canvas

            val stepX = size.width / (values.size - 1)
            val padding = 4.dp.toPx()
            val chartHeight = size.height - 2 * padding

            val points = values.mapIndexed { index, value ->
                val normalized = if (range > 0) {
                    1f - ((value - minValue) / range).coerceIn(0f, 1f)
                } else {
                    0.5f
                }
                Offset(
                    x = index * stepX,
                    y = padding + normalized * chartHeight
                )
            }

            for (i in 0..4) {
                val y = padding + (chartHeight * i / 4)
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }

            for (i in 0 until points.size - 1) {
                drawLine(
                    color = color,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
            }

            points.forEach { point ->
                drawCircle(
                    color = color,
                    radius = 3f,
                    center = point
                )
                drawCircle(
                    color = Color.White,
                    radius = 1.5f,
                    center = point
                )
            }
        }
    }
}

@Composable
private fun DeviceStatusCard(
    deviceState: org.project.data.DeviceState,
    currentMode: org.project.data.WorkMode,
    isOnline: Boolean
) {
    val isAbnormal = !isOnline || currentMode == org.project.data.WorkMode.ALARM

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "执行机构状态",
                style = MiuixTheme.textStyles.title3,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            DeviceStatusItem("风扇", deviceState.fan, Color(0xFF4CAF50), isAbnormal)
            Spacer(Modifier.height(8.dp))
            DeviceStatusItem("加热器", deviceState.heater, Color(0xFFFF9800), isAbnormal)
            Spacer(Modifier.height(8.dp))
            DeviceStatusItem("除湿器", deviceState.dehumidifier, Color(0xFF2196F3), isAbnormal)
        }
    }
}

@Composable
private fun DeviceStatusItem(
    name: String,
    status: org.project.data.DeviceStatus,
    activeColor: Color,
    isAbnormal: Boolean = false
) {
    val (statusText, statusColor) = when {
        isAbnormal -> Pair("异常", Color(0xFFF44336))
        status == org.project.data.DeviceStatus.OFF -> Pair("关闭", activeColor.copy(alpha = 0.5f))
        status == org.project.data.DeviceStatus.ON -> Pair("运行中", activeColor)
        status == org.project.data.DeviceStatus.AUTO -> Pair("自动", activeColor)
        else -> Pair("未知", activeColor)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, style = MiuixTheme.textStyles.body1)
        Surface(
            color = statusColor.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = statusText,
                style = MiuixTheme.textStyles.body2,
                color = statusColor,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun ControlCard(
    deviceState: org.project.data.DeviceState,
    onFanChange: (DeviceStatus) -> Unit,
    onHeaterChange: (DeviceStatus) -> Unit,
    onDehumidifierChange: (DeviceStatus) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "手动控制",
                style = MiuixTheme.textStyles.title3,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            DeviceControlRow("风扇", deviceState.fan, Color(0xFF4CAF50), onFanChange)
            Spacer(Modifier.height(12.dp))
            DeviceControlRow("加热器", deviceState.heater, Color(0xFFFF9800), onHeaterChange)
            Spacer(Modifier.height(12.dp))
            DeviceControlRow("除湿器", deviceState.dehumidifier, Color(0xFF2196F3), onDehumidifierChange)
        }
    }
}

@Composable
private fun DeviceControlRow(
    name: String,
    currentStatus: DeviceStatus,
    activeColor: Color,
    onStatusChange: (DeviceStatus) -> Unit
) {
    Column {
        Text(
            text = name,
            style = MiuixTheme.textStyles.body2,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf(DeviceStatus.OFF, DeviceStatus.ON).forEach { status ->
                val isSelected = currentStatus == status
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onStatusChange(status) }
                        .background(
                            color = if (isSelected) {
                                activeColor
                            } else {
                                activeColor.copy(alpha = 0.15f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (status) {
                            DeviceStatus.OFF -> "关闭"
                            DeviceStatus.ON -> "开启"
                            DeviceStatus.AUTO -> "自动"
                        },
                        color = if (isSelected) {
                            Color.White
                        } else {
                            MiuixTheme.colorScheme.onBackground
                        },
                        style = MiuixTheme.textStyles.footnote1
                    )
                }
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}