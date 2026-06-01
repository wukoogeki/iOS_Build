package org.project.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.project.navigation.Screen
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurBlendMode
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.highlight.Highlight
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun BottomNavBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    themeMode: ColorSchemeMode = ColorSchemeMode.System
) {
    val isDark = when (themeMode) {
        ColorSchemeMode.Dark, ColorSchemeMode.MonetDark -> true
        ColorSchemeMode.Light, ColorSchemeMode.MonetLight -> false
        ColorSchemeMode.System, ColorSchemeMode.MonetSystem -> isSystemInDarkTheme()
    }

    val backdrop = rememberLayerBackdrop {
        val bgColor = if (isDark) {
            Color(0xFF1C1C1E)
        } else {
            Color(0xFFF2F2F7)
        }
        drawRect(bgColor)
        drawContent()
    }

    val blurColors = if (isDark) {
        BlurDefaults.blurColors(
            blendColors = listOf(
                BlendColorEntry(Color(0xFF2C2C2E).copy(alpha = 0.6f), BlurBlendMode.SrcOver),
                BlendColorEntry(Color(0xFF3A3A3C).copy(alpha = 0.15f), BlurBlendMode.Overlay)
            ),
            brightness = 0.02f,
            contrast = 1.05f,
            saturation = 1.1f
        )
    } else {
        BlurDefaults.blurColors(
            blendColors = listOf(
                BlendColorEntry(Color.White.copy(alpha = 0.55f), BlurBlendMode.SrcOver),
                BlendColorEntry(Color(0xFFF5F5F7).copy(alpha = 0.2f), BlurBlendMode.Screen)
            ),
            brightness = 0.05f,
            contrast = 1.05f,
            saturation = 1.15f
        )
    }

    val highlight = if (isDark) {
        Highlight.GlassStrokeMiddleDark
    } else {
        Highlight.GlassStrokeMiddleLight
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .textureBlur(
                backdrop = backdrop,
                shape = RoundedCornerShape(16.dp),
                blurRadius = 40f,
                colors = blurColors,
                highlight = highlight
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
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
                        style = MiuixTheme.textStyles.body2
                    )
                }
            }
        }
    }
}
