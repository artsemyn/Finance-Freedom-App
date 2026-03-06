package com.example.financefreedom.ui.home

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financefreedom.data.repository.SavingsRepository
import com.example.financefreedom.data.repository.TransactionRepository
import com.example.financefreedom.domain.model.MonthlySummary
import com.example.financefreedom.domain.model.SavingsGoal
import com.example.financefreedom.domain.model.TransactionItem
import com.example.financefreedom.ui.theme.FinanceFreedomTheme
import com.example.financefreedom.ui.theme.financeCardGradient
import com.example.financefreedom.ui.theme.financeUiColors
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    transactionRepository: TransactionRepository,
    savingsRepository: SavingsRepository,
    onOpenSavings: () -> Unit,
    onOpenAdd: () -> Unit
) {
    val ui = financeUiColors()
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            transactionRepository = transactionRepository,
            savingsRepository = savingsRepository
        )
    )
    val state by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ui.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Header(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { viewModel.refresh() }
                )
            }

            item {
                BalanceCard(
                    netBalance = state.netBalance,
                    totalIncome = state.totalIncome,
                    totalExpense = state.totalExpense
                )
            }

            item {
                SavingsOverviewCard(
                    savingsGoals = state.savingsGoals,
                    totalCurrent = state.totalSavingsCurrent,
                    totalTarget = state.totalSavingsTarget,
                    progress = state.overallSavingsProgress,
                    onOpenSavings = onOpenSavings
                )
            }
            item {
                QuickActionsCard(onOpenAdd = onOpenAdd)
            }

            if (!state.errorMessage.isNullOrBlank()) {
                item {
                    MessageBanner(
                        message = state.errorMessage.orEmpty(),
                        isError = true
                    )
                }
            }

            when {
                state.isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = ui.positive,
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }

                state.transactions.isEmpty() -> {
                    item {
                        MessageBanner(
                            message = "Belum ada transaksi. Tambahkan data lewat tombol tambah di Home.",
                            isError = false
                        )
                    }
                }

                else -> {
                    item {
                        Text(
                            text = "Transaksi Terbaru",
                            modifier = Modifier.padding(horizontal = 20.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ui.secondaryText
                        )
                    }
                    items(state.transactions.take(6)) { transaction ->
                        TransactionRow(transaction = transaction)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun QuickActionsCard(onOpenAdd: () -> Unit) {
    val ui = financeUiColors()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(ui.surface)
            .border(1.dp, ui.outline, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Quick Action", fontWeight = FontWeight.SemiBold, color = ui.primaryText)
                Text("Catat transaksi baru", fontSize = 12.sp, color = ui.secondaryText)
            }
            Button(onClick = onOpenAdd) {
                Text("Tambah transaksi")
            }
        }
    }
}

@Composable
private fun Header(
    isRefreshing: Boolean,
    onRefresh: () -> Unit
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
                text = "Finance Freedom",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ui.primaryText
            )
            Text(
                text = "Ringkasan keuangan dan tabungan",
                fontSize = 13.sp,
                color = ui.secondaryText
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
            IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                Icon(
                    imageVector = if (isRefreshing) Icons.AutoMirrored.Rounded.TrendingUp else Icons.Rounded.Refresh,
                    contentDescription = "Refresh",
                    tint = if (isRefreshing) ui.mutedText else ui.positive
                )
            }
        }
    }
}

@Composable
private fun BalanceCard(
    netBalance: Double,
    totalIncome: Double,
    totalExpense: Double
) {
    val ui = financeUiColors()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(financeCardGradient())
            .border(1.dp, ui.outline, RoundedCornerShape(24.dp))
            .padding(22.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "Saldo Bersih", fontSize = 12.sp, color = ui.mutedText)
            Text(
                text = formatRupiah(netBalance),
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (netBalance >= 0) ui.accent else ui.negative
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Masuk: ${formatRupiah(totalIncome)}",
                    fontSize = 12.sp,
                    color = ui.positive
                )
                Text(
                    text = "Keluar: ${formatRupiah(totalExpense)}",
                    fontSize = 12.sp,
                    color = ui.negative
                )
            }
        }
    }
}

