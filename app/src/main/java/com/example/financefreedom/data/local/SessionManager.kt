package com.example.financefreedom.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager {
    @Volatile
    private var accessToken: String? = null
    private val _isLoggedInFlow = MutableStateFlow(false)
    val isLoggedInFlow: StateFlow<Boolean> = _isLoggedInFlow.asStateFlow()

    fun updateToken(token: String?) {
        accessToken = token
        _isLoggedInFlow.value = !token.isNullOrBlank()
    }

    fun getToken(): String? = accessToken

    fun isLoggedIn(): Boolean = !accessToken.isNullOrBlank()

    fun clear() {
        accessToken = null
        _isLoggedInFlow.value = false
    }
}
