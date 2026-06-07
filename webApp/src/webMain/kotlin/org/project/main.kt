package org.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        // 禁用 Web 端的 Semantics/a11y 树 - 解决 1.12.0-alpha01 的
        // "Node X not found" 崩溃 bug。Web 应用不需要屏幕阅读器语义。
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clearAndSetSemantics {}
        ) {
            WithFontResourcesLoaded {
                App()
            }
        }
    }
}