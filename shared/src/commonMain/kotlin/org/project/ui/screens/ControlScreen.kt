package org.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.project.data.DeviceStatus
import org.project.viewmodel.AppViewModel
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ControlScreen(viewModel: AppViewModel) {
    val state = viewModel.state

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "手动控制",
                style = MiuixTheme.textStyles.title2,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "当前工作模式: ${getModeText(state.currentMode)}",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item {
            DeviceControlCard(
                title = "加热器控制",
                description = "用于低温环境加热，防止凝露",
                currentStatus = state.deviceState.heater,
                activeColor = Color(0xFFFF9800),
                onStatusChange = { viewModel.setDeviceStatus("heater", it) }
            )
            Spacer(Modifier.height(16.dp))
        }

        item {
            DeviceControlCard(
                title = "风扇控制",
                description = "用于通风散热，降低柜内温度",
                currentStatus = state.deviceState.fan,
                activeColor = Color(0xFF4CAF50),
                onStatusChange = { viewModel.setDeviceStatus("fan", it) }
            )
            Spacer(Modifier.height(16.dp))
        }

        item {
            DeviceControlCard(
                title = "雾化器控制",
                description = "用于天气模拟，产生雾化效果",
                currentStatus = state.deviceState.atomizer,
                activeColor = Color(0xFF2196F3),
                onStatusChange = { viewModel.setDeviceStatus("atomizer", it) }
            )
            Spacer(Modifier.height(16.dp))
        }

        item {
            DeviceControlCard(
                title = "制冷器控制",
                description = "用于高温环境制冷降温",
                currentStatus = state.deviceState.cooling,
                activeColor = Color(0xFF9C27B0),
                onStatusChange = { viewModel.setDeviceStatus("cooling", it) }
            )
            Spacer(Modifier.height(16.dp))
        }

        item {
            DeviceControlCard(
                title = "蜂鸣器控制",
                description = "用于报警提示",
                currentStatus = state.deviceState.buzzer,
                activeColor = Color(0xFFF44336),
                onStatusChange = { viewModel.setDeviceStatus("buzzer", it) }
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DeviceControlCard(
    title: String,
    description: String,
    currentStatus: DeviceStatus,
    activeColor: Color,
    onStatusChange: (DeviceStatus) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        style = MiuixTheme.textStyles.title3
                    )
                    Text(
                        text = description,
                        style = MiuixTheme.textStyles.footnote2,
                        color = MiuixTheme.colorScheme.secondary
                    )
                }

                StatusBadge(status = currentStatus, activeColor = activeColor)
            }

            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                DeviceStatus.entries.forEach { status ->
                    val isSelected = currentStatus == status
                    val buttonColor = if (isSelected) {
                        MiuixTheme.colorScheme.primary
                    } else {
                        MiuixTheme.colorScheme.secondary
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onStatusChange(status) }
                            .background(
                                color = if (isSelected) {
                                    MiuixTheme.colorScheme.primary
                                } else {
                                    MiuixTheme.colorScheme.secondary.copy(alpha = 0.15f)
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
                            style = MiuixTheme.textStyles.body2
                        )
                    }
                    if (status != DeviceStatus.entries.last()) {
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: DeviceStatus, activeColor: Color) {
    val (text, color) = when (status) {
        DeviceStatus.OFF -> Pair("关闭", MiuixTheme.colorScheme.secondary)
        DeviceStatus.ON -> Pair("运行", activeColor)
        DeviceStatus.AUTO -> Pair("自动", Color(0xFF9C27B0))
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            style = MiuixTheme.textStyles.footnote2,
            color = color,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

private fun getModeText(mode: org.project.data.WorkMode): String {
    return when (mode) {
        org.project.data.WorkMode.NORMAL -> "正常运行"
        org.project.data.WorkMode.DEHUMIDIFY -> "除湿模式"
        org.project.data.WorkMode.HEAT -> "加热模式"
        org.project.data.WorkMode.VENTILATE -> "通风模式"
        org.project.data.WorkMode.ALARM -> "凝露警报"
        org.project.data.WorkMode.OFFLINE -> "设备离线"
    }
}