package org.project.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
            SystemInfoCard()
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
                        .clickable { onThemeChange(mode) }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = MiuixTheme.textStyles.body2
                    )
                    Text(
                        text = if (isSelected) "✓" else "",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.primary
                    )
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
private fun SystemInfoCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "系统信息",
                style = MiuixTheme.textStyles.title3,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            InfoRow(label = "系统名称", value = "环网柜微环境控制系统")
            Spacer(Modifier.height(8.dp))
            InfoRow(label = "软件版本", value = "v1.0.0")
            Spacer(Modifier.height(8.dp))
            InfoRow(label = "硬件版本", value = "HW-2024-A")
            Spacer(Modifier.height(8.dp))
            InfoRow(label = "通信协议", value = "Modbus RTU / TCP")
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
            color = MiuixTheme.colorScheme.secondary
        )
        Text(
            text = value,
            style = MiuixTheme.textStyles.body2
        )
    }
}

@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    Button(
        onClick = onLogout,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColorsPrimary()
    ) {
        Text("退出登录")
    }
}