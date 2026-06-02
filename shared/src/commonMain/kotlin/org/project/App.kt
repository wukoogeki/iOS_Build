package org.project

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.project.navigation.Screen
import org.project.ui.components.BottomNavBar
import org.project.ui.screens.*
import org.project.viewmodel.AppViewModel
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
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
                            DashboardScreen(viewModel = viewModel)
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
            }
        }
    }
}
