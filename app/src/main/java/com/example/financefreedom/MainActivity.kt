package com.example.financefreedom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
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
                val sessionManager = remember { SessionManager() }
                val apiService = remember { ApiClient.getApiService(applicationContext, sessionManager) }
                val authRepository = remember {
                    AuthRepositoryImpl(
                        apiService = apiService,
                        tokenManager = TokenManager(applicationContext)
                    )
                }
                val transactionRepository = remember {
                    TransactionRepositoryImpl(apiService = apiService)
                }
                AppNavGraph(
                    authRepository = authRepository,
                    transactionRepository = transactionRepository,
                    sessionManager = sessionManager
                )
            }
        }
    }
}

@Composable
fun AppPreview() {
    FinanceFreedomTheme {}
}

@Preview(showBackground = true)
@Composable
fun AppPreviewHost() {
    AppPreview()
}
