package com.example.financefreedom.domain.model

data class UserProfile(
    val id: String?,
    val email: String
)

data class TransactionItem(
    val id: String?,
    val title: String,
    val amount: Double,
    val type: String,
    val category: String,
    val date: String,
    val note: String?
)

data class MonthlySummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double
)

data class TransactionCategories(
    val income: List<String>,
    val expense: List<String>
)

data class SavingsGoal(
    val id: String,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadline: String,
    val autoSaveDay: Int,
    val monthlyAmount: Double
)

data class ReminderItem(
    val id: String,
    val title: String,
    val amount: Double,
    val type: String,
    val dueDate: String,
    val isPaid: Boolean,
    val repeatInterval: String?,
    val userId: String?,
    val createdAt: String?
)

