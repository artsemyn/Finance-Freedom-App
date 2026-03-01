package com.example.financefreedom.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financefreedom.R
import com.example.financefreedom.ui.components.GhostButton
import com.example.financefreedom.ui.components.PrimaryButton
import com.example.financefreedom.ui.theme.AppColors
import com.example.financefreedom.ui.theme.AppType
import kotlinx.coroutines.delay

@Composable
fun EntryScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(120); visible = true }

    val infiniteTransition = rememberInfiniteTransition(label = "ambient")

    val floatY by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 8f,
        animationSpec = infiniteRepeatable(
            animation  = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.06f,
        targetValue   = 0.16f,
        animationSpec = infiniteRepeatable(
            animation  = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        // Ambient glow
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(x = (-80).dp, y = (-80).dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                AppColors.Accent.copy(alpha = glowAlpha),
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
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // ── Top: Logo ──────────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter   = fadeIn(tween(600)) + slideInVertically(tween(600)) { -20 }
            ) {
                Row(
                    modifier              = Modifier.padding(top = 48.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(AppColors.Accent, Color(0xFF2C5EBF)),
                                    start = Offset(0f, 0f),
                                    end   = Offset(100f, 100f)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = "FF",
                            style = AppType.LabelSmall.copy(
                                color         = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                    Text(
                        text  = "Finance Freedom",
                        style = AppType.BodySmall.copy(color = AppColors.Secondary)
                    )
                }
            }

            // ── Center: Hero text + gambar ─────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {

                AnimatedVisibility(
                    visible = visible,
                    enter   = fadeIn(tween(700, 200)) + slideInVertically(tween(700, 200)) { 30 }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text  = buildAnnotatedString {
                                append("Kendali\n")
                                withStyle(SpanStyle(color = AppColors.Accent)) { append("Penuh") }
                                append(" atas\nKeuanganmu.")
                            },
                            style = AppType.DisplayMedium.copy(color = AppColors.Primary)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text  = "Pantau pemasukan, pengeluaran,\ndan tabunganmu dalam satu tempat.",
                            style = AppType.BodyMedium.copy(color = AppColors.Secondary)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = visible,
                    enter   = fadeIn(tween(800, 350)) + slideInVertically(tween(800, 350)) { 50 }
                ) {
                    HeroImageCard(floatY = floatY)
                }
            }

            // ── Bottom: CTAs ───────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter   = fadeIn(tween(700, 550)) + slideInVertically(tween(700, 550)) { 40 }
            ) {
                Column(
                    modifier            = Modifier.padding(bottom = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PrimaryButton(text = "Mulai Sekarang", onClick = onGetStarted)
                    GhostButton(text = "Sudah punya akun? Masuk", onClick = onLogin)
                    Text(
                        text     = "Gratis. Tanpa kartu kredit.",
                        style    = AppType.BodySmall.copy(color = AppColors.Tertiary),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

// ── Hero Image Card ────────────────────────────────────────────────────────────
// Langkah pakai gambar:
//   1. Simpan file gambarmu (PNG/JPG/WEBP) ke folder: app/src/main/res/drawable/
//   2. Beri nama file: img_hero.png  (atau nama lain, sesuaikan di R.drawable.img_hero)
//   3. Pastikan nama file huruf kecil semua dan tidak ada spasi
@Composable
private fun HeroImageCard(floatY: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .offset(y = (-floatY).dp)
            .clip(RoundedCornerShape(24.dp))
    ) {

        Image(
            painter            = painterResource(id = R.drawable.img_hero),
            contentDescription = "Finance illustration",
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.6f to Color.Transparent,
                            1.0f to AppColors.Background.copy(alpha = 0.6f)
                        )
                    )
                )
        )
    }
}