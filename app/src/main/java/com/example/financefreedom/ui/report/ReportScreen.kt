package com.example.financefreedom.ui.report

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financefreedom.data.repository.TransactionRepository
import com.example.financefreedom.domain.model.MonthlySummary
import com.example.financefreedom.domain.model.TransactionCategories
import com.example.financefreedom.domain.model.TransactionItem
import com.example.financefreedom.ui.theme.FinanceFreedomTheme
import com.example.financefreedom.ui.theme.financeUiColors
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─── Design Tokens ────────────────────────────────────────────────────────────

private val BgDeep = Color(0xFFDFDFDF)
private val BgCard = Color(0xFFF7F7F4)
private val BgCardAlt = Color(0xFFE5ECE6)
private val AccentGreen = Color(0xFF70AD77)
private val AccentRed = Color(0xFFB85C5C)
private val AccentBlue = Color(0xFF0F5257)
private val TextPrimary = Color(0xFF193032)
private val TextSecond = Color(0xFF47615B)
private val TextMuted = Color(0xFF62716B)
private val DividerCol = Color(0xFFD0D0CA)

private fun formatRupiah(amount: Double): String {
    val fmt = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return "Rp ${fmt.format(amount)}"
}

private fun formatMonthLabel(yyyyMM: String): String {
    return try {
        val date = LocalDate.parse("$yyyyMM-01")
        date.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("id", "ID")))
            .replaceFirstChar { it.uppercase() }
    } catch (e: Exception) { yyyyMM }
}

// ─── Main Screen ──────────────────────────────────────────────────────────────

@Composable
fun ReportScreen(transactionRepository: TransactionRepository) {
    val ui = financeUiColors()
    val currentMonth = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
    }
    var summary by remember { mutableStateOf<MonthlySummary?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val result = transactionRepository.getMonthlySummary(currentMonth)
        isLoading = false
        result.onSuccess { summary = it }
            .onFailure { errorMessage = it.message }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ui.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Header ────────────────────────────────────────────────────
            ReportHeader(monthLabel = formatMonthLabel(currentMonth))

            // ── Content ───────────────────────────────────────────────────
            when {
                isLoading -> LoadingState()
                !errorMessage.isNullOrBlank() -> ErrorBanner(errorMessage.orEmpty())
                summary != null -> ReportContent(summary = summary!!)
                else -> EmptyState()
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── Header ───────────────────────────────────────────────────────────────────

@Composable
private fun ReportHeader(monthLabel: String) {
    val ui = financeUiColors()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Laporan",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ui.primaryText,
                letterSpacing = (-0.5).sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = monthLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ui.secondaryText
                )
            }
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(ui.surface)
                .border(1.dp, ui.outline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.PieChart,
                contentDescription = null,
                tint = ui.accent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─── Report Content ───────────────────────────────────────────────────────────

@Composable
private fun ReportContent(summary: MonthlySummary) {
    val total = summary.totalIncome + summary.totalExpense
    val incomeRatio  = if (total > 0) (summary.totalIncome  / total).toFloat() else 0f
    val expenseRatio = if (total > 0) (summary.totalExpense / total).toFloat() else 0f
    val isPositive   = summary.balance >= 0

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Donut Chart Section ───────────────────────────────────────────
        DonutChartCard(
            incomeRatio  = incomeRatio,
            expenseRatio = expenseRatio,
            balance      = summary.balance,
            isPositive   = isPositive
        )

        // ── Income / Expense Cards ────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                modifier    = Modifier.weight(1f),
                label       = "Pemasukan",
                amount      = summary.totalIncome,
                icon        = Icons.Rounded.ArrowDownward,
                accentColor = AccentGreen
            )
            SummaryCard(
                modifier    = Modifier.weight(1f),
                label       = "Pengeluaran",
                amount      = summary.totalExpense,
                icon        = Icons.Rounded.ArrowUpward,
                accentColor = AccentRed
            )
        }

        // ── Progress Bars Section ─────────────────────────────────────────
        ProgressSection(
            totalIncome  = summary.totalIncome,
            totalExpense = summary.totalExpense,
            total        = total
        )

        // ── Balance Row ───────────────────────────────────────────────────
        BalanceRow(balance = summary.balance, isPositive = isPositive)
    }
}

// ─── Donut Chart Card ─────────────────────────────────────────────────────────

@Composable
private fun DonutChartCard(
    incomeRatio: Float,
    expenseRatio: Float,
    balance: Double,
    isPositive: Boolean
) {
    val ui = financeUiColors()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(ui.surface)
            .border(1.dp, ui.outline, RoundedCornerShape(24.dp))
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "DISTRIBUSI BULAN INI",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = ui.mutedText,
                letterSpacing = 1.5.sp
            )

            // Donut ring
            Box(contentAlignment = Alignment.Center) {
                val animIncome by animateFloatAsState(
                    targetValue = incomeRatio,
                    animationSpec = tween(900),
                    label = "income_anim"
                )
                val animExpense by animateFloatAsState(
                    targetValue = expenseRatio,
                    animationSpec = tween(900),
                    label = "expense_anim"
                )

                // Background ring
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(160.dp),
                    color = ui.surfaceAlt,
                    strokeWidth = 18.dp,
                    strokeCap = StrokeCap.Round
                )
                // Expense arc (below)
                CircularProgressIndicator(
                    progress = { animExpense },
                    modifier = Modifier.size(160.dp),
                    color = ui.negative,
                    strokeWidth = 18.dp,
                    strokeCap = StrokeCap.Round
                )
                // Income arc (on top, offset)
                CircularProgressIndicator(
                    progress = { animIncome },
                    modifier = Modifier.size(136.dp),
                    color = ui.positive,
                    strokeWidth = 18.dp,
                    strokeCap = StrokeCap.Round
                )

                // Center label
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isPositive) "+" else "-",
                        fontSize = 13.sp,
                        color = if (isPositive) ui.positive else ui.negative,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Saldo",
                        fontSize = 11.sp,
                        color = ui.mutedText
                    )
                }
            }

            // Legend row
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = ui.positive, label = "Pemasukan")
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(16.dp)
                        .background(ui.outline)
                )
                LegendItem(color = ui.negative, label = "Pengeluaran")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = label, fontSize = 12.sp, color = TextSecond, fontWeight = FontWeight.Medium)
    }
}

