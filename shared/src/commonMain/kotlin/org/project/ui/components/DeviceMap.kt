package org.project.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import org.project.data.CabinetDevice
import org.project.data.WorkMode
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun DeviceMap(
    devices: List<CabinetDevice>,
    selectedDevice: CabinetDevice?,
    onDeviceSelected: (CabinetDevice) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(Color(0xFFF5F5F5))
                .padding(16.dp)
        ) {
            Text(
                text = "设备分布地图",
                style = MiuixTheme.textStyles.title3,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        color = Color(0xFFE8E8E8),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                // Draw grid background
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val gridColor = Color(0xFFD0D0D0)
                    val gridSize = 40f

                    // Draw vertical lines
                    var x = 0f
                    while (x < size.width) {
                        drawLine(
                            color = gridColor,
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1f
                        )
                        x += gridSize
                    }

                    // Draw horizontal lines
                    var y = 0f
                    while (y < size.height) {
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                        y += gridSize
                    }

                    // Draw area labels
                    drawRect(
                        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width * 0.6f, size.height * 0.5f)
                    )
                    drawRect(
                        color = Color(0xFF2196F3).copy(alpha = 0.1f),
                        topLeft = Offset(0f, size.height * 0.5f),
                        size = Size(size.width * 0.6f, size.height * 0.5f)
                    )
                    drawRect(
                        color = Color(0xFFFF9800).copy(alpha = 0.1f),
                        topLeft = Offset(size.width * 0.6f, 0f),
                        size = Size(size.width * 0.4f, size.height)
                    )
                }

                // Area labels
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "东区",
                        style = MiuixTheme.textStyles.footnote2,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopStart)
                    )
                    Text(
                        text = "西区",
                        style = MiuixTheme.textStyles.footnote2,
                        color = Color(0xFF2196F3),
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.BottomStart)
                    )
                    Text(
                        text = "南区",
                        style = MiuixTheme.textStyles.footnote2,
                        color = Color(0xFFFF9800),
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopEnd)
                    )
                }

                // Device markers
                devices.forEach { device ->
                    val isSelected = selectedDevice?.id == device.id
                    DeviceMarker(
                        device = device,
                        isSelected = isSelected,
                        onClick = { onDeviceSelected(device) },
                        modifier = Modifier.offset(
                            x = (device.x * 100).dp,
                            y = (device.y * 100).dp
                        )
                    )
                }
            }

            // Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(color = Color(0xFF4CAF50), label = "正常")
                LegendItem(color = Color(0xFF2196F3), label = "除湿")
                LegendItem(color = Color(0xFFFF9800), label = "加热")
                LegendItem(color = Color(0xFFF44336), label = "警报")
                LegendItem(color = Color(0xFF9E9E9E), label = "离线")
            }
        }
    }
}

@Composable
private fun DeviceMarker(
    device: CabinetDevice,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = when {
        !device.isOnline -> Color(0xFF9E9E9E)
        device.currentMode == WorkMode.NORMAL -> Color(0xFF4CAF50)
        device.currentMode == WorkMode.DEHUMIDIFY -> Color(0xFF2196F3)
        device.currentMode == WorkMode.HEAT -> Color(0xFFFF9800)
        device.currentMode == WorkMode.VENTILATE -> Color(0xFF9C27B0)
        device.currentMode == WorkMode.ALARM -> Color(0xFFF44336)
        else -> Color(0xFF4CAF50)
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Selection ring
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color.copy(alpha = 0.3f), CircleShape)
            )
        }

        // Device dot
        Box(
            modifier = Modifier
                .size(if (isSelected) 24.dp else 16.dp)
                .background(color, CircleShape)
        )

        // Online indicator
        if (device.isOnline) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color.White, CircleShape)
                    .align(Alignment.BottomEnd)
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(Color(0xFF4CAF50), CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MiuixTheme.textStyles.footnote2,
            color = MiuixTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}
