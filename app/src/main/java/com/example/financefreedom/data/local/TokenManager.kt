package com.example.financefreedom.data.local

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        preferences.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getToken(): String? = preferences.getString(KEY_ACCESS_TOKEN, null)

    fun clearToken() {
        preferences.edit().remove(KEY_ACCESS_TOKEN).apply()
    }

    companion object {
        private const val PREF_NAME = "finance_freedom_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
    }
}

