package com.example.financefreedom.data.repository

import com.example.financefreedom.data.local.TransactionCategoryCacheManager
import com.example.financefreedom.data.remote.CreateTransactionRequest
import com.example.financefreedom.data.remote.FinanceApiService
import com.example.financefreedom.domain.model.MonthlySummary
import com.example.financefreedom.domain.model.TransactionCategories
import com.example.financefreedom.domain.model.TransactionItem
import com.example.financefreedom.utils.CrashLogger
import com.google.gson.JsonElement
import retrofit2.HttpException
import java.io.IOException
import java.net.UnknownHostException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TransactionRepositoryImpl(
    private val apiService: FinanceApiService,
    private val categoryCacheManager: TransactionCategoryCacheManager
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

    override suspend fun getTransactionCategories(forceRefresh: Boolean): Result<TransactionCategories> {
        val cached = categoryCacheManager.get()
        if (!forceRefresh && cached != null) {
            return Result.success(cached)
        }

        return runCatching {
            val parsed = parseTransactionCategories(apiService.getTransactionCategories())
            if (parsed.income.isEmpty() && parsed.expense.isEmpty()) {
                throw IllegalStateException("Kategori transaksi kosong dari server.")
            }
            categoryCacheManager.save(parsed)
            parsed
        }.recoverCatching {
            cached ?: throw it
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

    private fun parseTransactionCategories(payload: JsonElement): TransactionCategories {
        val income = linkedSetOf<String>()
        val expense = linkedSetOf<String>()

        if (payload.isJsonArray) {
            payload.asJsonArray.forEach { item ->
                if (item.isJsonPrimitive) {
                    item.asString
                        .trim()
                        .takeIf { it.isNotBlank() }
                        ?.let(expense::add)
                } else if (item.isJsonObject) {
                    val obj = item.asJsonObject
                    val name = obj.getString("name")
                        ?: obj.getString("category")
                        ?: obj.getString("label")
                    val type = obj.getString("type")
                        ?.lowercase()
                        ?.trim()
                    name?.takeIf { it.isNotBlank() }?.let {
                        when (type) {
                            "income" -> income.add(it)
                            "expense" -> expense.add(it)
                            else -> expense.add(it)
                        }
                    }
                }
            }
        } else if (payload.isJsonObject) {
            val root = payload.asJsonObject
            parseTypedArray(root.get("income"), income)
            parseTypedArray(root.get("expense"), expense)
            parseArrayObjects(root.get("categories"), income, expense)
        }

        return TransactionCategories(
            income = income.toList(),
            expense = expense.toList()
        )
    }

    private fun parseTypedArray(source: JsonElement?, target: MutableSet<String>) {
        if (source == null || !source.isJsonArray) return
        source.asJsonArray.forEach { item ->
            when {
                item.isJsonPrimitive -> {
                    item.asString.trim().takeIf { it.isNotBlank() }?.let(target::add)
                }
                item.isJsonObject -> {
                    item.asJsonObject.getString("name")
                        ?.takeIf { it.isNotBlank() }
                        ?.let(target::add)
                }
            }
        }
    }

    private fun parseArrayObjects(
        source: JsonElement?,
        income: MutableSet<String>,
        expense: MutableSet<String>
    ) {
        if (source == null || !source.isJsonArray) return
        source.asJsonArray.forEach { item ->
            if (!item.isJsonObject) return@forEach
            val obj = item.asJsonObject
            val name = obj.getString("name")
                ?: obj.getString("category")
                ?: obj.getString("label")
            val type = obj.getString("type")?.lowercase()?.trim()
            name?.takeIf { it.isNotBlank() }?.let {
                when (type) {
                    "income" -> income.add(it)
                    else -> expense.add(it)
                }
            }
        }
    }

    private fun com.google.gson.JsonObject.getString(key: String): String? {
        val value = get(key) ?: return null
        if (!value.isJsonPrimitive) return null
        return value.asString?.trim()
    }

    private fun <T> Result<T>.mapError(): Result<T> {
        val throwable = exceptionOrNull() ?: return this
        val mapped = when (throwable) {
            is UnknownHostException -> {
                IllegalStateException("Host backend tidak dapat dijangkau. Periksa koneksi internet atau konfigurasi DNS perangkat.")
            }
            is IOException -> {
                IllegalStateException("Koneksi jaringan gagal. Periksa internet Anda lalu coba lagi.")
            }
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
        CrashLogger.logException(mapped, "transaction_repository_error")
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
