package com.example.financefreedom.ui.history

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financefreedom.data.repository.TransactionRepository
import com.example.financefreedom.domain.model.MonthlySummary
import com.example.financefreedom.domain.model.TransactionCategories
import com.example.financefreedom.domain.model.TransactionItem
import com.example.financefreedom.ui.components.FinanceCardSurface
import com.example.financefreedom.ui.theme.FinanceCorners
import com.example.financefreedom.ui.theme.FinanceSpacing
import com.example.financefreedom.ui.theme.FinanceFreedomTheme
import com.example.financefreedom.ui.theme.financeUiColors
import java.text.NumberFormat
import java.util.Locale

// â”€â”€â”€ Design Tokens â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
private val BgDeep = Color(0xFFDFDFDF)
private val BgCard = Color(0xFFF7F7F4)
private val BgCardAlt = Color(0xFFE8EFE8)
private val AccentGreen = Color(0xFF70AD77)
private val AccentRed = Color(0xFFB85C5C)
private val TextPrimary = Color(0xFF193032)
private val TextSecond = Color(0xFF47615B)
private val TextMuted = Color(0xFF62716B)
private val DividerCol = Color(0xFFD0D0CA)

private fun formatRupiah(amount: Double): String {
    val fmt = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
    return "Rp ${fmt.format(amount)}"
}

private fun isIncome(type: String) =
    type.lowercase() in listOf("income", "pemasukan", "kredit", "credit")

private val filterOptions = listOf("Semua", "Pemasukan", "Pengeluaran")

// â”€â”€â”€ Main Screen â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
fun HistoryScreen(transactionRepository: TransactionRepository) {
    val ui = financeUiColors()
    val transactions = remember { mutableStateListOf<TransactionItem>() }
    var isLoading    by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var activeFilter by remember { mutableStateOf("Semua") }

    LaunchedEffect(Unit) {
        val result = transactionRepository.getTransactions()
        isLoading = false
        result.onSuccess {
            transactions.clear()
            transactions.addAll(it)
        }.onFailure { errorMessage = it.message }
    }

    val filtered = when (activeFilter) {
        "Pemasukan"   -> transactions.filter { isIncome(it.type) }
        "Pengeluaran" -> transactions.filter { !isIncome(it.type) }
        else          -> transactions.toList()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = ui.background) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // â”€â”€ Header â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            item {
                HistoryHeader()
            }

            // â”€â”€ Summary + Filter â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            if (!isLoading && transactions.isNotEmpty()) {
                item {
                    SummaryRow(transactions = transactions)
                    Spacer(Modifier.height(16.dp))
                    FilterChipsRow(
                        activeFilter     = activeFilter,
                        onFilterSelected = { activeFilter = it }
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text          = "TRANSAKSI",
                            fontSize      = 11.sp,
                            fontWeight    = FontWeight.Medium,
                            color         = ui.mutedTextReadable,
                            letterSpacing = 1.5.sp
                        )
                        Text(text = "${filtered.size} data", fontSize = 12.sp, color         = ui.mutedTextReadable)
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

            // â”€â”€ Loading â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color       = AccentGreen,
                            strokeWidth = 2.dp,
                            modifier    = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // â”€â”€ Error â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            if (!errorMessage.isNullOrBlank()) {
                item {
                    ErrorBanner(
                        message  = errorMessage.orEmpty(),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            // â”€â”€ Empty â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            if (!isLoading && filtered.isEmpty() && errorMessage.isNullOrBlank()) {
                item { EmptyState(isFiltered = activeFilter != "Semua") }
            }

            // â”€â”€ List â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            itemsIndexed(filtered) { index, trx ->
                TransactionRow(
                    trx    = trx,
                    isLast = index == filtered.lastIndex
                )
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

// â”€â”€â”€ Header â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun HistoryHeader() {
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
                text          = "Riwayat",
                fontSize      = 26.sp,
                fontWeight    = FontWeight.ExtraBold,
                color         = ui.primaryText,
                letterSpacing = (-0.5).sp
            )
            Text(
                text       = "Semua aktivitas transaksi",
                fontSize   = 14.sp,
                color      = ui.secondaryTextReadable,
                fontWeight = FontWeight.Medium
            )
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
                imageVector        = Icons.Rounded.History,
                contentDescription = null,
                tint               = ui.positive,
                modifier           = Modifier.size(20.dp)
            )
        }
    }
}

// â”€â”€â”€ Summary Row â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun SummaryRow(transactions: List<TransactionItem>) {
    val totalIncome  = transactions.filter { isIncome(it.type) }.sumOf { it.amount }
    val totalExpense = transactions.filter { !isIncome(it.type) }.sumOf { it.amount }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MiniStatCard(
            modifier = Modifier.weight(1f),
            label    = "Total Masuk",
            amount   = totalIncome,
            color    = AccentGreen,
            icon     = Icons.Rounded.ArrowDownward
        )
        MiniStatCard(
            modifier = Modifier.weight(1f),
            label    = "Total Keluar",
            amount   = totalExpense,
            color    = AccentRed,
            icon     = Icons.Rounded.ArrowUpward
        )
    }
}

