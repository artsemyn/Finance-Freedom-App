package com.example.financefreedom.data.repository

import com.example.financefreedom.data.local.SessionManager
import com.example.financefreedom.data.local.TokenManager
import com.example.financefreedom.data.remote.AuthRequest
import com.example.financefreedom.data.remote.FinanceApiService
import com.example.financefreedom.domain.model.UserProfile
import retrofit2.HttpException

class AuthRepositoryImpl(
    private val apiService: FinanceApiService,
    private val tokenManager: TokenManager,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun register(email: String, password: String): Result<UserProfile> {
        return runCatching {
            val response = apiService.register(AuthRequest(email = email, password = password))
            val token = response.token.orEmpty()
            if (token.isBlank()) {
                throw IllegalStateException("Token kosong dari server.")
            }
            tokenManager.saveToken(token)
            sessionManager.updateToken(token)
            val user = response.user
            UserProfile(
                id = user?.id,
                email = user?.email ?: email
            )
        }.mapError()
    }

    override suspend fun login(email: String, password: String): Result<UserProfile> {
        return runCatching {
            val response = apiService.login(AuthRequest(email = email, password = password))
            val token = response.token.orEmpty()
            if (token.isBlank()) {
                throw IllegalStateException("Token kosong dari server.")
            }
            tokenManager.saveToken(token)
            sessionManager.updateToken(token)
            val user = response.user
            UserProfile(
                id = user?.id,
                email = user?.email ?: email
            )
        }.mapError()
    }

    override suspend fun me(): Result<UserProfile> {
        return runCatching {
            val user = apiService.me()
            UserProfile(
                id = user.id,
                email = user.email.orEmpty()
            )
        }.mapError()
    }

    override fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()

    override fun logout() {
        tokenManager.clearToken()
        sessionManager.clear()
    }

    private fun <T> Result<T>.mapError(): Result<T> {
        val throwable = exceptionOrNull() ?: return this
        val mapped = when (throwable) {
            is HttpException -> {
                if (throwable.code() == 401) {
                    IllegalStateException("Sesi berakhir. Silakan login kembali.")
                } else {
                    IllegalStateException("HTTP ${throwable.code()}: ${throwable.message()}")
                }
            }
            else -> IllegalStateException(throwable.message ?: "Terjadi kesalahan tak terduga.")
        }
        return Result.failure(mapped)
    }
}
