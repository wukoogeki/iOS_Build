package org.project

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.project.navigation.Screen
import org.project.ui.components.BottomNavBar
import org.project.ui.components.ToastHost
import org.project.ui.screens.*
import org.project.viewmodel.AppViewModel
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

@Composable
fun App() {
    var themeMode by remember { mutableStateOf(ColorSchemeMode.System) }
    val controller = remember(themeMode) { ThemeController(themeMode) }
    val viewModel = remember { AppViewModel() }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }
    val scrollBehavior = MiuixScrollBehavior(rememberTopAppBarState())

    // Check if user is already logged in
    LaunchedEffect(Unit) {
        if (viewModel.state.isLoggedIn) {
            currentScreen = Screen.Dashboard
        }
    }

    // Show toast when error message changes
    LaunchedEffect(viewModel.errorMessage) {
        if (viewModel.errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }

    // Redirect to login when token is expired
    LaunchedEffect(viewModel.isTokenExpired) {
        if (viewModel.isTokenExpired) {
            currentScreen = Screen.Login
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.dispose()
        }
    }

    MiuixTheme(controller = controller) {
        Scaffold(
            topBar = {
                if (currentScreen != Screen.Login) {
                    SmallTopAppBar(
                        title = currentScreen.title,
                        scrollBehavior = scrollBehavior
                    )
                }
            },
            bottomBar = {
                if (currentScreen != Screen.Login) {
                    BottomNavBar(
                        currentScreen = currentScreen,
                        onScreenSelected = { screen ->
                            if (screen == Screen.Device) {
                                viewModel.loadDevices()
                            }
                            currentScreen = screen
                        },
                        themeMode = themeMode
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding()
                    )
                    .fillMaxSize()
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        val direction = when {
                            // Login to Dashboard - slide from right
                            initialState is Screen.Login && targetState is Screen.Dashboard ->
                                AnimatedContentTransitionScope.SlideDirection.Left
                            // Dashboard to Login - slide from left
                            initialState is Screen.Dashboard && targetState is Screen.Login ->
                                AnimatedContentTransitionScope.SlideDirection.Right
                            // Settings to others - slide based on navigation order
                            initialState is Screen.Settings && targetState is Screen.Dashboard ->
                                AnimatedContentTransitionScope.SlideDirection.Right
                            initialState is Screen.Dashboard && targetState is Screen.Settings ->
                                AnimatedContentTransitionScope.SlideDirection.Left
                            // Default fade
                            else -> null
                        }

                        if (direction != null) {
                            slideIntoContainer(direction, tween(300)) togetherWith
                            slideOutOfContainer(direction, tween(300))
                        } else {
                            fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                        }
                    },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        is Screen.Login -> {
                            LoginScreen(
                                viewModel = viewModel,
                                onLoginSuccess = { currentScreen = Screen.Dashboard }
                            )
                        }
                        is Screen.Dashboard -> {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToDevices = {
                                    viewModel.loadDevices()
                                    currentScreen = Screen.Device
                                }
                            )
                        }
                        is Screen.Device -> {
                            DeviceScreen(
                                viewModel = viewModel,
                                onDeviceSelected = { currentScreen = Screen.Dashboard }
                            )
                        }
                        is Screen.Control -> {
                            ControlScreen(viewModel = viewModel)
                            }
                        is Screen.Settings -> {
                            SettingsScreen(
                                viewModel = viewModel,
                                currentThemeMode = themeMode,
                                onThemeModeChange = { themeMode = it },
                                onLogout = {
                                    viewModel.logout()
                                    currentScreen = Screen.Login
                                }
                            )
                        }
                    }
                }

                // Toast overlay
                ToastHost(
                    message = viewModel.errorMessage,
                    onDismiss = { viewModel.clearError() },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            // 紧急告警背景（淡红色闪烁）
            if (viewModel.abnormalAlertUrgent) {
                UrgentAlertOverlay()
            }
        }

        // 严重告警弹窗
        if (viewModel.abnormalAlertVisible) {
            AbnormalAlertDialog(
                count = viewModel.abnormalAlertCount,
                onConfirm = {
                    viewModel.dismissAbnormalAlert()
                    viewModel.loadDevices()
                    currentScreen = Screen.Device
                },
                onDismiss = { viewModel.dismissAbnormalAlert() }
            )
        }
    }
}

@Composable
private fun UrgentAlertOverlay() {
    var alpha by remember { mutableStateOf(0.15f) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(800)
            alpha = if (alpha > 0.1f) 0.05f else 0.25f
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFCDD2).copy(alpha = alpha))
    )
}

@Composable
private fun AbnormalAlertDialog(
    count: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    OverlayDialog(
        show = true,
        title = "严重告警",
        summary = "检测到 $count 台设备严重告警，请立即查看！",
        onDismissRequest = onDismiss
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(text = "忽略", onClick = onDismiss)
            Spacer(Modifier.width(8.dp))
            TextButton(text = "查看设备", onClick = onConfirm)
        }
    }
}
