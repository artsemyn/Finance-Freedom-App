package com.example.financefreedom.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface FinanceApiService {
    @POST("auth/register")
    suspend fun register(@Body request: AuthRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @GET("auth/me")
    suspend fun me(): UserDto

    @GET("transactions")
    suspend fun getTransactions(): List<TransactionDto>

    @POST("transactions")
    suspend fun createTransaction(@Body request: CreateTransactionRequest): TransactionDto

    @GET("transactions/summary")
    suspend fun getSummary(@Query("month") month: String): SummaryDto
}

data class AuthRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String?,
    val user: UserDto? = null
)

data class UserDto(
    val id: String? = null,
    val email: String? = null
)

data class TransactionDto(
    val id: String? = null,
    val title: String = "",
    val amount: Double = 0.0,
    val type: String = "",
    val category: String = "",
    val date: String = "",
    val note: String? = null
)

data class CreateTransactionRequest(
    val title: String,
    val amount: Double,
    val type: String,
    val category: String,
    val date: String,
    val note: String
)

data class SummaryDto(
    val totalIncome: Double? = null,
    val totalExpense: Double? = null,
    val balance: Double? = null,
    val income: Double? = null,
    val expense: Double? = null
)