@Composable
private fun SavingsOverviewCard(
    savingsGoals: List<SavingsGoal>,
    totalCurrent: Double,
    totalTarget: Double,
    progress: Float,
    onOpenSavings: () -> Unit
) {
    val ui = financeUiColors()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(ui.surface)
            .border(1.dp, ui.outline, RoundedCornerShape(24.dp))
            .clickable(onClick = onOpenSavings)
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Savings,
                        contentDescription = null,
                        tint = ui.positive
                    )
                    Text(
                        text = "Savings Goals",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ui.primaryText
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = "Open savings",
                    tint = ui.mutedText
                )
            }

            Text(
                text = if (savingsGoals.isEmpty()) {
                    "Belum ada target tabungan"
                } else {
                    "${savingsGoals.size} target aktif"
                },
                fontSize = 12.sp,
                color = ui.secondaryText
            )
            Text(
                text = "${formatRupiah(totalCurrent)} / ${formatRupiah(totalTarget)}",
                fontSize = 13.sp,
                color = ui.primaryText,
                fontWeight = FontWeight.Medium
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(ui.surfaceAlt)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(ui.positive)
                )
            }

            Text(
                text = "Progress total: ${(progress * 100).toInt()}%",
                fontSize = 12.sp,
                color = ui.mutedText
            )
        }
    }
}

@Composable
private fun TransactionRow(transaction: TransactionItem) {
    val ui = financeUiColors()
    val income = transaction.type.lowercase() in listOf("income", "pemasukan", "kredit", "credit")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(ui.surface)
            .border(1.dp, ui.outline, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title.ifBlank { "Tanpa judul" },
                fontWeight = FontWeight.SemiBold,
                color = ui.primaryText,
                fontSize = 14.sp
            )
            Text(
                text = "${transaction.category} • ${transaction.date}",
                color = ui.secondaryText,
                fontSize = 12.sp
            )
        }
        Text(
            text = (if (income) "+ " else "- ") + formatRupiah(transaction.amount),
            color = if (income) ui.positive else ui.negative,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun MessageBanner(message: String, isError: Boolean) {
    val ui = financeUiColors()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isError) ui.negative.copy(alpha = 0.14f) else ui.surface)
            .border(1.dp, ui.outline, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = message,
            color = if (isError) ui.negative else ui.secondaryText,
            fontSize = 12.sp
        )
    }
}

private fun formatRupiah(amount: Double): String {
    val fmt = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
    return "Rp ${fmt.format(amount)}"
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    val fakeTransactionRepository = object : TransactionRepository {
        override suspend fun getTransactions(): Result<List<TransactionItem>> {
            return Result.success(
                listOf(
                    TransactionItem(
                        id = "1",
                        title = "Gaji",
                        amount = 7000000.0,
                        type = "income",
                        category = "Salary",
                        date = "2026-03-01",
                        note = null
                    ),
                    TransactionItem(
                        id = "2",
                        title = "Belanja Bulanan",
                        amount = 1200000.0,
                        type = "expense",
                        category = "Groceries",
                        date = "2026-03-03",
                        note = null
                    )
                )
            )
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
            return Result.success(MonthlySummary(0.0, 0.0, 0.0))
        }
    }

    val fakeSavingsRepository = object : SavingsRepository {
        override suspend fun getSavingsGoals(): Result<List<SavingsGoal>> {
            return Result.success(
                listOf(
                    SavingsGoal(
                        id = "sv1",
                        title = "Dana Darurat",
                        targetAmount = 10000000.0,
                        currentAmount = 3200000.0,
                        deadline = "2026-12-31",
                        autoSaveDay = 25,
                        monthlyAmount = 500000.0
                    )
                )
            )
        }

        override suspend fun createSavingsGoal(
            title: String,
            targetAmount: Double,
            deadline: String?,
            autoSaveDay: Int,
            monthlyAmount: Double
        ): Result<SavingsGoal> = Result.failure(UnsupportedOperationException())

        override suspend fun addSavingsProgress(goalId: String, amount: Double): Result<SavingsGoal> {
            return Result.failure(UnsupportedOperationException())
        }

        override suspend fun deleteSavingsGoal(goalId: String): Result<Unit> {
            return Result.failure(UnsupportedOperationException())
        }
    }

    FinanceFreedomTheme {
        HomeScreen(
            transactionRepository = fakeTransactionRepository,
            savingsRepository = fakeSavingsRepository,
            onOpenSavings = {},
            onOpenAdd = {}
        )
    }
}
