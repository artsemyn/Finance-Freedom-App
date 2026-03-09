package com.example.financefreedom.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.GeneralSecurityException

class TokenManager(context: Context) {
    private val preferences: SharedPreferences? = createEncryptedPreferences(context)

    fun saveToken(token: String) {
        preferences?.edit()?.putString(KEY_ACCESS_TOKEN, token)?.apply()
    }

    fun getToken(): String? = preferences?.getString(KEY_ACCESS_TOKEN, null)

    fun clearToken() {
        preferences?.edit()?.remove(KEY_ACCESS_TOKEN)?.apply()
    }

    companion object {
        private const val PREF_NAME = "finance_freedom_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
    }

    private fun createEncryptedPreferences(context: Context): SharedPreferences? {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: GeneralSecurityException) {
            null
        } catch (_: IllegalStateException) {
            null
        }
    }
}

