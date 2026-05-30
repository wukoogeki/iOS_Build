package org.project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.project.navigation.Screen
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun BottomNavBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val items = listOf(
            Screen.Dashboard to "仪表盘",
            Screen.Device to "设备",
            Screen.Settings to "设置"
        )

        items.forEach { (screen, label) ->
            val isSelected = currentScreen == screen
            Box(
                modifier = Modifier
                    .clickable { onScreenSelected(screen) }
                    .background(
                        color = if (isSelected) {
                            MiuixTheme.colorScheme.primary.copy(alpha = 0.15f)
                        } else {
                            Color.Transparent
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) {
                        MiuixTheme.colorScheme.primary
                    } else {
                        MiuixTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    },
                    style = if (isSelected) {
                        MiuixTheme.textStyles.body2
                    } else {
                        MiuixTheme.textStyles.body2
                    }
                )
            }
        }
    }
}