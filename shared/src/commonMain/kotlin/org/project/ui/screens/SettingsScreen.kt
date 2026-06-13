package org.project.ui.screens

import androidx.compose.foundation.background
import org.project.ui.components.clickableWithFeedback
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.project.viewmodel.AppViewModel
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    currentThemeMode: ColorSchemeMode,
    onThemeModeChange: (ColorSchemeMode) -> Unit,
    onLogout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "设置",
                style = MiuixTheme.textStyles.title2,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item {
            ThemeSelectorCard(currentThemeMode, onThemeModeChange)
            Spacer(Modifier.height(16.dp))
        }

        item {
            UserInfoCard(viewModel)
            Spacer(Modifier.height(16.dp))
        }

        item {
            LogoutButton(onLogout)
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ThemeSelectorCard(
    selectedTheme: ColorSchemeMode,
    onThemeChange: (ColorSchemeMode) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "主题设置",
                style = MiuixTheme.textStyles.title3,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val themes = listOf(
                ColorSchemeMode.System to "跟随系统",
                ColorSchemeMode.Light to "浅色模式",
                ColorSchemeMode.Dark to "深色模式"
            )

            themes.forEach { (mode, label) ->
                val isSelected = selectedTheme == mode

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickableWithFeedback { onThemeChange(mode) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        style = MiuixTheme.textStyles.body2
                    )
                    // Radio button style: outer circle with inner dot
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                color = if (isSelected) {
                                    MiuixTheme.colorScheme.primary
                                } else {
                                    MiuixTheme.colorScheme.outline
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserInfoCard(viewModel: AppViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "用户信息",
                style = MiuixTheme.textStyles.title3,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            InfoRow(label = "当前用户", value = viewModel.state.currentUser)
            Spacer(Modifier.height(8.dp))
            InfoRow(label = "登录状态", value = "已登录")
            Spacer(Modifier.height(8.dp))
            InfoRow(label = "用户角色", value = "管理员")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MiuixTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp)
            )
            .clickableWithFeedback { onLogout() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "退出登录",
            color = Color.White,
            style = MiuixTheme.textStyles.body1
        )
    }
}
