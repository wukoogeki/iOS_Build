package org.project.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 跨平台下拉刷新 + 越界回弹容器（基于 nestedScroll，与 LazyColumn 滚动协同）。
 *
 * 行为组合：
 * | enableRefresh | enableBounce | 顶部下拉              | 底部上滑         |
 * |---------------|--------------|-----------------------|------------------|
 * | true          | true         | 下拉刷新 + 指示器     | 越界回弹         |
 * | true          | false        | 下拉刷新 + 指示器     | LazyColumn 原生  |
 * | false         | true         | 纯越界回弹（无指示器）| 越界回弹         |
 * | false         | false        | LazyColumn 原生       | LazyColumn 原生  |
 *
 * 用法：
 * ```
 * PullToRefresh(
 *     refreshing = vm.isRefreshing,
 *     onRefresh = vm::refreshAll,
 *     enableRefresh = true,
 *     enableBounce = true
 * ) {
 *     LazyColumn { ... }
 * }
 * ```
 */
@Composable
fun PullToRefresh(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    enableRefresh: Boolean = true,
    enableBounce: Boolean = true,
    triggerDistance: Dp = PullToRefreshDefaults.TriggerDistance,
    maxDistance: Dp = PullToRefreshDefaults.MaxDistance,
    bounceDistance: Dp = PullToRefreshDefaults.BounceDistance,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val triggerPx = with(density) { triggerDistance.toPx() }
    val maxPx = with(density) { maxDistance.toPx() }
    val bouncePx = with(density) { bounceDistance.toPx() }

    // 顶部偏移：refresh 与 top-bounce 复用同一个偏移量
    var pullOffset by remember { mutableFloatStateOf(0f) }
    // 底部偏移：独立
    var bottomOverscrollOffset by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    // 刷新成功提示
    var showSuccess by remember { mutableStateOf(false) }

    // 外部刷新结束后，归零 + 显示成功提示
    LaunchedEffect(refreshing) {
        if (!refreshing) {
            pullOffset = 0f
            if (showSuccess) {
                kotlinx.coroutines.delay(1200)
                showSuccess = false
            }
        }
    }

    val nestedScrollConnection = remember(enableRefresh, enableBounce) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // 向上滑动：先消耗底部回弹
                if (available.y < 0 && bottomOverscrollOffset > 0f) {
                    val consumed = available.y.coerceIn(-bottomOverscrollOffset, 0f)
                    bottomOverscrollOffset += consumed
                    return Offset(0f, consumed)
                }
                // 再消耗顶部偏移（refresh 或 top-bounce）
                if (available.y < 0 && pullOffset > 0f) {
                    val consumed = available.y.coerceIn(-pullOffset, 0f)
                    pullOffset += consumed
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // ===== 顶部：下拉手势 =====
                // 优先级：refresh > top-bounce > LazyColumn
                // 只有 LazyColumn 无法消费滚动时（已在顶部），才处理顶部回弹
                if (available.y > 0f && consumed.y == 0f) {
                    // 场景 1：下拉刷新（pullOffset 走 maxPx）
                    if (enableRefresh) {
                        val newOffset = (pullOffset + available.y).coerceIn(0f, maxPx)
                        val consumedY = newOffset - pullOffset
                        pullOffset = newOffset
                        return Offset(0f, consumedY)
                    }
                    // 场景 2：纯顶部回弹（pullOffset 走 bouncePx，更短）
                    if (enableBounce) {
                        val newOffset = (pullOffset + available.y).coerceIn(0f, bouncePx)
                        val consumedY = newOffset - pullOffset
                        pullOffset = newOffset
                        return Offset(0f, consumedY)
                    }
                    // 场景 3：完全交给 LazyColumn
                    return Offset.Zero
                }
                // ===== 底部：上滑手势 =====
                // 只有 LazyColumn 无法消费滚动时（已到底部），才处理底部回弹
                if (available.y < 0f && enableBounce && consumed.y == 0f) {
                    val newOffset = (bottomOverscrollOffset - available.y).coerceIn(0f, bouncePx)
                    val consumedY = -(newOffset - bottomOverscrollOffset)
                    bottomOverscrollOffset = newOffset
                    return Offset(0f, consumedY)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                // 顶部偏移：松手时归零
                if (pullOffset > 0f) {
                    if (enableRefresh && pullOffset >= triggerPx && !refreshing) {
                        onRefresh()
                        showSuccess = true
                        scope.launch {
                            Animatable(pullOffset).animateTo(
                                targetValue = triggerPx,
                                animationSpec = tween(200, easing = FastOutSlowInEasing)
                            ) { pullOffset = value }
                        }
                    } else {
                        scope.launch {
                            Animatable(pullOffset).animateTo(
                                targetValue = 0f,
                                animationSpec = tween(250, easing = FastOutSlowInEasing)
                            ) { pullOffset = value }
                        }
                    }
                }
                // 底部偏移：松手时归零
                if (bottomOverscrollOffset > 0f) {
                    scope.launch {
                        Animatable(bottomOverscrollOffset).animateTo(
                            targetValue = 0f,
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) { bottomOverscrollOffset = value }
                    }
                }
                return Velocity.Zero
            }
        }
    }

    Box(modifier = modifier.nestedScroll(nestedScrollConnection)) {
        // 主体内容：上正下负，整体跟随偏移
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    IntOffset(
                        0,
                        (pullOffset - bottomOverscrollOffset).toInt()
                    )
                }
        ) {
            content()
        }

        // 顶部指示器（仅 enableRefresh=true 时显示文字；纯回弹时不显示）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(density) { pullOffset.toDp() })
                .align(Alignment.TopCenter),
            contentAlignment = Alignment.BottomCenter
        ) {
            if (refreshing) {
                SpinnerIndicator(color = MiuixTheme.colorScheme.primary)
            } else if (enableRefresh && pullOffset > 0f) {
                val progress = (pullOffset / triggerPx).coerceIn(0f, 1f)
                val ready = progress >= 1f
                Text(
                    text = if (ready) "松手刷新" else "下拉刷新",
                    color = MiuixTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            // enableRefresh=false 时，顶部下拉仅做视觉回弹，不显示任何文字
        }

        // 刷新成功提示（仅 enableRefresh=true 时可能显示）
        if (enableRefresh) {
            AnimatedVisibility(
                visible = showSuccess && !refreshing,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                        .wrapContentHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                            .height(40.dp)
                            .background(
                                color = MiuixTheme.colorScheme.primary.copy(alpha = 0.9f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "刷新成功",
                            color = Color.White,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

object PullToRefreshDefaults {
    val TriggerDistance = 72.dp
    val MaxDistance = 120.dp
    val BounceDistance = 80.dp
}

/**
 * 持续旋转的环形指示器（纯 Canvas 实现，跨平台）
 */
@Composable
private fun SpinnerIndicator(
    color: Color,
    size: Dp = 24.dp,
    strokeWidth: Float = 3f
) {
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        rotation.animateTo(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }
    Canvas(
        modifier = Modifier
            .size(size)
            .padding(2.dp)
    ) {
        rotate(rotation.value, pivot = Offset(this.size.width / 2, this.size.height / 2)) {
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = androidx.compose.ui.geometry.Size(
                    width = this.size.width - strokeWidth,
                    height = this.size.height - strokeWidth
                ),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}
