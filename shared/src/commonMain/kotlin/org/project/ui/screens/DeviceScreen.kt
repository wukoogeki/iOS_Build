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

        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            label = "搜索设备",
            useLabelAsPlaceholder = true
        )

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
                            isAbnormalPressed -> MiuixTheme.colorScheme.primary.copy(alpha = 0.2f)
                            showAbnormalOnly -> MiuixTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else -> MiuixTheme.colorScheme.surface
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

        LazyColumn {
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
}

@Composable
private fun DeviceListItem(
    device: CabinetDevice,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val statusColor = when {
        !device.isOnline -> Color(0xFF9E9E9E)
        device.currentMode == WorkMode.NORMAL -> Color(0xFF4CAF50)
        device.currentMode == WorkMode.DEHUMIDIFY -> Color(0xFF2196F3)
        device.currentMode == WorkMode.HEAT -> Color(0xFFFF9800)
        device.currentMode == WorkMode.VENTILATE -> Color(0xFF9C27B0)
        device.currentMode == WorkMode.ALARM -> Color(0xFFF44336)
        else -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isSelected) {
                        MiuixTheme.colorScheme.primary.copy(alpha = 0.1f)
                    } else {
                        Color.Transparent
                    }
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(statusColor, CircleShape)
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onBackground
                )
                Text(
                    text = device.location,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            if (!device.isOnline) {
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