@Composable
private fun MiniStatCard(
    modifier: Modifier,
    label: String,
    amount: Double,
    color: Color,
    icon: ImageVector
) {
    val ui = financeUiColors()

    FinanceCardSurface(
        modifier = modifier
            .fillMaxWidth(),
        cornerRadius = FinanceCorners.chip
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(color.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Column {
                Text(text = label, fontSize = 10.sp, color = ui.mutedTextReadable, fontWeight = FontWeight.Medium, letterSpacing = 0.3.sp)
                Text(text = formatRupiah(amount), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (color == AccentGreen) ui.positiveText else color)
            }
        }
    }
}

// â”€â”€â”€ Filter Chips â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun FilterChipsRow(activeFilter: String, onFilterSelected: (String) -> Unit) {
    LazyRow(
        contentPadding        = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filterOptions) { option ->
            FilterChip(
                label      = option,
                isSelected = option == activeFilter,
                onClick    = { onFilterSelected(option) }
            )
        }
    }
}

@Composable
private fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val ui = financeUiColors()

    val bgColor by animateColorAsState(
        targetValue   = if (isSelected) ui.positive.copy(alpha = 0.15f) else ui.surface,
        animationSpec = tween(200), label = "chip_bg"
    )
    val borderColor by animateColorAsState(
        targetValue   = if (isSelected) ui.positive.copy(alpha = 0.5f) else ui.outline,
        animationSpec = tween(200), label = "chip_border"
    )
    val textColor by animateColorAsState(
        targetValue   = if (isSelected) ui.positiveText else ui.secondaryTextReadable,
        animationSpec = tween(200), label = "chip_text"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text       = label,
            fontSize   = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color      = textColor
        )
    }
}

// â”€â”€â”€ Transaction Row â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun TransactionRow(trx: TransactionItem, isLast: Boolean) {
    val ui = financeUiColors()
    val income      = isIncome(trx.type)
    val accentColor = if (income) ui.positive else ui.negative
    val initial     = trx.title.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                        .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = initial, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = accentColor)
                }

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        text       = trx.title,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = ui.primaryText,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(3.dp))
                    Row(
                        verticalAlignment      = Alignment.CenterVertically,
                        horizontalArrangement  = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(ui.surfaceAlt)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = trx.category, fontSize = 10.sp, color = ui.secondaryTextReadable, fontWeight = FontWeight.Medium)
                        }
                        Text(text = "Â·", fontSize = 10.sp, color         = ui.mutedTextReadable)
                        Text(text = trx.date, fontSize = 11.sp, color = ui.mutedTextReadable)
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text       = "${if (income) "+" else "-"}${formatRupiah(trx.amount)}",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = accentColor
                )
                Spacer(Modifier.height(2.dp))
                Text(text = trx.type, fontSize = 10.sp, color = ui.mutedTextReadable, fontWeight = FontWeight.Medium)
            }
        }

        if (!isLast) {
            HorizontalDivider(
                color    = ui.outline,
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = 54.dp)
            )
        }
    }
}

// â”€â”€â”€ Error Banner â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun ErrorBanner(message: String, modifier: Modifier = Modifier) {
    val ui = financeUiColors()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ui.negative.copy(alpha = 0.1f))
            .border(1.dp, ui.negative.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ui.negative))
        Text(text = message, fontSize = 13.sp, color = ui.negative, fontWeight = FontWeight.Medium)
    }
}

// â”€â”€â”€ Empty State â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun EmptyState(isFiltered: Boolean) {
    val ui = financeUiColors()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = if (isFiltered) "ðŸ”" else "", fontSize = 40.sp)
        Text(
            text       = if (isFiltered) "Tidak ada hasil" else "Belum ada transaksi",
            fontSize   = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color      = ui.secondaryTextReadable
        )
        Text(
            text      = if (isFiltered) "Coba pilih filter yang lain"
            else "Riwayat transaksi kamu akan muncul di sini",
            fontSize  = 13.sp,
            color     = ui.mutedTextReadable,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFDFDFDF)
@Composable
private fun HistoryScreenPreview() {
    FinanceFreedomTheme(darkTheme = false) {
        HistoryScreen(
            transactionRepository = object : TransactionRepository {
                override suspend fun getTransactions(): Result<List<TransactionItem>> =
                    Result.success(
                        listOf(
                            TransactionItem("1", "Gaji", 8_500_000.0, "income", "Gaji", "2026-03-01", null),
                            TransactionItem("2", "Makan Siang", 55_000.0, "expense", "Makanan", "2026-03-02", null),
                            TransactionItem("3", "Transport Online", 32_000.0, "expense", "Transport", "2026-03-02", null)
                        )
                    )

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
                    Result.success(MonthlySummary(8_500_000.0, 87_000.0, 8_413_000.0))
            }
        )
    }
}


