package org.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.project.data.CabinetDevice
import org.project.data.WorkMode
import org.project.getPlatform
import org.project.ui.theme.appBlue
import org.project.ui.theme.appGreen
import org.project.ui.theme.appOrange
import org.project.ui.theme.appRed
import org.project.ui.theme.themedSurface
import org.project.viewmodel.AppViewModel
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun DeviceScreen(
    viewModel: AppViewModel,
    onDeviceSelected: (CabinetDevice) -> Unit
) {
    val state = viewModel.state
    var searchQuery by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var showAbnormalOnly by remember { mutableStateOf(false) }

    val filteredDevices = remember(state.devices, searchQuery, showAbnormalOnly) {
        var devices = state.devices

        if (showAbnormalOnly) {
            devices = devices.filter { device ->
                !device.isOnline ||
                device.currentMode == WorkMode.ALARM ||
                device.currentMode == WorkMode.HEAT ||
                device.currentMode == WorkMode.DEHUMIDIFY
            }
        }

        if (searchQuery.isNotBlank()) {
            devices = devices.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.location.contains(searchQuery, ignoreCase = true)
            }
        }
        devices
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            text = "设备列表",
            style = MiuixTheme.textStyles.title2
        )

        Spacer(Modifier.height(16.dp))

        SearchBar(
            modifier = Modifier.fillMaxWidth(),
            inputField = {
                InputField(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { expanded = false },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    label = "搜索设备"
                )
            },
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            // 搜索建议列表：用 Column 显式垂直堆叠，
            // 避免 Miuix SearchBar 内容容器默认水平排版把 BasicComponent 横排。
            val suggestions = remember(state.devices, searchQuery) {
                if (searchQuery.isBlank()) state.devices.map { it.name }
                else state.devices.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.location.contains(searchQuery, ignoreCase = true)
                }.map { it.name }
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                suggestions.forEach { suggestion ->
                    BasicComponent(
                        title = suggestion,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            searchQuery = suggestion
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            val abnormalInteractionSource = remember { MutableInteractionSource() }
            val isAbnormalPressed by abnormalInteractionSource.collectIsPressedAsState()

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = abnormalInteractionSource,
                        indication = null
                    ) { showAbnormalOnly = !showAbnormalOnly }
                    .background(
                        when {
                            isAbnormalPressed -> MiuixTheme.colorScheme.primary.copy(alpha = 0.3f)
                            showAbnormalOnly -> MiuixTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else -> MiuixTheme.colorScheme.onBackground.copy(alpha = 0.12f) // 灰色
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "仅显示异常设备",
                    style = MiuixTheme.textStyles.footnote1,
                    color = if (showAbnormalOnly) {
                        MiuixTheme.colorScheme.primary
                    } else {
                        MiuixTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // 设备列表：支持下拉刷新（WebAssembly 平台禁用）
        val supportsPullToRefresh = getPlatform().supportsPullToRefresh
        val deviceListContent: @Composable () -> Unit = {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredDevices) { device ->
                    DeviceListItem(
                        device = device,
                        isSelected = state.selectedDevice?.id == device.id,
                        onClick = {
                            viewModel.selectDevice(device)
                            onDeviceSelected(device)
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        if (supportsPullToRefresh) {
            val pullToRefreshState = rememberPullToRefreshState()
            PullToRefresh(
                isRefreshing = viewModel.isRefreshing,
                onRefresh = { viewModel.refreshAll() },
                pullToRefreshState = pullToRefreshState,
                refreshTexts = listOf(
                    "下拉刷新",
                    "松开刷新",
                    "正在刷新",
                    "刷新成功"
                ),
                modifier = Modifier.weight(1f)
            ) {
                deviceListContent()
            }
        } else {
            Box(modifier = Modifier.weight(1f)) {
                deviceListContent()
            }
        }
    }
}

@Composable
private fun DeviceListItem(
    device: CabinetDevice,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val statusColor = when {
        !device.isOnline -> appOrange() // 离线统一用橙色（深浅可读）
        device.currentMode == WorkMode.NORMAL -> appGreen()
        device.currentMode == WorkMode.DEHUMIDIFY -> appBlue()
        device.currentMode == WorkMode.HEAT -> appOrange()
        device.currentMode == WorkMode.VENTILATE -> appBlue()
        device.currentMode == WorkMode.ALARM -> appRed()
        device.currentMode == WorkMode.OFFLINE -> appOrange()
        else -> appGreen()
    }

    // 异常设备背景色：深浅主题使用不同的色板
    val abnormalBg = when {
        !device.isOnline -> themedSurface(
            light = Color(0xFFFAFAFA),
            dark = Color(0xFF3A3A3A),
            lightAlpha = 1f,
            darkAlpha = 1f
        )
        device.alarm.severity == org.project.data.AlarmSeverity.CRITICAL -> themedSurface(
            light = Color(0xFFFFEBEE),
            dark = Color(0xFF4A1F1F)
        )
        device.alarm.severity == org.project.data.AlarmSeverity.WARNING -> themedSurface(
            light = Color(0xFFFFF3E0),
            dark = Color(0xFF4A3520)
        )
        else -> Color.Transparent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(abnormalBg)
                .background(
                    if (isSelected) {
                        MiuixTheme.colorScheme.primary.copy(alpha = 0.15f)
                    } else {
                        Color.Transparent
                    }
                )
                .padding(vertical = 16.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(12.dp)
                    .background(statusColor, CircleShape)
            )

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = device.name,
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = device.location,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            when {
                !device.isOnline -> {
                    val orange = appOrange()
                    Surface(
                        color = orange.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "离线",
                            style = MiuixTheme.textStyles.footnote2,
                            color = orange,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                device.alarm.severity == org.project.data.AlarmSeverity.CRITICAL -> {
                    val red = appRed()
                    Surface(
                        color = red.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "严重",
                            style = MiuixTheme.textStyles.footnote2,
                            color = red,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                device.alarm.severity == org.project.data.AlarmSeverity.WARNING -> {
                    val orange = appOrange()
                    Surface(
                        color = orange.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "告警",
                            style = MiuixTheme.textStyles.footnote2,
                            color = orange,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
