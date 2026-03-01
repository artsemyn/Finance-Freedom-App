package com.example.financefreedom.data.repository

import com.example.financefreedom.domain.model.MonthlySummary
import com.example.financefreedom.domain.model.TransactionItem

interface TransactionRepository {
    suspend fun getTransactions(): Result<List<TransactionItem>>
    /** Creates a transaction with the given form data. Use this for the Add screen. */
    suspend fun createTransaction(
        title: String,
        amount: Double,
        type: String,
        category: String,
        date: String,
        note: String
    ): Result<TransactionItem>
    suspend fun getMonthlySummary(month: String): Result<MonthlySummary>
}

