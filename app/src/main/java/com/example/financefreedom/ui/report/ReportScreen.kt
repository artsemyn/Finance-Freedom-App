package com.example.financefreedom.ui.report

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financefreedom.data.repository.TransactionRepository
import com.example.financefreedom.domain.model.MonthlySummary
import com.example.financefreedom.domain.model.TransactionCategories
import com.example.financefreedom.domain.model.TransactionItem
import com.example.financefreedom.ui.components.FinanceAnimatedReveal
import com.example.financefreedom.ui.components.FinanceCardSurface
import com.example.financefreedom.ui.components.FinanceMessageBanner
import com.example.financefreedom.ui.theme.FinanceCorners
import com.example.financefreedom.ui.theme.FinanceFreedomTheme
import com.example.financefreedom.ui.theme.FinanceSpacing
import com.example.financefreedom.ui.theme.financeUiColors
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(transactionRepository: TransactionRepository) {
    val viewModel: ReportViewModel = viewModel(
        factory = ReportViewModelFactory(transactionRepository = transactionRepository)
    )
    val state by viewModel.uiState.collectAsState()
    val ui = financeUiColors()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ui.background
    ) {
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            state = rememberPullToRefreshState(),
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    ReportHeader(
                        monthLabel = state.monthLabel,
                        onPreviousMonth = { viewModel.previousMonth() },
                        onNextMonth = { viewModel.nextMonth() }
                    )
                }

                if (state.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = ui.positive,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                } else {
                    item {
                        FinanceAnimatedReveal(index = 1) {
                            SummaryCards(
                                income = state.totalIncome,
                                expense = state.totalExpense,
                                balance = state.balance
                            )
                        }
                    }
                    item {
                        FinanceAnimatedReveal(index = 2) {
                            IncomeExpenseComparisonCard(
                                income = state.totalIncome,
                                expense = state.totalExpense
                            )
                        }
                    }
                    item {
                        FinanceAnimatedReveal(index = 3) {
                            CategoryPieCard(
                                selectedType = state.categoryType,
                                slices = state.categorySlices,
                                onSelectType = { viewModel.setCategoryType(it) }
                            )
                        }
                    }
                    item {
                        FinanceAnimatedReveal(index = 4) {
                            WeeklyBarCard(buckets = state.weeklyBuckets)
                        }
                    }
                    item {
                        FinanceAnimatedReveal(index = 5) {
                            MonthTrendCard(points = state.monthlyTrend)
                        }
                    }
                }

                if (!state.hasSelectedMonthTransactions && !state.isLoading) {
                    item {
                        FinanceMessageBanner(
                            message = "Belum ada transaksi pada bulan ${state.monthLabel}.",
                            isError = false,
                            modifier = Modifier.padding(horizontal = FinanceSpacing.screenHorizontal)
                        )
                    }
                }

                if (!state.errorMessage.isNullOrBlank()) {
                    item {
                        FinanceMessageBanner(
                            message = state.errorMessage.orEmpty(),
                            isError = true,
                            modifier = Modifier.padding(horizontal = FinanceSpacing.screenHorizontal)
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(18.dp)) }
            }
        }
    }
}

@Composable
private fun ReportHeader(
    monthLabel: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
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
                color = ui.primaryText
            )
            Text(
                text = monthLabel,
                fontSize = 13.sp,
                color = ui.secondaryText
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = "Bulan sebelumnya",
                    tint = ui.secondaryText
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(ui.surface, CircleShape)
                    .border(1.dp, ui.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PieChart,
                    contentDescription = null,
                    tint = ui.accent
                )
            }
            IconButton(onClick = onNextMonth) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = "Bulan berikutnya",
                    tint = ui.secondaryText
                )
            }
        }
    }
}

