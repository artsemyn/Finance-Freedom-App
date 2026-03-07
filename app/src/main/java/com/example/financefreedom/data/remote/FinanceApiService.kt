package com.example.financefreedom.data.remote

import com.google.gson.JsonElement
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
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

    @GET("transactions/categories")
    suspend fun getTransactionCategories(): JsonElement

    @POST("transactions")
    suspend fun createTransaction(@Body request: CreateTransactionRequest): TransactionDto

    @GET("transactions/summary")
    suspend fun getSummary(@Query("month") month: String): SummaryDto

    @GET("savings")
    suspend fun getSavingsGoals(): List<SavingsGoalDto>

    @POST("savings")
    suspend fun createSavingsGoal(@Body request: CreateSavingsGoalRequest): SavingsGoalDto

    @PUT("savings/{id}/progress")
    suspend fun updateSavingsProgress(
        @Path("id") id: String,
        @Body request: UpdateSavingsProgressRequest
    ): SavingsGoalDto

    @DELETE("savings/{id}")
    suspend fun deleteSavingsGoal(@Path("id") id: String)

    @GET("reminders")
    suspend fun getReminders(): List<ReminderDto>

    @GET("reminders/upcoming")
    suspend fun getUpcomingReminders(): List<ReminderDto>

    @POST("reminders")
    suspend fun createReminder(@Body request: CreateReminderRequest): ReminderDto

    @PUT("reminders/{id}/paid")
    suspend fun markReminderPaid(@Path("id") id: String): ReminderDto

    @DELETE("reminders/{id}")
    suspend fun deleteReminder(@Path("id") id: String)
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

data class SavingsGoalDto(
    val id: String? = null,
    val title: String = "",
    val targetAmount: Double? = null,
    val currentAmount: Double? = null,
    val deadline: String? = null,
    val autoSaveDay: Int? = null,
    val monthlyAmount: Double? = null
)

data class CreateSavingsGoalRequest(
    val title: String,
    val targetAmount: Double,
    val deadline: String?,
    val autoSaveDay: Int,
    val monthlyAmount: Double
)

data class UpdateSavingsProgressRequest(
    val amount: Double
)

data class ReminderDto(
    val id: String? = null,
    val title: String = "",
    val amount: Double? = null,
    val type: String = "",
    val dueDate: String = "",
    val repeatInterval: String? = null,
    val isPaid: Boolean? = null,
    val paid: Boolean? = null,
    val userId: String? = null,
    val createdAt: String? = null
)

data class CreateReminderRequest(
    val title: String,
    val amount: Double,
    val type: String,
    val dueDate: String,
    val repeatInterval: String
)

