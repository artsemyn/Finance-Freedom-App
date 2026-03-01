package com.example.financefreedom.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.financefreedom.data.repository.AuthRepository
import com.example.financefreedom.ui.components.AppTextField
import com.example.financefreedom.ui.components.PrimaryButton
import com.example.financefreedom.ui.theme.AppColors
import com.example.financefreedom.ui.theme.AppType
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    authRepository: AuthRepository,
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit,
    onLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }
    var submitError by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopEnd)
                .offset(x = 70.dp, y = (-50).dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                AppColors.Accent.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.Surface)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBack
                    )
                    .drawBehind {
                        drawRoundRect(
                            color = AppColors.Outline,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(1f)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "<-",
                    style = AppType.BodyMedium.copy(color = AppColors.Primary)
                )
            }

            Spacer(Modifier.height(30.dp))

            Text(
                text = "Buat Akun",
                style = AppType.DisplayMedium.copy(color = AppColors.Primary)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Daftar gratis untuk mulai kelola keuanganmu.",
                style = AppType.BodyMedium.copy(color = AppColors.Secondary)
            )

            Spacer(Modifier.height(34.dp))

            AppTextField(
                value = email,
                onValueChange = { email = it; emailError = ""; submitError = "" },
                label = "Email",
                placeholder = "nama@email.com",
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                isError = emailError.isNotEmpty(),
                errorMessage = emailError
            )

            Spacer(Modifier.height(20.dp))

            AppTextField(
                value = password,
                onValueChange = { password = it; passwordError = ""; submitError = "" },
                label = "Password",
                placeholder = "Minimal 8 karakter",
                isPassword = true,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
                isError = passwordError.isNotEmpty(),
                errorMessage = passwordError
            )

            Spacer(Modifier.height(20.dp))

            AppTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; confirmPasswordError = ""; submitError = "" },
                label = "Konfirmasi Password",
                placeholder = "Ulangi password",
                isPassword = true,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                onImeAction = { focusManager.clearFocus() },
                isError = confirmPasswordError.isNotEmpty(),
                errorMessage = confirmPasswordError
            )

            if (submitError.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = submitError,
                    style = AppType.BodySmall.copy(color = AppColors.Negative)
                )
            }

            Spacer(Modifier.height(28.dp))

            PrimaryButton(
                text = "Daftar gratis",
                isLoading = isLoading,
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

                    if (!valid) return@PrimaryButton

                    isLoading = true
                    scope.launch {
                        authRepository
                            .register(email.trim(), password)
                            .onSuccess { onRegisterSuccess() }
                            .onFailure {
                                submitError = it.message ?: "Registrasi gagal, coba lagi."
                            }
                        isLoading = false
                    }
                }
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sudah punya akun?",
                    style = AppType.BodySmall.copy(color = AppColors.Secondary)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Masuk",
                    style = AppType.BodySmall.copy(color = AppColors.Accent),
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onLogin
                    )
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
