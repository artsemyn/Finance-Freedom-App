package com.example.financefreedom.data.repository

import com.example.financefreedom.data.remote.CreateTransactionRequest
import com.example.financefreedom.data.remote.FinanceApiService
import com.example.financefreedom.domain.model.MonthlySummary
import com.example.financefreedom.domain.model.TransactionItem
import retrofit2.HttpException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TransactionRepositoryImpl(
    private val apiService: FinanceApiService
) : TransactionRepository {

    override suspend fun getTransactions(): Result<List<TransactionItem>> {
        return runCatching {
            apiService.getTransactions().map { dto ->
                TransactionItem(
                    id = dto.id,
                    title = dto.title,
                    amount = dto.amount,
                    type = dto.type,
                    category = dto.category,
                    date = dto.date,
                    note = dto.note
                )
            }
        }.mapError()
    }

    override suspend fun createTransaction(
        title: String,
        amount: Double,
        type: String,
        category: String,
        date: String,
        note: String
    ): Result<TransactionItem> {
        return runCatching {
            val request = CreateTransactionRequest(
                title = title.trim(),
                amount = amount,
                type = type,
                category = category,
                date = date,
                note = note.ifBlank { "" }
            )
            val dto = apiService.createTransaction(request)
            TransactionItem(
                id = dto.id,
                title = dto.title,
                amount = dto.amount,
                type = dto.type,
                category = dto.category,
                date = dto.date,
                note = dto.note
            )
        }.mapError()
    }

    override suspend fun getMonthlySummary(month: String): Result<MonthlySummary> {
        return runCatching {
            val summary = apiService.getSummary(month)
            val totalIncome = summary.totalIncome ?: summary.income ?: 0.0
            val totalExpense = summary.totalExpense ?: summary.expense ?: 0.0
            val balance = summary.balance ?: (totalIncome - totalExpense)
            MonthlySummary(
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                balance = balance
            )
        }.mapError()
    }

    fun currentMonth(): String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))

    private fun <T> Result<T>.mapError(): Result<T> {
        val throwable = exceptionOrNull() ?: return this
        val mapped = when (throwable) {
            is HttpException -> {
                if (throwable.code() == 401) {
                    IllegalStateException("Sesi berakhir. Silakan login kembali.")
                } else {
                    val backendMessage = parseBackendError(throwable)
                    IllegalStateException(backendMessage)
                }
            }
            else -> IllegalStateException(throwable.message ?: "Terjadi kesalahan tak terduga.")
        }
        return Result.failure(mapped)
    }

    /** Extracts backend error message from response body (format: { "error": "message" }). */
    private fun parseBackendError(e: HttpException): String {
        val body = e.response()?.errorBody()?.string() ?: return "HTTP ${e.code()}: ${e.message()}"
        val regex = """"error"\s*:\s*"([^"]*)"""".toRegex()
        val match = regex.find(body)
        return match?.groupValues?.getOrNull(1)?.takeIf { it.isNotBlank() }
            ?: "HTTP ${e.code()}: ${e.message()}"
    }
}
