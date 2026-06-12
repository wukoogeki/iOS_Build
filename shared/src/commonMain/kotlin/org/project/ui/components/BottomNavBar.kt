package org.project.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.project.navigation.Screen
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlinproject.shared.generated.resources.Res
import kotlinproject.shared.generated.resources.dashboard_fill
import kotlinproject.shared.generated.resources.server_fill
import kotlinproject.shared.generated.resources.settings_3_fill

/**
 * 底部导航栏
 *
 * 使用 Miuix 主题：选中态使用 primary 色，未选中态使用 onBackground 60% 透明度。
 * 按下时使用 primary 20% 透明度作为反馈。
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
            .padding(horizontal = 8.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data class NavItem(val screen: Screen, val label: String, val iconRes: DrawableResource)
            val items = listOf(
                NavItem(Screen.Dashboard, "仪表盘", Res.drawable.dashboard_fill),
                NavItem(Screen.Device, "设备", Res.drawable.server_fill),
                NavItem(Screen.Settings, "设置", Res.drawable.settings_3_fill)
            )

            items.forEach { (screen, label, iconRes) ->
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
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onScreenSelected(screen) }
                        .background(color = pressOverlay, shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(iconRes),
                            contentDescription = label,
                            colorFilter = ColorFilter.tint(contentColor),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = label,
                            color = contentColor,
                            style = MiuixTheme.textStyles.body1
                        )
                    }
                }
            }
        }
    }
}
