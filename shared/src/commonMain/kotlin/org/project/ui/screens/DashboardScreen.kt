package org.project.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.project.data.AlarmCodeTable
import org.project.data.AlarmSeverity
import org.project.data.DeviceStatus
import org.project.data.EnvironmentData
import org.project.data.WorkMode
import org.project.ui.components.PullToRefresh
import org.project.viewmodel.AppViewModel
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun DashboardScreen(viewModel: AppViewModel, onNavigateToDevices: () -> Unit = {}) {
    val state = viewModel.state
    val selectedDevice = state.selectedDevice

    // 每 10 秒刷新设备列表（设备总览数据）
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(10_000)
            viewModel.loadDevices()
        }
    }

    val listState = rememberLazyListState()

    // 主内容区：包一层 PullToRefresh，向上滑到顶继续下拉触发刷新
    PullToRefresh(
        refreshing = viewModel.isRefreshing,
        onRefresh = { viewModel.refreshAll() },
        // 空选设备时仅禁用下拉刷新
        enableRefresh = selectedDevice != null,
        enableBounce = false,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(Modifier.height(16.dp))

                // 设备总览：普通卡片，随列表滚动
                DeviceOverviewCard(state.devices, onClick = onNavigateToDevices)
                Spacer(Modifier.height(16.dp))
            }

            if (selectedDevice == null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
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
                }
                return@LazyColumn
            }

            item {
                SelectedDeviceCard(selectedDevice)
                Spacer(Modifier.height(16.dp))
                WorkModeCard(state.currentMode, selectedDevice.isOnline == false)
                Spacer(Modifier.height(16.dp))
                AlarmCodeCard(alarmCode = state.currentData.alarmCode)
                Spacer(Modifier.height(16.dp))
            }

            item {
                EnvironmentDataCard(state.currentData)
                Spacer(Modifier.height(16.dp))
            }

            item {
                // 历史数据反转：最新数据放最右边
                HistoryChartCard(state.historyData.reversed())
                Spacer(Modifier.height(16.dp))
            }

            item {
                DeviceStatusCard(
                    deviceState = state.deviceState,
                    currentMode = state.currentMode,
                    isOnline = selectedDevice.isOnline
                )
                Spacer(Modifier.height(16.dp))
            }

            item {
                ControlCard(
                    deviceState = state.deviceState,
                    onHeaterChange = { viewModel.setDeviceStatus("heater", it) },
                    onFanChange = { viewModel.setDeviceStatus("fan", it) },
                    onAtomizerChange = { viewModel.setDeviceStatus("atomizer", it) },
                    onCoolingChange = { viewModel.setDeviceStatus("cooling", it) },
                    onBuzzerChange = { viewModel.setDeviceStatus("buzzer", it) }
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun DeviceOverviewCard(devices: List<org.project.data.CabinetDevice>, onClick: () -> Unit = {}) {
    val total = devices.size
    val normal = devices.count { it.isOnline && !it.alarm.isAbnormal }
    val offline = devices.count { !it.isOnline }
    val warning = devices.count { it.isOnline && it.alarm.severity == org.project.data.AlarmSeverity.WARNING }
    val critical = devices.count { it.isOnline && it.alarm.severity == org.project.data.AlarmSeverity.CRITICAL }
    val abnormal = offline + warning + critical

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                color = MiuixTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))

            // Row 1: 设备总数 / 正常运行 / 异常设备（始终显示）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                OverviewItem(
                    label = "设备总数",
                    value = total.toString(),
                    color = MiuixTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                OverviewItem(
                    label = "正常运行",
                    value = normal.toString(),
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
                OverviewItem(
                    label = "异常设备",
                    value = abnormal.toString(),
                    color = if (abnormal > 0) Color(0xFFC62828) else Color(0xFF9E9E9E),
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: 离线 / 一般告警 / 严重告警（始终显示，便于一眼看到分布）
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                OverviewItem(
                    label = "离线",
                    value = offline.toString(),
                    color = if (offline > 0) Color(0xFF616161) else Color(0xFF9E9E9E),
                    modifier = Modifier.weight(1f)
                )
                OverviewItem(
                    label = "一般告警",
                    value = warning.toString(),
                    color = if (warning > 0) Color(0xFFFFA726) else Color(0xFF9E9E9E),
                    modifier = Modifier.weight(1f)
                )
                OverviewItem(
                    label = "严重告警",
                    value = critical.toString(),
                    color = if (critical > 0) Color(0xFFC62828) else Color(0xFF9E9E9E),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun OverviewItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MiuixTheme.textStyles.title1,
            color = color,
            maxLines = 1
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MiuixTheme.textStyles.footnote2,
            color = MiuixTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 1,
            softWrap = false
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
            WorkMode.OFFLINE -> Triple("设备离线", Color(0xFF9E9E9E), Color(0xFFF5F5F5))
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
                .heightIn(min = 64.dp)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = device.name,
                    style = MiuixTheme.textStyles.title3,
                    color = MiuixTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(2.dp))
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
                    value = data.lightLx.toFloat(),
                    minValue = 0f,
                    maxValue = 1000f,
                    label = "光照",
                    unit = "lx",
                    color = Color(0xFFFFEB3B)
                )
            }

            val alarmCodes = AlarmCodeTable.parseFromBitmask(data.alarmCode)
            if (alarmCodes.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    alarmCodes.forEach { alarm ->
                        val bgColor = when (alarm.severity) {
                            AlarmSeverity.CRITICAL -> Color(0xFFFFEBEE)
                            AlarmSeverity.WARNING -> Color(0xFFFFF3E0)
                            else -> Color(0xFFF5F5F5)
                        }
                        val textColor = when (alarm.severity) {
                            AlarmSeverity.CRITICAL -> Color(0xFFB71C1C)
                            AlarmSeverity.WARNING -> Color(0xFFE65100)
                            else -> MiuixTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        }
                        Surface(
                            color = bgColor,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "[${alarm.category}] ${alarm.meaning}",
                                style = MiuixTheme.textStyles.footnote1,
                                color = textColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
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
    val targetProgress = ((value - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
    )

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
                    sweepAngle = 270f * animatedProgress,
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
    Column {
        InteractiveLineChart(
            data = data,
            valueSelector = { it.temperature },
            color = Color(0xFFFF9800),
            label = "温度",
            unit = "°C"
        )
        Spacer(Modifier.height(12.dp))
        InteractiveLineChart(
            data = data,
            valueSelector = { it.humidity },
            color = Color(0xFF2196F3),
            label = "湿度",
            unit = "%"
        )
    }
}

/**
 * 可点击的折线图：点击某点弹出该点的时间戳 + 数值详情。
 *
 * @param data 原始数据点（提供 timestamp / 温度/湿度 等）
 * @param valueSelector 取值函数（温度 or 湿度）
 */
@Composable
private fun InteractiveLineChart(
    data: List<EnvironmentData>,
    valueSelector: (EnvironmentData) -> Float,
    color: Color,
    label: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    val values = data.map(valueSelector)
    val maxValue = values.maxOrNull() ?: 1f
    val minValue = values.minOrNull() ?: 0f
    val range = (maxValue - minValue).coerceAtLeast(0.0001f)

    // 当前被选中的数据点索引；null 表示未选
    var selectedIndex by remember(data) { mutableStateOf<Int?>(null) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label ($unit)",
                style = MiuixTheme.textStyles.footnote2,
                color = color,
                modifier = Modifier.weight(1f)
            )
            // 选中时显示：时间 · 数值
            selectedIndex?.let { idx ->
                val point = data.getOrNull(idx)
                if (point != null) {
                    Text(
                        text = "${formatTimestamp(point.timestamp)}  ·  ${valueSelector(point).format(1)}$unit",
                        style = MiuixTheme.textStyles.footnote2,
                        color = MiuixTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))

        Row(modifier = Modifier.fillMaxWidth().height(72.dp)) {
            // Y 轴标签
            Column(
                modifier = Modifier
                    .width(40.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(maxValue.format(1), style = MiuixTheme.textStyles.footnote2, color = MiuixTheme.colorScheme.secondary)
                Text(((maxValue + minValue) / 2).format(1), style = MiuixTheme.textStyles.footnote2, color = MiuixTheme.colorScheme.secondary)
                Text(minValue.format(1), style = MiuixTheme.textStyles.footnote2, color = MiuixTheme.colorScheme.secondary)
            }

            // 画布 + 点击
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .pointerInput(data) {
                        detectTapGestures(
                            onTap = { offset: Offset ->
                                if (values.size < 2) return@detectTapGestures
                                val stepX = size.width / (values.size - 1).toFloat()
                                val rawIdx = (offset.x / stepX).toInt()
                                    .coerceIn(0, values.size - 1)
                                // 选最近点：和左右两点的距离取更近者
                                val nearest = if (rawIdx < values.size - 1) {
                                    val distLeft = offset.x - rawIdx * stepX
                                    val distRight = (rawIdx + 1) * stepX - offset.x
                                    if (distRight < distLeft) rawIdx + 1 else rawIdx
                                } else rawIdx
                                selectedIndex = nearest
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (values.size < 2) return@Canvas

                    val stepX = size.width / (values.size - 1)
                    val padding = 4.dp.toPx()
                    val chartHeight = size.height - 2 * padding

                    // 网格
                    for (i in 0..4) {
                        val y = padding + (chartHeight * i / 4)
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.2f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                    }

                    val points = values.mapIndexed { index, value ->
                        val normalized = if (range > 0) {
                            1f - ((value - minValue) / range).coerceIn(0f, 1f)
                        } else 0.5f
                        Offset(x = index * stepX, y = padding + normalized * chartHeight)
                    }

                    // 折线
                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = color,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 2.5f,
                            cap = StrokeCap.Round
                        )
                    }

                    // 折点
                    points.forEach { p ->
                        drawCircle(color = color, radius = 3f, center = p)
                        drawCircle(color = Color.White, radius = 1.5f, center = p)
                    }

                    // 选中的高亮：垂直虚线 + 大点
                    selectedIndex?.let { idx ->
                        val p = points.getOrNull(idx) ?: return@let
                        // 垂直参考线
                        drawLine(
                            color = color.copy(alpha = 0.6f),
                            start = Offset(p.x, padding),
                            end = Offset(p.x, size.height - padding),
                            strokeWidth = 1.5f
                        )
                        // 高亮点
                        drawCircle(color = color.copy(alpha = 0.25f), radius = 10f, center = p)
                        drawCircle(color = color, radius = 5f, center = p)
                        drawCircle(color = Color.White, radius = 2.5f, center = p)
                    }
                }
            }
        }
    }
}

// 缓存本地时区 + 格式（构造一次复用）
private val localZone: TimeZone = TimeZone.currentSystemDefault()

private val tsFormat: kotlinx.datetime.format.DateTimeFormat<LocalDateTime> =
    LocalDateTime.Format {
        monthNumber(padding = Padding.ZERO)
        char('-')
        dayOfMonth(padding = Padding.ZERO)
        char(' ')
        hour(padding = Padding.ZERO)
        char(':')
        minute(padding = Padding.ZERO)
    }

/** 把 Long(ms) 时间戳格式化为 MM-dd HH:mm（使用 kotlinx-datetime，自动处理时区与月日） */
private fun formatTimestamp(ts: Long): String {
    if (ts <= 0L) return "--:--"
    val ldt = Instant.fromEpochMilliseconds(ts).toLocalDateTime(localZone)
    return ldt.format(tsFormat)
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

            DeviceStatusItem("加热器", deviceState.heater, Color(0xFFFF9800), isAbnormal)
            Spacer(Modifier.height(8.dp))
            DeviceStatusItem("风扇", deviceState.fan, Color(0xFF4CAF50), isAbnormal)
            Spacer(Modifier.height(8.dp))
            DeviceStatusItem("雾化器", deviceState.atomizer, Color(0xFF2196F3), isAbnormal)
            Spacer(Modifier.height(8.dp))
            DeviceStatusItem("制冷器", deviceState.cooling, Color(0xFF9C27B0), isAbnormal)
            Spacer(Modifier.height(8.dp))
            DeviceStatusItem("蜂鸣器", deviceState.buzzer, Color(0xFFF44336), isAbnormal)
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
    onHeaterChange: (DeviceStatus) -> Unit,
    onFanChange: (DeviceStatus) -> Unit,
    onAtomizerChange: (DeviceStatus) -> Unit,
    onCoolingChange: (DeviceStatus) -> Unit,
    onBuzzerChange: (DeviceStatus) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "手动控制",
                style = MiuixTheme.textStyles.title3,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            DeviceControlRow("加热器", deviceState.heater, Color(0xFFFF9800), onHeaterChange)
            Spacer(Modifier.height(12.dp))
            DeviceControlRow("风扇", deviceState.fan, Color(0xFF4CAF50), onFanChange)
            Spacer(Modifier.height(12.dp))
            DeviceControlRow("雾化器", deviceState.atomizer, Color(0xFF2196F3), onAtomizerChange)
            Spacer(Modifier.height(12.dp))
            DeviceControlRow("制冷器", deviceState.cooling, Color(0xFF9C27B0), onCoolingChange)
            Spacer(Modifier.height(12.dp))
            DeviceControlRow("蜂鸣器", deviceState.buzzer, Color(0xFFF44336), onBuzzerChange)
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

/**
 * 告警代码含义卡片：显示当前设备所有触发的告警码及其含义。
 * 右上角有折叠按钮，可展开/收起详细列表。
 */
@Composable
private fun AlarmCodeCard(alarmCode: Int) {
    var expanded by remember { mutableStateOf(true) }
    val alarms = remember(alarmCode) { AlarmCodeTable.parseFromBitmask(alarmCode) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "告警详情",
                    style = MiuixTheme.textStyles.title3
                )
                // 折叠/展开按钮
                Text(
                    text = if (expanded) "收起" else "展开",
                    style = MiuixTheme.textStyles.footnote2,
                    color = MiuixTheme.colorScheme.primary,
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            }

            if (alarms.isEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "当前无告警",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.secondary
                )
            } else {
                Spacer(Modifier.height(12.dp))
                if (expanded) {
                    alarms.forEach { alarm ->
                        AlarmCodeRow(alarm)
                        Spacer(Modifier.height(8.dp))
                    }
                } else {
                    // 收起时只显示最严重的一条 + 数量
                    val criticalCount = alarms.count { it.severity == AlarmSeverity.CRITICAL }
                    val warningCount = alarms.count { it.severity == AlarmSeverity.WARNING }
                    val summary = buildString {
                        if (criticalCount > 0) append("${criticalCount}项严重")
                        if (warningCount > 0) {
                            if (isNotEmpty()) append("，")
                            append("${warningCount}项一般")
                        }
                    }
                    Text(
                        text = summary,
                        style = MiuixTheme.textStyles.body2,
                        color = if (criticalCount > 0) Color(0xFFC62828) else Color(0xFFFFA726)
                    )
                }
            }
        }
    }
}

@Composable
private fun AlarmCodeRow(alarm: org.project.data.AlarmCodeInfo) {
    val color = when (alarm.severity) {
        AlarmSeverity.CRITICAL -> Color(0xFFC62828)
        AlarmSeverity.WARNING -> Color(0xFFFFA726)
        else -> MiuixTheme.colorScheme.secondary
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //  severity 指示点
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, androidx.compose.foundation.shape.CircleShape)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = alarm.meaning,
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onBackground
            )
            Text(
                text = alarm.category,
                style = MiuixTheme.textStyles.footnote2,
                color = MiuixTheme.colorScheme.secondary
            )
        }
        Text(
            text = when (alarm.severity) {
                AlarmSeverity.CRITICAL -> "严重"
                AlarmSeverity.WARNING -> "一般"
                else -> "正常"
            },
            style = MiuixTheme.textStyles.footnote2,
            color = color
        )
    }
}