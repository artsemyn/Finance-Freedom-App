package com.example.financefreedom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.financefreedom.data.local.ReminderNotificationWorker
import com.example.financefreedom.data.local.ReminderScheduler
import com.example.financefreedom.data.local.SessionManager
import com.example.financefreedom.data.local.ThemeMode
import com.example.financefreedom.data.local.ThemeModeManager
import com.example.financefreedom.data.local.TokenManager
import com.example.financefreedom.data.remote.ApiClient
import com.example.financefreedom.data.repository.AuthRepositoryImpl
import com.example.financefreedom.data.repository.ReminderRepositoryImpl
import com.example.financefreedom.data.repository.SavingsRepositoryImpl
import com.example.financefreedom.data.repository.TransactionRepositoryImpl
import com.example.financefreedom.ui.navigation.AppNavGraph
import com.example.financefreedom.ui.theme.FinanceFreedomTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val tokenManager = remember { TokenManager(applicationContext) }
            val sessionManager = remember {
                SessionManager().apply {
                    updateToken(tokenManager.getToken())
                }
            }
            val themeModeManager = remember { ThemeModeManager(applicationContext) }
            var themeMode by remember { mutableStateOf(themeModeManager.getThemeMode()) }
            val systemDarkTheme = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemDarkTheme
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            FinanceFreedomTheme(darkTheme = darkTheme) {
                ReminderNotificationWorker.createNotificationChannel(applicationContext)
                val apiService = remember { ApiClient.getApiService(sessionManager) }
                val authRepository = remember {
                    AuthRepositoryImpl(
                        apiService = apiService,
                        tokenManager = tokenManager,
                        sessionManager = sessionManager
                    )
                }
                val transactionRepository = remember {
                    TransactionRepositoryImpl(apiService = apiService)
                }
                val savingsRepository = remember {
                    SavingsRepositoryImpl(apiService = apiService)
                }
                val reminderRepository = remember {
                    ReminderRepositoryImpl(
                        apiService = apiService,
                        scheduler = ReminderScheduler(applicationContext)
                    )
                }

                AppNavGraph(
                    authRepository = authRepository,
                    transactionRepository = transactionRepository,
                    savingsRepository = savingsRepository,
                    reminderRepository = reminderRepository,
                    sessionManager = sessionManager,
                    themeMode = themeMode,
                    onThemeModeChange = { selectedMode ->
                        themeModeManager.setThemeMode(selectedMode)
                        themeMode = selectedMode
                    }
                )
            }
        }
    }
}
