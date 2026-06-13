package org.project.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * 仪表盘（dashboard）图标，来源于 .Icons/dashboard_fill.svg
 */
val DashboardIcon: ImageVector = ImageVector.Builder(
    name = "DashboardIcon",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.NonZero
    ) {
        moveTo(3f, 12f)
        curveTo(3f, 12.5523f, 3.44772f, 13f, 4f, 13f)
        horizontalLineTo(10f)
        curveTo(10.5523f, 13f, 11f, 12.5523f, 11f, 12f)
        verticalLineTo(4f)
        curveTo(11f, 3.44772f, 10.5523f, 3f, 10f, 3f)
        horizontalLineTo(4f)
        curveTo(3.44772f, 3f, 3f, 3.44772f, 3f, 4f)
        verticalLineTo(12f)
        close()
        moveTo(3f, 20f)
        curveTo(3f, 20.5523f, 3.44772f, 21f, 4f, 21f)
        horizontalLineTo(10f)
        curveTo(10.5523f, 21f, 11f, 20.5523f, 11f, 20f)
        verticalLineTo(16f)
        curveTo(11f, 15.4477f, 10.5523f, 15f, 10f, 15f)
        horizontalLineTo(4f)
        curveTo(3.44772f, 15f, 3f, 15.4477f, 3f, 16f)
        verticalLineTo(20f)
        close()
        moveTo(13f, 20f)
        curveTo(13f, 20.5523f, 13.4477f, 21f, 14f, 21f)
        horizontalLineTo(20f)
        curveTo(20.5523f, 21f, 21f, 20.5523f, 21f, 20f)
        verticalLineTo(12f)
        curveTo(21f, 11.4477f, 20.5523f, 11f, 20f, 11f)
        horizontalLineTo(14f)
        curveTo(13.4477f, 11f, 13f, 11.4477f, 13f, 12f)
        verticalLineTo(20f)
        close()
        moveTo(14f, 3f)
        curveTo(13.4477f, 3f, 13f, 3.44772f, 13f, 4f)
        verticalLineTo(8f)
        curveTo(13f, 8.55228f, 13.4477f, 9f, 14f, 9f)
        horizontalLineTo(20f)
        curveTo(20.5523f, 9f, 21f, 8.55228f, 21f, 8f)
        verticalLineTo(4f)
        curveTo(21f, 3.44772f, 20.5523f, 3f, 20f, 3f)
        horizontalLineTo(14f)
        close()
    }
}.build()

/**
 * 服务器（server）图标，来源于 .Icons/server_fill.svg
 */
val ServerIcon: ImageVector = ImageVector.Builder(
    name = "ServerIcon",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.NonZero
    ) {
        moveTo(4f, 3f)
        horizontalLineTo(20f)
        curveTo(20.5523f, 3f, 21f, 3.44772f, 21f, 4f)
        verticalLineTo(11f)
        horizontalLineTo(3f)
        verticalLineTo(4f)
        curveTo(3f, 3.44772f, 3.44772f, 3f, 4f, 3f)
        close()
        moveTo(3f, 13f)
        horizontalLineTo(21f)
        verticalLineTo(20f)
        curveTo(21f, 20.5523f, 20.5523f, 21f, 20f, 21f)
        horizontalLineTo(4f)
        curveTo(3.44772f, 21f, 3f, 20.5523f, 3f, 20f)
        verticalLineTo(13f)
        close()
        moveTo(7f, 16f)
        verticalLineTo(18f)
        horizontalLineTo(10f)
        verticalLineTo(16f)
        horizontalLineTo(7f)
        close()
        moveTo(7f, 6f)
        verticalLineTo(8f)
        horizontalLineTo(10f)
        verticalLineTo(6f)
        horizontalLineTo(7f)
        close()
    }
}.build()

/**
 * 设置（settings-3）图标，来源于 .Icons/settings_3_fill.svg
 */
