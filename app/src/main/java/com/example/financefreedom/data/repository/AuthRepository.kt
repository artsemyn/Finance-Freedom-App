package com.example.financefreedom.data.repository

import com.example.financefreedom.domain.model.UserProfile

interface AuthRepository {
    suspend fun register(email: String, password: String): Result<UserProfile>
    suspend fun login(email: String, password: String): Result<UserProfile>
    suspend fun me(): Result<UserProfile>
    fun isLoggedIn(): Boolean
    fun logout()
}

