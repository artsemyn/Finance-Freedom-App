package com.example.financefreedom.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Palette ───────────────────────────────────────────────────────────────────
object AppColors {
    // Dark theme surfaces
    val Background     = Color(0xFF0B0E14)
    val Surface        = Color(0xFF13171F)
    val SurfaceVariant = Color(0xFF1C2130)
    val Outline        = Color(0xFF252B3B)

    // Text
    val Primary    = Color(0xFFE8F0FF)  // near-white
    val Secondary  = Color(0xFF6B7A99)  // muted
    val Tertiary   = Color(0xFF3A4357)  // placeholder / disabled

    // Accent — single signature electric blue
    val Accent    = Color(0xFF4F8EF7)
    val AccentDim = Color(0x1A4F8EF7)

    // Semantic
    val Positive = Color(0xFF3DD68C)
    val Negative = Color(0xFFFF6B6B)
}

// ── Font families ─────────────────────────────────────────────────────────────
// Swap FontFamily.Serif / Default to your custom res/font files if desired
val DisplayFont = FontFamily.Serif
val BodyFont    = FontFamily.Default

// ── Type scale ────────────────────────────────────────────────────────────────
object AppType {
    val DisplayLarge = TextStyle(
        fontFamily    = DisplayFont,
        fontWeight    = FontWeight.Light,
        fontSize      = 48.sp,
        lineHeight    = 54.sp,
        letterSpacing = (-1.5).sp
    )
    val DisplayMedium = TextStyle(
        fontFamily    = DisplayFont,
        fontWeight    = FontWeight.Normal,
        fontSize      = 36.sp,
        lineHeight    = 42.sp,
        letterSpacing = (-1).sp
    )
    val HeadlineMedium = TextStyle(
        fontFamily    = BodyFont,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 22.sp,
        lineHeight    = 28.sp,
        letterSpacing = (-0.3).sp
    )
    val BodyMedium = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Normal,
        fontSize   = 15.sp,
        lineHeight = 22.sp
    )
    val BodySmall = TextStyle(
        fontFamily    = BodyFont,
        fontWeight    = FontWeight.Normal,
        fontSize      = 13.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.1.sp
    )
    val LabelSmall = TextStyle(
        fontFamily    = BodyFont,
        fontWeight    = FontWeight.Medium,
        fontSize      = 11.sp,
        lineHeight    = 14.sp,
        letterSpacing = 1.2.sp
    )
}

// ── Color schemes ─────────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary      = AppColors.Accent,
    secondary    = PurpleGrey80,
    tertiary     = Pink80,
    background   = AppColors.Background,
    surface      = AppColors.Surface,
    onPrimary    = Color.White,
    onBackground = AppColors.Primary,
    onSurface    = AppColors.Primary,
    outline      = AppColors.Outline
)

private val LightColorScheme = lightColorScheme(
    primary      = Purple40,
    secondary    = PurpleGrey40,
    tertiary     = Pink40,
    background   = Color(0xFFF5F7FF),
    surface      = Color(0xFFFFFFFF),
    onPrimary    = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface    = Color(0xFF1C1B1F)
)

// ── Theme entry point ─────────────────────────────────────────────────────────
@Composable
fun FinanceFreedomTheme(
    darkTheme    : Boolean = isSystemInDarkTheme(),
    // Dynamic color available on Android 12+ (API 31)
    dynamicColor : Boolean = true,
    content      : @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}