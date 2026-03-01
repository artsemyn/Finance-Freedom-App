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

