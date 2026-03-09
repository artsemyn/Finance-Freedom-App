package com.example.financefreedom.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import com.example.financefreedom.data.repository.ReminderRepository
import com.example.financefreedom.data.repository.SavingsRepository
import com.example.financefreedom.data.repository.TransactionRepository
import com.example.financefreedom.domain.model.MonthlySummary
import com.example.financefreedom.domain.model.ReminderItem
import com.example.financefreedom.domain.model.SavingsGoal
import com.example.financefreedom.domain.model.TransactionCategories
import com.example.financefreedom.domain.model.TransactionItem
import com.example.financefreedom.ui.components.FinanceAnimatedReveal
import com.example.financefreedom.ui.components.FinanceCardSurface
import com.example.financefreedom.ui.components.FinanceEmptyStateCard
import com.example.financefreedom.ui.components.FinanceLoadingCardSkeleton
import com.example.financefreedom.ui.components.FinanceMessageBanner
import com.example.financefreedom.ui.theme.FinanceFreedomTheme
import com.example.financefreedom.ui.theme.FinanceCorners
import com.example.financefreedom.ui.theme.FinanceSpacing
import com.example.financefreedom.ui.theme.financeCardGradient
import com.example.financefreedom.ui.theme.financeUiColors
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    transactionRepository: TransactionRepository,
    savingsRepository: SavingsRepository,
    reminderRepository: ReminderRepository,
    onOpenSavings: () -> Unit,
    onOpenAdd: () -> Unit,
    onOpenReminder: () -> Unit
) {
    val ui = financeUiColors()
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            transactionRepository = transactionRepository,
            savingsRepository = savingsRepository,
            reminderRepository = reminderRepository
        )
    )
    val state by viewModel.uiState.collectAsState()

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
                    Header(onOpenReminder = onOpenReminder)
                }

                item {
                    FinanceAnimatedReveal(index = 1) {
                        BalanceCard(
                            netBalance = state.netBalance,
                            totalIncome = state.totalIncome,
                            totalExpense = state.totalExpense
                        )
                    }
                }

                item {
                    FinanceAnimatedReveal(index = 2) {
                        SavingsOverviewCard(
                            savingsGoals = state.savingsGoals,
                            totalCurrent = state.totalSavingsCurrent,
                            totalTarget = state.totalSavingsTarget,
                            progress = state.overallSavingsProgress,
                            onOpenSavings = onOpenSavings
                        )
                    }
                }
                item {
                    FinanceAnimatedReveal(index = 3) {
                        UpcomingRemindersCard(
                            reminders = state.upcomingReminders,
                            onOpenReminder = onOpenReminder
                        )
                    }
                }
                item {
                    FinanceAnimatedReveal(index = 4) {
                        QuickActionsCard(onOpenAdd = onOpenAdd)
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

                when {
                    state.isLoading -> {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = FinanceSpacing.screenHorizontal),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FinanceLoadingCardSkeleton(modifier = Modifier.fillMaxWidth())
                                FinanceLoadingCardSkeleton(modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }

                    state.transactions.isEmpty() -> {
                        item {
                            FinanceEmptyStateCard(
                                title = "Belum Ada Transaksi",
                                description = "Tambahkan transaksi pertama Anda dari tombol tambah di Home.",
                                icon = Icons.Rounded.Payments,
                                modifier = Modifier.padding(horizontal = FinanceSpacing.screenHorizontal)
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
                                color = ui.secondaryTextReadable
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
}

@Composable
private fun QuickActionsCard(onOpenAdd: () -> Unit) {
    val ui = financeUiColors()
    FinanceCardSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = FinanceSpacing.screenHorizontal)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Quick Action", fontWeight = FontWeight.SemiBold, color = ui.primaryText)
                Text("Catat transaksi baru", fontSize = 12.sp, color = ui.secondaryTextReadable)
            }
            Button(onClick = onOpenAdd) {
                Text("Tambah transaksi")
            }
        }
    }
}

@Composable
private fun Header(onOpenReminder: () -> Unit) {
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
                color = ui.secondaryTextReadable
            )
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(ui.surface)
                .border(1.dp, ui.outline, CircleShape)
                .clickable(onClick = onOpenReminder),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Notifications,
                contentDescription = "Open reminders",
                tint = ui.positive
            )
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
    FinanceCardSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = FinanceSpacing.screenHorizontal),
        cornerRadius = FinanceCorners.cardLarge,
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(FinanceCorners.cardLarge))
                .background(financeCardGradient())
                .padding(FinanceSpacing.cardContent)
        ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "Saldo Bersih Bulan Ini", fontSize = 12.sp, color = ui.mutedTextReadable)
            Text(
                text = formatRupiah(netBalance),
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (netBalance >= 0) ui.accent else ui.negative
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Masuk Bulan Ini: ${formatRupiah(totalIncome)}",
                    fontSize = 12.sp,
                    color = ui.positiveText
                )
                Text(
                    text = "Keluar Bulan Ini: ${formatRupiah(totalExpense)}",
                    fontSize = 12.sp,
                    color = ui.negative
                )
            }
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
    FinanceCardSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = FinanceSpacing.screenHorizontal)
            .clickable(onClick = onOpenSavings),
        cornerRadius = FinanceCorners.cardLarge
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
                    tint = ui.mutedTextReadable
                )
            }

            Text(
                text = if (savingsGoals.isEmpty()) {
                    "Belum ada target tabungan"
                } else {
                    "${savingsGoals.size} target aktif"
                },
                fontSize = 12.sp,
                color = ui.secondaryTextReadable
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
                    .clip(RoundedCornerShape(FinanceCorners.pill))
                    .background(ui.surfaceAlt)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(10.dp)
                        .clip(RoundedCornerShape(FinanceCorners.pill))
                        .background(ui.positive)
                )
            }

            Text(
                text = "Progress total: ${(progress * 100).toInt()}%",
                fontSize = 12.sp,
                color = ui.mutedTextReadable
            )
        }
    }
}