// ─── Summary Card ─────────────────────────────────────────────────────────────

@Composable
private fun SummaryCard(
    modifier: Modifier,
    label: String,
    amount: Double,
    icon: ImageVector,
    accentColor: Color
) {
    val ui = financeUiColors()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(ui.surface)
            .border(1.dp, ui.outline, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = label,
                fontSize = 11.sp,
                color = ui.mutedText,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
            Text(
                text = formatRupiah(amount),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

// ─── Progress Bars Section ────────────────────────────────────────────────────

@Composable
private fun ProgressSection(
    totalIncome: Double,
    totalExpense: Double,
    total: Double
) {
    val ui = financeUiColors()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(ui.surface)
            .border(1.dp, ui.outline, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "PROPORSI",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = ui.mutedText,
                letterSpacing = 1.5.sp
            )
            ProgressRow(
                label       = "Pemasukan",
                amount      = totalIncome,
                ratio       = if (total > 0) (totalIncome / total).toFloat() else 0f,
                accentColor = AccentGreen
            )
            ProgressRow(
                label       = "Pengeluaran",
                amount      = totalExpense,
                ratio       = if (total > 0) (totalExpense / total).toFloat() else 0f,
                accentColor = AccentRed
            )
        }
    }
}

@Composable
private fun ProgressRow(
    label: String,
    amount: Double,
    ratio: Float,
    accentColor: Color
) {
    val ui = financeUiColors()

    val animRatio by animateFloatAsState(
        targetValue = ratio,
        animationSpec = tween(800),
        label = "progress_$label"
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 12.sp, color = ui.secondaryText, fontWeight = FontWeight.Medium)
            Text(
                text = "${(ratio * 100).toInt()}%",
                fontSize = 12.sp,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
        }
        // Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(ui.surfaceAlt)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animRatio)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            listOf(accentColor.copy(alpha = 0.7f), accentColor)
                        )
                    )
            )
        }
        Text(text = formatRupiah(amount), fontSize = 11.sp, color = ui.mutedText)
    }
}

// ─── Balance Row ──────────────────────────────────────────────────────────────

@Composable
private fun BalanceRow(balance: Double, isPositive: Boolean) {
    val ui = financeUiColors()
    val accentColor = if (isPositive) ui.positive else ui.negative

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(accentColor.copy(alpha = 0.08f))
            .border(1.dp, accentColor.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.SwapVert,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = "SALDO BERSIH",
                    fontSize = 10.sp,
                    color = ui.mutedText,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (isPositive) "Surplus bulan ini" else "Defisit bulan ini",
                    fontSize = 12.sp,
                    color = ui.secondaryText,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Text(
            text = "${if (isPositive) "+" else ""}${formatRupiah(balance)}",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = accentColor
        )
    }
}

// ─── Loading State ────────────────────────────────────────────────────────────

@Composable
private fun LoadingState() {
    val ui = financeUiColors()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = ui.positive,
            strokeWidth = 2.dp,
            modifier = Modifier.size(32.dp)
        )
    }
}

// ─── Error Banner ─────────────────────────────────────────────────────────────

@Composable
private fun ErrorBanner(message: String) {
    val ui = financeUiColors()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ui.negative.copy(alpha = 0.1f))
            .border(1.dp, ui.negative.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(ui.negative)
        )
        Text(text = message, fontSize = 13.sp, color = ui.negative, fontWeight = FontWeight.Medium)
    }
}

// ─── Empty State ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyState() {
    val ui = financeUiColors()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "📊", fontSize = 40.sp)
        Text(
            text = "Belum ada ringkasan",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = ui.secondaryText
        )
        Text(
            text = "Data laporan bulan ini belum tersedia",
            fontSize = 13.sp,
            color = ui.mutedText,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFDFDFDF)
@Composable
private fun ReportScreenPreview() {
    FinanceFreedomTheme(darkTheme = false) {
        ReportScreen(
            transactionRepository = object : TransactionRepository {
                override suspend fun getTransactions(): Result<List<TransactionItem>> =
                    Result.success(emptyList())

                override suspend fun getTransactionCategories(forceRefresh: Boolean): Result<TransactionCategories> {
                    return Result.success(TransactionCategories(income = emptyList(), expense = emptyList()))
                }

                override suspend fun createTransaction(
                    title: String,
                    amount: Double,
                    type: String,
                    category: String,
                    date: String,
                    note: String
                ): Result<TransactionItem> =
                    Result.success(TransactionItem("preview", title, amount, type, category, date, note))

                override suspend fun getMonthlySummary(month: String): Result<MonthlySummary> =
                    Result.success(MonthlySummary(9_200_000.0, 3_450_000.0, 5_750_000.0))
            }
        )
    }
}
