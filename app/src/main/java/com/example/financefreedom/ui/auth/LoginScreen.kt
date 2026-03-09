package com.example.financefreedom.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financefreedom.data.repository.AuthRepository
import com.example.financefreedom.domain.model.UserProfile
import com.example.financefreedom.ui.theme.FinanceFreedomTheme
import com.example.financefreedom.ui.theme.financeUiColors
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authRepository: AuthRepository,
    onLoginSuccess: () -> Unit,
    onBack: () -> Boolean,
    onRegister: () -> Unit
) {
    val ui = financeUiColors()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ui.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                OutlinedButton(
                    onClick = { onBack() },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ui.outline),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = ui.surface,
                        contentColor = ui.primaryText
                    )
                ) {
                    Text("Kembali")
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Masuk ke Akun",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ui.primaryText,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Akses dashboard keuangan, riwayat transaksi, dan laporan bulananmu.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ui.secondaryTextReadable
                )

                Spacer(Modifier.height(24.dp))

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = ui.surface,
                    tonalElevation = 2.dp,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "LOGIN",
                            style = MaterialTheme.typography.labelMedium,
                            color = ui.mutedTextReadable
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                errorMessage = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_field"),
                            label = { Text("Email") },
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp)
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_field"),
                            label = { Text("Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(18.dp)
                        )

                        if (!errorMessage.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = ui.negative.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = errorMessage.orEmpty(),
                                    color = ui.negative,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                scope.launch {
                                    val result = authRepository.login(email.trim(), password)
                                    isLoading = false
                                    result.onSuccess { onLoginSuccess() }
                                        .onFailure { error ->
                                            errorMessage = error.message ?: "Login gagal."
                                        }
                                }
                            },
                            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("login_submit_button"),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ui.accent,
                                contentColor = ui.onAccent
                            )
                        ) {
                            Text(if (isLoading) "Memproses..." else "Masuk")
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onRegister,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, ui.outline),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = ui.surface,
                        contentColor = ui.secondaryTextReadable
                    )
                ) {
                    Text("Belum punya akun? Daftar")
                }

                Text(
                    text = "Gunakan email aktif dan jangan simpan token di perangkat umum.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ui.mutedTextReadable
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFDFDFDF)
@Composable
private fun LoginScreenPreview() {
    FinanceFreedomTheme(darkTheme = false) {
        LoginScreen(
            authRepository = object : AuthRepository {
                override suspend fun register(email: String, password: String): Result<UserProfile> =
                    Result.success(UserProfile("preview", email))

                override suspend fun login(email: String, password: String): Result<UserProfile> =
                    Result.success(UserProfile("preview", email))

                override suspend fun me(): Result<UserProfile> =
                    Result.success(UserProfile("preview", "preview@financefreedom.app"))

                override fun isLoggedIn(): Boolean = false

                override fun logout() = Unit
            },
            onLoginSuccess = {},
            onBack = { true },
            onRegister = {}
        )
    }
}

