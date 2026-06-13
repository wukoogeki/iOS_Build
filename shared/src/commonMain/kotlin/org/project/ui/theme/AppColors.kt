package org.project.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 应用统一颜色规范。
 *
 * 颜色对照表（浅色 / 深色）：
 * - 绿色  `#4CAF50` / `#2E7D32`
 * - 橙色  `#FF9800` / `#EF6C00`
 * - 红色  `#F44336` / `#C62828`
 * - 蓝色  跟随 Miuix 主题 primary（自带浅深切换）
 */
object AppColors {
    val Green = Color(0xFF4CAF50)
    val GreenDark = Color(0xFF2E7D32)

    val Orange = Color(0xFFFF9800)
    val OrangeDark = Color(0xFFEF6C00)

    val Red = Color(0xFFF44336)
    val RedDark = Color(0xFFC62828)
}

/** 当前是否处于深色模式（基于 Miuix 主题文字色判断）。 */
val MiuixIsDark: Boolean
    @Composable
    @ReadOnlyComposable
    get() = MiuixTheme.colorScheme.onBackground.luminance() > 0.5f

/**
 * 解析语义颜色：深色模式返回深色变体，浅色模式返回浅色变体。
 * 蓝色始终跟随 Miuix 主题 primary。
 */
@Composable
@ReadOnlyComposable
fun semanticColor(
    light: Color,
    dark: Color
): Color = if (MiuixIsDark) dark else light

/**
 * 主题感知的语义色：返回当前主题对应的 [AppColors] 颜色。
 * 蓝色专用，使用 Miuix 主题 primary。
 */
@Composable
@ReadOnlyComposable
fun appGreen(): Color = semanticColor(AppColors.Green, AppColors.GreenDark)

@Composable
@ReadOnlyComposable
fun appOrange(): Color = semanticColor(AppColors.Orange, AppColors.OrangeDark)

@Composable
@ReadOnlyComposable
fun appRed(): Color = semanticColor(AppColors.Red, AppColors.RedDark)

@Composable
@ReadOnlyComposable
fun appBlue(): Color = MiuixTheme.colorScheme.primary

/**
 * 容器背景色：在 [light] 上叠加深色模式的弱 alpha、深色模式叠加深色 alpha。
 * 用于"轻微高亮"的背景（异常告警条 / 离线状态条等）。
 */
@Composable
@ReadOnlyComposable
fun themedSurface(light: Color, dark: Color, lightAlpha: Float = 0.6f, darkAlpha: Float = 0.25f): Color {
    return if (MiuixIsDark) dark.copy(alpha = darkAlpha) else light.copy(alpha = lightAlpha)
}
