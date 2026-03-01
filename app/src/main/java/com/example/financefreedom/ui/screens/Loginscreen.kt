package com.example.financefreedom.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.example.financefreedom.ui.components.*
import com.example.financefreedom.ui.theme.AppColors
import com.example.financefreedom.ui.theme.AppType
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    onLoginSuccess : () -> Unit,
    onBack         : () -> Unit,
    onRegister     : () -> Unit
) {
    var email       by remember { mutableStateOf("") }
    var password    by remember { mutableStateOf("") }
    var isLoading   by remember { mutableStateOf(false) }
    var emailError  by remember { mutableStateOf("") }
    var passError   by remember { mutableStateOf("") }

    val focusManager   = LocalFocusManager.current
    val scrollState    = rememberScrollState()

    // Entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(80); visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        // Ambient glow – bottom right
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 80.dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                AppColors.Accent.copy(alpha = 0.08f),
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
                .verticalScroll(scrollState)
                .padding(horizontal = 28.dp)
        ) {

            Spacer(Modifier.height(20.dp))

            // ── Back button ────────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter   = fadeIn(tween(400))
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.Surface)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null,
                            onClick           = onBack
                        )
                        .drawBehind {
                            drawRoundRect(
                                color       = AppColors.Outline,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
                                style        = androidx.compose.ui.graphics.drawscope.Stroke(1f)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = "←",
                        style = AppType.BodyMedium.copy(color = AppColors.Primary)
                    )
                }
            }

            Spacer(Modifier.height(36.dp))

            // ── Heading ────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter   = fadeIn(tween(600, 100)) + slideInVertically(tween(600, 100)) { 30 }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text  = "Masuk",
                        style = AppType.DisplayMedium.copy(color = AppColors.Primary)
                    )
                    Text(
                        text  = "Lanjutkan perjalanan finansialmu.",
                        style = AppType.BodyMedium.copy(color = AppColors.Secondary)
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            // ── Form fields ────────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter   = fadeIn(tween(600, 200)) + slideInVertically(tween(600, 200)) { 25 }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

                    AppTextField(
                        value         = email,
                        onValueChange = { email = it; emailError = "" },
                        label         = "Email",
                        placeholder   = "nama@email.com",
                        keyboardType  = KeyboardType.Email,
                        imeAction     = ImeAction.Next,
                        isError       = emailError.isNotEmpty(),
                        errorMessage  = emailError
                    )

                    AppTextField(
                        value         = password,
                        onValueChange = { password = it; passError = "" },
                        label         = "Password",
                        placeholder   = "Minimal 8 karakter",
                        isPassword    = true,
                        keyboardType  = KeyboardType.Password,
                        imeAction     = ImeAction.Done,
                        onImeAction   = { focusManager.clearFocus() },
                        isError       = passError.isNotEmpty(),
                        errorMessage  = passError
                    )

                    // Forgot password
                    Text(
                        text     = "Lupa password?",
                        style    = AppType.BodySmall.copy(color = AppColors.Accent),
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = null
                            ) { /* navigate to forgot password */ }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Login button ───────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter   = fadeIn(tween(600, 350)) + slideInVertically(tween(600, 350)) { 20 }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    PrimaryButton(
                        text      = "Masuk",
                        onClick   = {
                            // Basic validation
                            var valid = true
                            if (email.isBlank() || !email.contains("@")) {
                                emailError = "Email tidak valid"
                                valid = false
                            }
                            if (password.length < 8) {
                                passError = "Password minimal 8 karakter"
                                valid = false
                            }
                            if (valid) {
                                isLoading = true
                                // Delegate to caller / ViewModel
                                onLoginSuccess()
                            }
                        },
                        isLoading = isLoading
                    )

                    DividerWithLabel(label = "atau masuk dengan")

                    // Social login row
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SocialButton(
                            label    = "Google",
                            emoji    = "G",
                            modifier = Modifier.weight(1f),
                            onClick  = { /* Google sign-in */ }
                        )
                        SocialButton(
                            label    = "Apple",
                            emoji    = "",
                            modifier = Modifier.weight(1f),
                            onClick  = { /* Apple sign-in */ }
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Footer: register link ──────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter   = fadeIn(tween(600, 500))
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text  = "Belum punya akun? ",
                        style = AppType.BodySmall.copy(color = AppColors.Secondary)
                    )
                    Text(
                        text     = "Daftar gratis",
                        style    = AppType.BodySmall.copy(color = AppColors.Accent),
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null,
                            onClick           = onRegister
                        )
                    )
                }
            }

            Spacer(Modifier.height(36.dp))
        }
    }
}

// ── Social Login Button ────────────────────────────────────────────────────────
@Composable
private fun SocialButton(
    label    : String,
    emoji    : String,
    modifier : Modifier = Modifier,
    onClick  : () -> Unit
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.Surface)
            .drawBehind {
                drawRoundRect(
                    color        = AppColors.Outline,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx()),
                    style        = androidx.compose.ui.graphics.drawscope.Stroke(1f)
                )
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (emoji.isNotEmpty()) {
                Text(
                    text  = emoji,
                    style = AppType.BodyMedium.copy(
                        color      = AppColors.Accent,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                )
            }
            Text(
                text  = label,
                style = AppType.BodySmall.copy(color = AppColors.Secondary)
            )
        }
    }
}