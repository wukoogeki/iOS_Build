package org.project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.project.navigation.Screen
import org.project.ui.icons.DashboardIcon
import org.project.ui.icons.ServerIcon
import org.project.ui.icons.SettingsIcon
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 底部导航栏
 *
 * 使用 Miuix 主题：选中态使用 primary 色，未选中态使用 onBackground 60% 透明度。
 * 按下时使用 primary 20% 透明度作为反馈。
 * 图标定义见 [org.project.ui.icons.AppIcons]。
 */
@Composable
fun BottomNavBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MiuixTheme.colorScheme.surface.copy(alpha = 0.85f))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data class NavItem(val screen: Screen, val label: String, val icon: ImageVector)
            val items = listOf(
                NavItem(Screen.Dashboard, "仪表盘", DashboardIcon),
                NavItem(Screen.Device, "设备", ServerIcon),
                NavItem(Screen.Settings, "设置", SettingsIcon)
            )

            items.forEach { (screen, label, icon) ->
                val isSelected = currentScreen == screen
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()

                val pressOverlay = if (isPressed) {
                    MiuixTheme.colorScheme.primary.copy(alpha = 0.2f)
                } else {
                    Color.Transparent
                }

                val contentColor = if (isSelected) {
                    MiuixTheme.colorScheme.primary
                } else {
                    MiuixTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(11.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onScreenSelected(screen) }
                        .background(color = pressOverlay, shape = RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = label,
                            color = contentColor,
                            style = MiuixTheme.textStyles.footnote1
                        )
                    }
                }
            }
        }
    }
}
