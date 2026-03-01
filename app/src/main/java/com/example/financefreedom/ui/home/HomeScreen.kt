package com.example.financefreedom.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financefreedom.data.repository.TransactionRepository
import com.example.financefreedom.domain.model.TransactionItem
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

// ─── Design Tokens ───────────────────────────────────────────────────────────

private val BgDeep      = Color(0xFF0F1117)
private val BgSurface   = Color(0xFF181C24)
private val BgCard      = Color(0xFF1E2330)
private val BgCardAlt   = Color(0xFF232838)
private val AccentGreen = Color(0xFF34D997)
private val AccentRed   = Color(0xFFFF6B6B)
private val AccentBlue  = Color(0xFF6C8EF5)
private val TextPrimary = Color(0xFFF0F2F8)
private val TextSecond  = Color(0xFF8A90A4)
private val TextMuted   = Color(0xFF565C72)
private val DividerCol  = Color(0xFF252A38)

private val GreenGradient = Brush.horizontalGradient(
    listOf(Color(0xFF34D997), Color(0xFF1DB97A))
)
private val CardGradient = Brush.verticalGradient(
    listOf(Color(0xFF1E2330), Color(0xFF181C24))
)

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun formatRupiah(amount: Double): String {
    val fmt = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return "Rp ${fmt.format(amount)}"
}

private fun isIncome(type: String) =
    type.lowercase() in listOf("income", "pemasukan", "kredit", "credit")

// ─── Main Screen ─────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(transactionRepository: TransactionRepository) {
    val transactions = remember { mutableStateListOf<TransactionItem>() }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val rotateAnim by animateFloatAsState(
        targetValue = if (isLoading) 360f else 0f,
        animationSpec = tween(600),
        label = "refresh_rotate"
    )

    fun refresh() {
        scope.launch {
            isLoading = true
            errorMessage = null
            val result = transactionRepository.getTransactions()
            isLoading = false
            result.onSuccess {
                transactions.clear()
                transactions.addAll(it)
            }.onFailure { errorMessage = it.message }
        }
    }

    LaunchedEffect(Unit) { refresh() }

    // Summary computed values
    val totalIncome  = transactions.filter { isIncome(it.type) }.sumOf { it.amount }
    val totalExpense = transactions.filter { !isIncome(it.type) }.sumOf { it.amount }
    val netBalance   = totalIncome - totalExpense

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BgDeep
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────
            item {
                HomeHeader(
                    isLoading = isLoading,
                    rotateAnim = rotateAnim,
                    onRefresh = { refresh() }
                )
            }

            // ── Balance Card ──────────────────────────────────────────────
            item {
                AnimatedContent(
                    targetState = isLoading,
                    transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(200)) },
                    label = "balance_anim"
                ) { loading ->
                    if (loading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = AccentGreen,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    } else {
                        BalanceCard(
                            netBalance = netBalance,
                            totalIncome = totalIncome,
                            totalExpense = totalExpense
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // ── Error State ───────────────────────────────────────────────
            if (!errorMessage.isNullOrBlank()) {
                item {
                    ErrorBanner(message = errorMessage.orEmpty())
                    Spacer(Modifier.height(16.dp))
                }
            }

            // ── Section Header ────────────────────────────────────────────
            if (!isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Riwayat Transaksi",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecond,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "${transactions.size} transaksi",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            // ── Empty State ───────────────────────────────────────────────
            if (!isLoading && transactions.isEmpty() && errorMessage.isNullOrBlank()) {
                item { EmptyState() }
            }

            // ── Transaction Items ─────────────────────────────────────────
            itemsIndexed(transactions) { index, trx ->
                TransactionRow(
                    trx = trx,
                    index = index,
                    isLast = index == transactions.lastIndex
                )
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

// ─── Header Component ─────────────────────────────────────────────────────────

@Composable
private fun HomeHeader(
    isLoading: Boolean,
    rotateAnim: Float,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Finance",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                letterSpacing = (-0.5).sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Freedom",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentGreen,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Rounded.TrendingUp,
                    contentDescription = null,
                    tint = AccentGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(BgCard)
                .border(1.dp, DividerCol, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onRefresh,
                enabled = !isLoading,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Refresh",
                    tint = if (isLoading) TextMuted else AccentGreen,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(rotateAnim)
                )
            }
        }
    }
}

// ─── Balance Card ────────────────────────────────────────────────────────────

@Composable
private fun BalanceCard(
    netBalance: Double,
    totalIncome: Double,
    totalExpense: Double
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(BgCard)
            .border(
                width = 1.dp,
                color = DividerCol,
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "SALDO BERSIH",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted,
                letterSpacing = 1.5.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = formatRupiah(netBalance),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (netBalance >= 0) AccentGreen else AccentRed,
                letterSpacing = (-0.5).sp
            )

            Spacer(Modifier.height(20.dp))
            Divider(color = DividerCol, thickness = 1.dp)
            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BalanceStat(
                    label = "Pemasukan",
                    amount = totalIncome,
                    isIncome = true
                )
                // Vertical divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(DividerCol)
                )
                BalanceStat(
                    label = "Pengeluaran",
                    amount = totalExpense,
                    isIncome = false
                )
            }
        }
    }
}

@Composable
private fun BalanceStat(
    label: String,
    amount: Double,
    isIncome: Boolean
) {
    val accentColor = if (isIncome) AccentGreen else AccentRed
    val icon = if (isIncome) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward

    Column(horizontalAlignment = if (isIncome) Alignment.Start else Alignment.End) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (!isIncome) {
                // Spacer for right-aligned
            }
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(10.dp)
                )
            }
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextMuted,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.3.sp
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = formatRupiah(amount),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
    }
}

// ─── Transaction Row ──────────────────────────────────────────────────────────

@Composable
private fun TransactionRow(
    trx: TransactionItem,
    index: Int,
    isLast: Boolean
) {
    val income = isIncome(trx.type)
    val accentColor = if (income) AccentGreen else AccentRed
    val initial = trx.title.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

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
            // Icon + Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Avatar circle
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                        .border(
                            1.dp,
                            accentColor.copy(alpha = 0.25f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        text = trx.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CategoryChip(text = trx.category)
                        Text(
                            text = "·",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                        Text(
                            text = trx.date,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            // Amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (income) "+" else "-"}${formatRupiah(trx.amount)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = trx.type,
                    fontSize = 10.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (!isLast) {
            Divider(
                color = DividerCol,
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = 54.dp)
            )
        }
    }
}

// ─── Category Chip ────────────────────────────────────────────────────────────

@Composable
private fun CategoryChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(BgCardAlt)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            color = TextSecond,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─── Error Banner ────────────────────────────────────────────────────────────

@Composable
private fun ErrorBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AccentRed.copy(alpha = 0.1f))
            .border(1.dp, AccentRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(AccentRed)
        )
        Text(
            text = message,
            fontSize = 13.sp,
            color = AccentRed,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─── Empty State ─────────────────────────────────────────────────────────────

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "", fontSize = 40.sp)
        Text(
            text = "Belum ada transaksi",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecond
        )
        Text(
            text = "Transaksi kamu akan tampil di sini",
            fontSize = 13.sp,
            color = TextMuted
        )
    }
}