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
import androidx.compose.material3.Divider
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financefreedom.data.repository.TransactionRepository
import com.example.financefreedom.domain.model.TransactionItem
import java.text.NumberFormat
import java.util.Locale

// ─── Design Tokens ────────────────────────────────────────────────────────────
private val BgDeep      = Color(0xFF0F1117)
private val BgCard      = Color(0xFF1E2330)
private val BgCardAlt   = Color(0xFF232838)
private val AccentGreen = Color(0xFF34D997)
private val AccentRed   = Color(0xFFFF6B6B)
private val TextPrimary = Color(0xFFF0F2F8)
private val TextSecond  = Color(0xFF8A90A4)
private val TextMuted   = Color(0xFF565C72)
private val DividerCol  = Color(0xFF252A38)

private fun formatRupiah(amount: Double): String {
    val fmt = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return "Rp ${fmt.format(amount)}"
}

private fun isIncome(type: String) =
    type.lowercase() in listOf("income", "pemasukan", "kredit", "credit")

private val filterOptions = listOf("Semua", "Pemasukan", "Pengeluaran")

// ─── Main Screen ──────────────────────────────────────────────────────────────

@Composable
fun HistoryScreen(transactionRepository: TransactionRepository) {
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

    Surface(modifier = Modifier.fillMaxSize(), color = BgDeep) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ── Header ────────────────────────────────────────────────────
            item {
                HistoryHeader()
            }

            // ── Summary + Filter ──────────────────────────────────────────
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
                            color         = TextMuted,
                            letterSpacing = 1.5.sp
                        )
                        Text(text = "${filtered.size} data", fontSize = 12.sp, color = TextMuted)
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

            // ── Loading ───────────────────────────────────────────────────
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

            // ── Error ─────────────────────────────────────────────────────
            if (!errorMessage.isNullOrBlank()) {
                item {
                    ErrorBanner(
                        message  = errorMessage.orEmpty(),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            // ── Empty ─────────────────────────────────────────────────────
            if (!isLoading && filtered.isEmpty() && errorMessage.isNullOrBlank()) {
                item { EmptyState(isFiltered = activeFilter != "Semua") }
            }

            // ── List ──────────────────────────────────────────────────────
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

// ─── Header ───────────────────────────────────────────────────────────────────

@Composable
private fun HistoryHeader() {
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
                color         = TextPrimary,
                letterSpacing = (-0.5).sp
            )
            Text(
                text       = "Semua aktivitas transaksi",
                fontSize   = 14.sp,
                color      = TextSecond,
                fontWeight = FontWeight.Medium
            )
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(BgCard)
                .border(1.dp, DividerCol, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Rounded.History,
                contentDescription = null,
                tint               = AccentGreen,
                modifier           = Modifier.size(20.dp)
            )
        }
    }
}

// ─── Summary Row ──────────────────────────────────────────────────────────────

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
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .border(1.dp, DividerCol, RoundedCornerShape(16.dp))
            .padding(14.dp),
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
            Text(text = label, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Medium, letterSpacing = 0.3.sp)
            Text(text = formatRupiah(amount), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

// ─── Filter Chips ─────────────────────────────────────────────────────────────

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
    val bgColor by animateColorAsState(
        targetValue   = if (isSelected) AccentGreen.copy(alpha = 0.15f) else BgCard,
        animationSpec = tween(200), label = "chip_bg"
    )
    val borderColor by animateColorAsState(
        targetValue   = if (isSelected) AccentGreen.copy(alpha = 0.5f) else DividerCol,
        animationSpec = tween(200), label = "chip_border"
    )
    val textColor by animateColorAsState(
        targetValue   = if (isSelected) AccentGreen else TextSecond,
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

// ─── Transaction Row ──────────────────────────────────────────────────────────

@Composable
private fun TransactionRow(trx: TransactionItem, isLast: Boolean) {
    val income      = isIncome(trx.type)
    val accentColor = if (income) AccentGreen else AccentRed
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
                        color      = TextPrimary,
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
                                .background(BgCardAlt)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = trx.category, fontSize = 10.sp, color = TextSecond, fontWeight = FontWeight.Medium)
                        }
                        Text(text = "·", fontSize = 10.sp, color = TextMuted)
                        Text(text = trx.date, fontSize = 11.sp, color = TextMuted)
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
                Text(text = trx.type, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Medium)
            }
        }

        if (!isLast) {
            Divider(
                color    = DividerCol,
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = 54.dp)
            )
        }
    }
}

// ─── Error Banner ─────────────────────────────────────────────────────────────

@Composable
private fun ErrorBanner(message: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AccentRed.copy(alpha = 0.1f))
            .border(1.dp, AccentRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AccentRed))
        Text(text = message, fontSize = 13.sp, color = AccentRed, fontWeight = FontWeight.Medium)
    }
}

// ─── Empty State ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(isFiltered: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = if (isFiltered) "🔍" else "", fontSize = 40.sp)
        Text(
            text       = if (isFiltered) "Tidak ada hasil" else "Belum ada transaksi",
            fontSize   = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color      = TextSecond
        )
        Text(
            text      = if (isFiltered) "Coba pilih filter yang lain"
            else "Riwayat transaksi kamu akan muncul di sini",
            fontSize  = 13.sp,
            color     = TextMuted,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 32.dp)
        )
    }
}