package com.example.financefreedom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.example.financefreedom.data.local.SessionManager
import com.example.financefreedom.data.local.TokenManager
import com.example.financefreedom.data.remote.ApiClient
import com.example.financefreedom.data.repository.AuthRepositoryImpl
import com.example.financefreedom.data.repository.TransactionRepositoryImpl
import com.example.financefreedom.ui.navigation.AppNavGraph
import com.example.financefreedom.ui.theme.FinanceFreedomTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinanceFreedomTheme {
                val tokenManager = remember { TokenManager(applicationContext) }
                val sessionManager = remember {
                    SessionManager().apply {
                        updateToken(tokenManager.getToken())
                    }
                }
                val apiService = remember { ApiClient.getApiService(sessionManager) }
                val authRepository = remember {
                    AuthRepositoryImpl(
                        apiService     = apiService,
                        tokenManager   = tokenManager,
                        sessionManager = sessionManager
                    )
                }
                val transactionRepository = remember {
                    TransactionRepositoryImpl(apiService = apiService)
                }

                // ✅ Satu titik masuk — semua navigasi dikelola AppNavGraph
                AppNavGraph(
                    authRepository        = authRepository,
                    transactionRepository = transactionRepository,
                    sessionManager        = sessionManager
                )
            }
        }
    }
}