package com.example.financefreedom.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.financefreedom.data.local.SessionManager
import com.example.financefreedom.data.local.ThemeMode
import com.example.financefreedom.data.repository.AuthRepository
import com.example.financefreedom.data.repository.ReminderRepository
import com.example.financefreedom.data.repository.SavingsRepository
import com.example.financefreedom.data.repository.TransactionRepository
import com.example.financefreedom.utils.CrashLogger
import com.example.financefreedom.ui.add.AddScreen
import com.example.financefreedom.ui.auth.LoginScreen
import com.example.financefreedom.ui.history.HistoryScreen
import com.example.financefreedom.ui.home.HomeScreen
import com.example.financefreedom.ui.profile.ProfileScreen
import com.example.financefreedom.ui.reminder.ReminderScreen
import com.example.financefreedom.ui.report.ReportScreen
import com.example.financefreedom.ui.savings.SavingsScreen
import com.example.financefreedom.ui.screens.EntryScreen
import com.example.financefreedom.ui.screens.RegisterScreen

@Composable
fun AppNavGraph(
    authRepository: AuthRepository,
    transactionRepository: TransactionRepository,
    savingsRepository: SavingsRepository,
    reminderRepository: ReminderRepository,
    sessionManager: SessionManager,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    val navController    = rememberNavController()
    val isLoggedIn       = sessionManager.isLoggedInFlow.collectAsState().value

    // Jika sudah login langsung ke home, jika belum mulai dari entry
    val startDestination = if (sessionManager.isLoggedIn()) Routes.HOME else Routes.ENTRY

    // Auto-redirect ke entry ketika sesi habis / logout
    LaunchedEffect(isLoggedIn) {
        val currentRoute = navController.currentDestination?.route
        val authRoutes   = setOf(Routes.ENTRY, Routes.LOGIN, Routes.REGISTER)
        if (!isLoggedIn && currentRoute !in authRoutes) {
            navController.navigate(Routes.ENTRY) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
            }
        }
        if (isLoggedIn) {
            reminderRepository.syncReminderSchedules()
        }
    }

    NavHost(
        navController    = navController,
        startDestination = startDestination
    ) {

        // ── Auth / Onboarding ────────────────────────────────────────────────
        composable(Routes.ENTRY) {
            LaunchedEffect(Unit) { CrashLogger.setScreen("entry") }
            EntryScreen(
                onGetStarted = { navController.navigate(Routes.REGISTER) },
                onLogin      = { navController.navigate(Routes.LOGIN) }
            )
        }

        composable(Routes.LOGIN) {
            LaunchedEffect(Unit) { CrashLogger.setScreen("login") }
            LoginScreen(
                authRepository = authRepository,
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ENTRY) { inclusive = true }
                    }
                },
                onBack     = { navController.popBackStack() },
                onRegister = {
                    navController.navigate(Routes.REGISTER) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            LaunchedEffect(Unit) { CrashLogger.setScreen("register") }
            RegisterScreen(
                authRepository = authRepository,
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ENTRY) { inclusive = true }
                    }
                },
                onBack  = { navController.popBackStack() },
                onLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        // ── Main Tabs ────────────────────────────────────────────────────────
        composable(Routes.HOME) {
            LaunchedEffect(Unit) { CrashLogger.setScreen("home") }
            MainTabsScaffold(navController = navController) {
                HomeScreen(
                    transactionRepository = transactionRepository,
                    savingsRepository = savingsRepository,
                    reminderRepository = reminderRepository,
                    onOpenSavings = { navController.navigate(Routes.SAVINGS) },
                    onOpenAdd = { navController.navigate(Routes.ADD) },
                    onOpenReminder = { navController.navigate(Routes.REMINDER) }
                )
            }
        }
        composable(Routes.REPORT) {
            LaunchedEffect(Unit) { CrashLogger.setScreen("report") }
            MainTabsScaffold(navController = navController) {
                ReportScreen(transactionRepository = transactionRepository)
            }
        }
        composable(Routes.REMINDER) {
            LaunchedEffect(Unit) { CrashLogger.setScreen("reminder") }
            MainTabsScaffold(navController = navController) {
                ReminderScreen(reminderRepository = reminderRepository)
            }
        }
        composable(Routes.HISTORY) {
            LaunchedEffect(Unit) { CrashLogger.setScreen("history") }
            MainTabsScaffold(navController = navController) {
                HistoryScreen(transactionRepository = transactionRepository)
            }
        }
        composable(Routes.PROFILE) {
            LaunchedEffect(Unit) { CrashLogger.setScreen("profile") }
            MainTabsScaffold(navController = navController) {
                ProfileScreen(
                    authRepository = authRepository,
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange,
                    onLogout = {
                        navController.navigate(Routes.ENTRY) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }
        composable(Routes.SAVINGS) {
            LaunchedEffect(Unit) { CrashLogger.setScreen("savings") }
            SavingsScreen(
                savingsRepository = savingsRepository,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.ADD) {
            LaunchedEffect(Unit) { CrashLogger.setScreen("add") }
            MainTabsScaffold(navController = navController) {
                AddScreen(
                    transactionRepository = transactionRepository,
                    reminderRepository = reminderRepository
                )
            }
        }
    }
}
