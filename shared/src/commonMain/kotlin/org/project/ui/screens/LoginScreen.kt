package org.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.project.viewmodel.AppViewModel
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun LoginScreen(
    viewModel: AppViewModel,
    onLoginSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "环网柜微环境控制系统",
            style = MiuixTheme.textStyles.title2,
            color = MiuixTheme.colorScheme.primary
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "自适应控制管理平台",
            style = MiuixTheme.textStyles.subtitle,
            color = MiuixTheme.colorScheme.primary
        )

        Spacer(Modifier.height(48.dp))

        Text(
            text = "用户名",
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = username,
            onValueChange = {
                username = it
                viewModel.clearError()
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "密码",
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = password,
            onValueChange = {
                password = it
                viewModel.clearError()
            },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                if (username.isNotBlank() && password.isNotBlank()) {
                    viewModel.login(username, password) {
                        onLoginSuccess()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColorsPrimary()
        ) {
            Text("登录")
        }
    }
}
