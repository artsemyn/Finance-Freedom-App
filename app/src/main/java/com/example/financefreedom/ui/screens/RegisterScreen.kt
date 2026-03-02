package com.example.financefreedom.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
fun RegisterScreen(
    authRepository: AuthRepository,
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit,
    onLogin: () -> Unit
) {
    val ui = financeUiColors()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }
    var submitError by remember { mutableStateOf("") }
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
                    onClick = onBack,
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
                    text = "Buat Akun Baru",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ui.primaryText,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Daftar untuk mulai mengatur transaksi, laporan, dan progres finansialmu.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ui.secondaryText
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
                            text = "REGISTER",
                            style = MaterialTheme.typography.labelMedium,
                            color = ui.mutedText
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                emailError = ""
                                submitError = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Email") },
                            singleLine = true,
                            isError = emailError.isNotBlank(),
                            supportingText = {
                                if (emailError.isNotBlank()) {
                                    Text(emailError)
                                }
                            },
                            shape = RoundedCornerShape(18.dp)
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                passwordError = ""
                                submitError = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            isError = passwordError.isNotBlank(),
                            supportingText = {
                                if (passwordError.isNotBlank()) {
                                    Text(passwordError)
                                }
                            },
                            shape = RoundedCornerShape(18.dp)
                        )

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                confirmPasswordError = ""
                                submitError = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Konfirmasi Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            isError = confirmPasswordError.isNotBlank(),
                            supportingText = {
                                if (confirmPasswordError.isNotBlank()) {
                                    Text(confirmPasswordError)
                                }
                            },
                            shape = RoundedCornerShape(18.dp)
                        )

                        if (submitError.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = ui.negative.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = submitError,
                                    color = ui.negative,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                emailError = ""
                                passwordError = ""
                                confirmPasswordError = ""
                                submitError = ""

                                var valid = true
                                if (email.isBlank() || !email.contains("@")) {
                                    emailError = "Email tidak valid"
                                    valid = false
                                }
                                if (password.length < 8) {
                                    passwordError = "Password minimal 8 karakter"
                                    valid = false
                                }
                                if (confirmPassword != password) {
                                    confirmPasswordError = "Konfirmasi password tidak sama"
                                    valid = false
                                }
                                if (!valid) return@Button

                                isLoading = true
                                scope.launch {
                                    authRepository.register(email.trim(), password)
                                        .onSuccess { onRegisterSuccess() }
                                        .onFailure { error ->
                                            submitError = error.message ?: "Registrasi gagal."
                                        }
                                    isLoading = false
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ui.accent,
                                contentColor = ui.onAccent
                            )
                        ) {
                            Text(if (isLoading) "Memproses..." else "Daftar")
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = onLogin,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, ui.outline),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = ui.surface,
                    contentColor = ui.secondaryText
                )
            ) {
                Text("Sudah punya akun? Masuk")
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFDFDFDF)
@Composable
private fun RegisterScreenPreview() {
    FinanceFreedomTheme(darkTheme = false) {
        RegisterScreen(
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
            onRegisterSuccess = {},
            onBack = {},
            onLogin = {}
        )
    }
}
