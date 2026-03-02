package com.example.financefreedom.ui.theme

import androidx.compose.ui.graphics.Color

val FinancePrimary = Color(0xFF70AD77)
val FinanceAccent = Color(0xFF0F5257)
val FinanceLightBackground = Color(0xFFDFDFDF)
val FinanceDarkBackground = Color(0xFF1C1C1C)
val FinanceLightSurface = Color(0xFFF7F7F4)
val FinanceDarkSurface = Color(0xFF262626)
val FinanceIncome = Color(0xFF4D8F5A)
val FinanceExpense = Color(0xFFB85C5C)
val FinanceOutlineLight = Color(0xFFD0D0CA)
val FinanceOutlineDark = Color(0xFF3A3A3A)
val FinanceOnLight = Color(0xFF193032)
val FinanceOnDark = Color(0xFFF4F6F2)
val FinanceMutedLight = Color(0xFF5F6D66)
val FinanceMutedDark = Color(0xFFB6C1BA)

object AppColors {
    val Primary = FinanceOnLight
    val Secondary = FinanceMutedLight
    val Tertiary = FinanceMutedLight.copy(alpha = 0.78f)
    val Accent = FinanceAccent
    val Background = FinanceLightBackground
    val Surface = FinanceLightSurface
    val SurfaceVariant = Color(0xFFE9E9E2)
    val Outline = FinanceOutlineLight
    val Positive = FinanceIncome
    val Negative = FinanceExpense
}