@Composable
private fun SummaryCards(
    income: Double,
    expense: Double,
    balance: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = FinanceSpacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryCard(
            modifier = Modifier.weight(1f),
            title = "Pemasukan",
            value = formatRupiah(income),
            valueColor = financeUiColors().positive
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            title = "Pengeluaran",
            value = formatRupiah(expense),
            valueColor = financeUiColors().negative
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            title = "Saldo",
            value = "${if (balance >= 0) "+ " else "- "}${formatRupiah(kotlin.math.abs(balance))}",
            valueColor = if (balance >= 0) financeUiColors().positive else financeUiColors().negative
        )
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier,
    title: String,
    value: String,
    valueColor: Color
) {
    FinanceCardSurface(
        modifier = modifier
            .fillMaxWidth(),
        cornerRadius = 18.dp,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
    ) {
        val ui = financeUiColors()
        Text(text = title, fontSize = 11.sp, color = ui.mutedText)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun IncomeExpenseComparisonCard(
    income: Double,
    expense: Double
) {
    val total = max(income + expense, 0.0)
    val incomeRatio = if (total > 0) (income / total).toFloat() else 0f
    val expenseRatio = if (total > 0) (expense / total).toFloat() else 0f

    FinanceCardSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = FinanceSpacing.screenHorizontal),
        cornerRadius = FinanceCorners.cardMedium
    ) {
        val ui = financeUiColors()
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = "Income vs Expense", color = ui.primaryText, fontWeight = FontWeight.SemiBold)
        RatioBar(label = "Pemasukan", ratio = incomeRatio, color = ui.positive)
        RatioBar(label = "Pengeluaran", ratio = expenseRatio, color = ui.negative)
        }
    }
}

@Composable
private fun RatioBar(label: String, ratio: Float, color: Color) {
    val ui = financeUiColors()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, fontSize = 12.sp, color = ui.secondaryText)
            Text(text = "${(ratio * 100).toInt()}%", fontSize = 12.sp, color = color, fontWeight = FontWeight.Bold)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(ui.surfaceAlt, RoundedCornerShape(50))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio.coerceIn(0f, 1f))
                    .height(8.dp)
                    .background(color, RoundedCornerShape(50))
            )
        }
    }
}