@Composable
private fun UpcomingRemindersCard(
    reminders: List<ReminderItem>,
    onOpenReminder: () -> Unit
) {
    val ui = financeUiColors()
    FinanceCardSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = FinanceSpacing.screenHorizontal)
            .clickable(onClick = onOpenReminder),
        cornerRadius = FinanceCorners.cardLarge
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
                        imageVector = Icons.Rounded.Notifications,
                        contentDescription = null,
                        tint = ui.accent
                    )
                    Text(
                        text = "Upcoming Reminders",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ui.primaryText
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = "Open reminders",
                    tint = ui.mutedTextReadable
                )
            }

            if (reminders.isEmpty()) {
                Text(
                    text = "Tidak ada reminder mendatang.",
                    fontSize = 12.sp,
                    color = ui.secondaryTextReadable
                )
            } else {
                reminders.take(3).forEach { reminder ->
                    ReminderRow(reminder = reminder)
                }
            }
        }
    }
}

@Composable
private fun ReminderRow(reminder: ReminderItem) {
    val ui = financeUiColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ui.surfaceAlt)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = reminder.title.ifBlank { "Tanpa judul" },
                fontWeight = FontWeight.Medium,
                color = ui.primaryText,
                fontSize = 13.sp
            )
            Text(
                text = "Jatuh tempo: ${reminder.dueDate.take(10)}",
                fontSize = 12.sp,
                color = ui.secondaryTextReadable
            )
        }
        Text(
            text = formatRupiah(reminder.amount),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = ui.accent
        )
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
                color = ui.secondaryTextReadable,
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

        override suspend fun getTransactionCategories(forceRefresh: Boolean): Result<TransactionCategories> {
            return Result.success(
                TransactionCategories(
                    income = listOf("Gaji"),
                    expense = listOf("Makanan")
                )
            )
        }

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
    val fakeReminderRepository = object : ReminderRepository {
        override suspend fun getReminders(): Result<List<ReminderItem>> = Result.success(emptyList())

        override suspend fun getUpcomingReminders(): Result<List<ReminderItem>> {
            return Result.success(
                listOf(
                    ReminderItem(
                        id = "rm1",
                        title = "Bayar listrik",
                        amount = 350000.0,
                        type = "bill",
                        dueDate = "2026-03-10",
                        isPaid = false,
                        repeatInterval = null,
                        userId = null,
                        createdAt = null
                    ),
                    ReminderItem(
                        id = "rm2",
                        title = "Cicilan motor",
                        amount = 750000.0,
                        type = "installment",
                        dueDate = "2026-03-12",
                        isPaid = false,
                        repeatInterval = null,
                        userId = null,
                        createdAt = null
                    )
                )
            )
        }

        override suspend fun createReminder(
            title: String,
            amount: Double,
            type: String,
            dueDate: String,
            repeatInterval: String?
        ): Result<ReminderItem> = Result.failure(UnsupportedOperationException())

        override suspend fun markReminderPaid(reminderId: String): Result<ReminderItem> =
            Result.failure(UnsupportedOperationException())

        override suspend fun deleteReminder(reminderId: String): Result<Unit> =
            Result.failure(UnsupportedOperationException())

        override suspend fun syncReminderSchedules(): Result<Unit> = Result.success(Unit)
    }

    FinanceFreedomTheme {
        HomeScreen(
            transactionRepository = fakeTransactionRepository,
            savingsRepository = fakeSavingsRepository,
            reminderRepository = fakeReminderRepository,
            onOpenSavings = {},
            onOpenAdd = {},
            onOpenReminder = {}
        )
    }
}