val SettingsIcon: ImageVector = ImageVector.Builder(
    name = "SettingsIcon",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.NonZero
    ) {
        moveTo(9.95401f, 2.2106f)
        curveTo(11.2876f, 1.93144f, 12.6807f, 1.92263f, 14.0449f, 2.20785f)
        curveTo(14.2219f, 3.3674f, 14.9048f, 4.43892f, 15.9997f, 5.07103f)
        curveTo(17.0945f, 5.70313f, 18.364f, 5.75884f, 19.4566f, 5.3323f)
        curveTo(20.3858f, 6.37118f, 21.0747f, 7.58203f, 21.4997f, 8.87652f)
        curveTo(20.5852f, 9.60958f, 19.9997f, 10.736f, 19.9997f, 11.9992f)
        curveTo(19.9997f, 13.2632f, 20.5859f, 14.3902f, 21.5013f, 15.1232f)
        curveTo(21.29f, 15.7636f, 21.0104f, 16.3922f, 20.6599f, 16.9992f)
        curveTo(20.3094f, 17.6063f, 19.9049f, 18.1627f, 19.4559f, 18.6659f)
        curveTo(18.3634f, 18.2396f, 17.0943f, 18.2955f, 15.9997f, 18.9274f)
        curveTo(14.9057f, 19.559f, 14.223f, 20.6294f, 14.0453f, 21.7879f)
        curveTo(12.7118f, 22.067f, 11.3187f, 22.0758f, 9.95443f, 21.7906f)
        curveTo(9.77748f, 20.6311f, 9.09451f, 19.5595f, 7.99967f, 18.9274f)
        curveTo(6.90484f, 18.2953f, 5.63539f, 18.2396f, 4.54272f, 18.6662f)
        curveTo(3.61357f, 17.6273f, 2.92466f, 16.4164f, 2.49964f, 15.1219f)
        curveTo(3.41412f, 14.3889f, 3.99968f, 13.2624f, 3.99968f, 11.9992f)
        curveTo(3.99968f, 10.7353f, 3.41344f, 9.60827f, 2.49805f, 8.87524f)
        curveTo(2.70933f, 8.23482f, 2.98894f, 7.60629f, 3.33942f, 6.99923f)
        curveTo(3.68991f, 6.39217f, 4.09443f, 5.83576f, 4.54341f, 5.33257f)
        curveTo(5.63593f, 5.75881f, 6.90507f, 5.703f, 7.99967f, 5.07103f)
        curveTo(9.09364f, 4.43942f, 9.7764f, 3.3691f, 9.95401f, 2.2106f)
        close()
        moveTo(11.9997f, 14.9992f)
        curveTo(13.6565f, 14.9992f, 14.9997f, 13.6561f, 14.9997f, 11.9992f)
        curveTo(14.9997f, 10.3424f, 13.6565f, 8.99923f, 11.9997f, 8.99923f)
        curveTo(10.3428f, 8.99923f, 8.99967f, 10.3424f, 8.99967f, 11.9992f)
        curveTo(8.99967f, 13.6561f, 10.3428f, 14.9992f, 11.9997f, 14.9992f)
        close()
    }
}.build()

/**
 * 向上箭头（chevron up）图标，来源于 .Icons/chevron_up.xml
 * 用于可折叠区域：展开时显示"向上"表示可收起。
 */
val ChevronUpIcon: ImageVector = ImageVector.Builder(
    name = "ChevronUpIcon",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(4.5f, 15.75f)
        lineToRelative(7.5f, -7.5f)
        lineToRelative(7.5f, 7.5f)
    }
}.build()

/**
 * 向下箭头（chevron down）图标，来源于 .Icons/chevron_down.xml
 * 用于可折叠区域：收起时显示"向下"表示可展开。
 */
val ChevronDownIcon: ImageVector = ImageVector.Builder(
    name = "ChevronDownIcon",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(19.5f, 8.25f)
        lineToRelative(-7.5f, 7.5f)
        lineToRelative(-7.5f, -7.5f)
    }
}.build()