@Composable
private fun CategoryPieCard(
    selectedType: ReportCategoryType,
    slices: List<CategorySlice>,
    onSelectType: (ReportCategoryType) -> Unit
) {
    val palette = listOf(
        Color(0xFF70AD77),
        Color(0xFF0F5257),
        Color(0xFFF4A261),
        Color(0xFFE76F51),
        Color(0xFF457B9D),
        Color(0xFF2A9D8F)
    )

    FinanceCardSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = FinanceSpacing.screenHorizontal),
        cornerRadius = FinanceCorners.cardMedium
    ) {
        val ui = financeUiColors()
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = "Distribusi Kategori", color = ui.primaryText, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CategoryTypeChip(
                label = "Expense",
                selected = selectedType == ReportCategoryType.EXPENSE,
                onClick = { onSelectType(ReportCategoryType.EXPENSE) }
            )
            CategoryTypeChip(
                label = "Income",
                selected = selectedType == ReportCategoryType.INCOME,
                onClick = { onSelectType(ReportCategoryType.INCOME) }
            )
        }

        if (slices.isEmpty()) {
            Text(
                text = "Belum ada data kategori untuk tipe ini.",
                fontSize = 12.sp,
                color = ui.secondaryText
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Canvas(modifier = Modifier.size(120.dp)) {
                    val stroke = 18.dp.toPx()
                    var startAngle = -90f
                    slices.forEachIndexed { index, slice ->
                        val sweep = slice.ratio * 360f
                        drawArc(
                            color = palette[index % palette.size],
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = Offset(stroke / 2f, stroke / 2f),
                            size = Size(size.width - stroke, size.height - stroke),
                            style = Stroke(width = stroke, cap = StrokeCap.Butt)
                        )
                        startAngle += sweep
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    slices.take(5).forEachIndexed { index, slice ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(palette[index % palette.size], CircleShape)
                                )
                                Text(
                                    text = slice.category,
                                    fontSize = 12.sp,
                                    color = ui.secondaryText
                                )
                            }
                            Text(
                                text = "${(slice.ratio * 100).toInt()}%",
                                fontSize = 12.sp,
                                color = ui.primaryText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun CategoryTypeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val ui = financeUiColors()
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(
                color = if (selected) ui.accent.copy(alpha = 0.16f) else ui.surfaceAlt,
                shape = RoundedCornerShape(999.dp)
            )
            .border(
                width = 1.dp,
                color = if (selected) ui.accent.copy(alpha = 0.4f) else ui.outline,
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (selected) ui.accent else ui.secondaryText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.background(Color.Transparent),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun WeeklyBarCard(buckets: List<WeeklyBucket>) {
    val maxValue = buckets
        .flatMap { listOf(it.income, it.expense) }
        .maxOrNull()
        ?.coerceAtLeast(1.0)
        ?: 1.0

    FinanceCardSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = FinanceSpacing.screenHorizontal),
        cornerRadius = FinanceCorners.cardMedium
    ) {
        val ui = financeUiColors()
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Weekly Bars", color = ui.primaryText, fontWeight = FontWeight.SemiBold)
        if (buckets.isEmpty()) {
            Text("Belum ada data mingguan.", fontSize = 12.sp, color = ui.secondaryText)
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                buckets.forEach { bucket ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 8.dp, height = (72 * (bucket.income / maxValue).toFloat()).dp)
                                    .background(ui.positive, RoundedCornerShape(6.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .size(width = 8.dp, height = (72 * (bucket.expense / maxValue).toFloat()).dp)
                                    .background(ui.negative, RoundedCornerShape(6.dp))
                            )
                        }
                        Text(bucket.weekLabel, fontSize = 11.sp, color = ui.mutedText)
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun MonthTrendCard(points: List<MonthlyTrendPoint>) {
    val maxValue = points
        .flatMap { listOf(it.income, it.expense) }
        .maxOrNull()
        ?.coerceAtLeast(1.0)
        ?: 1.0

    FinanceCardSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = FinanceSpacing.screenHorizontal),
        cornerRadius = FinanceCorners.cardMedium
    ) {
        val ui = financeUiColors()
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = "Month to Month (6 bulan)", color = ui.primaryText, fontWeight = FontWeight.SemiBold)
        if (points.isEmpty()) {
            Text("Belum ada data tren bulanan.", fontSize = 12.sp, color = ui.secondaryText)
        } else {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val spacing = size.width / max(points.size, 1)
                points.forEachIndexed { index, point ->
                    val x = spacing * index + (spacing / 2f)
                    val incomeHeight = (point.income / maxValue).toFloat() * (size.height - 20f)
                    val expenseHeight = (point.expense / maxValue).toFloat() * (size.height - 20f)
                    drawRect(
                        color = ui.positive.copy(alpha = 0.7f),
                        topLeft = Offset(x - 8f, size.height - incomeHeight),
                        size = Size(6f, incomeHeight)
                    )
                    drawRect(
                        color = ui.negative.copy(alpha = 0.75f),
                        topLeft = Offset(x + 2f, size.height - expenseHeight),
                        size = Size(6f, expenseHeight)
                    )
                }
                drawRect(
                    color = ui.outline,
                    topLeft = Offset(0f, size.height - 1f),
                    size = Size(size.width, 1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                points.forEach { point ->
                    Text(
                        text = point.monthLabel,
                        fontSize = 10.sp,
                        color = ui.mutedText
                    )
                }
            }
        }
        }
    }
}

private fun formatRupiah(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
    return "Rp ${formatter.format(amount)}"
}

@Preview(showBackground = true)
@Composable
private fun ReportScreenPreview() {
    val fakeRepository = object : TransactionRepository {
        override suspend fun getTransactions(): Result<List<TransactionItem>> {
            return Result.success(
                listOf(
                    TransactionItem(
                        id = "1",
                        title = "Gaji",
                        amount = 6000000.0,
                        type = "income",
                        category = "Salary",
                        date = "2026-03-02",
                        note = null
                    ),
                    TransactionItem(
                        id = "2",
                        title = "Belanja",
                        amount = 1200000.0,
                        type = "expense",
                        category = "Groceries",
                        date = "2026-03-08",
                        note = null
                    )
                )
            )
        }

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
        ): Result<TransactionItem> = Result.failure(UnsupportedOperationException())

        override suspend fun getMonthlySummary(month: String): Result<MonthlySummary> {
            return Result.success(
                MonthlySummary(
                    totalIncome = 6_000_000.0,
                    totalExpense = 1_200_000.0,
                    balance = 4_800_000.0
                )
            )
        }
    }

    FinanceFreedomTheme {
        ReportScreen(transactionRepository = fakeRepository)
    }
}
