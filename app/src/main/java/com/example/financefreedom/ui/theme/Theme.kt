package com.example.financefreedom.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FinancePrimary,
    secondary = FinanceAccent,
    tertiary = FinanceIncome,
    background = FinanceDarkBackground,
    surface = FinanceDarkSurface,
    surfaceVariant = Color(0xFF303730),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = FinanceOnDark,
    onSurface = FinanceOnDark,
    outline = FinanceOutlineDark,
    error = FinanceExpense
)

private val LightColorScheme = lightColorScheme(
    primary = FinancePrimary,
    secondary = FinanceAccent,
    tertiary = FinanceIncome,
    background = FinanceLightBackground,
    surface = FinanceLightSurface,
    surfaceVariant = Color(0xFFE9E9E2),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = FinanceOnLight,
    onSurface = FinanceOnLight,
    outline = FinanceOutlineLight,
    error = FinanceExpense
)

@Composable
fun FinanceFreedomTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
