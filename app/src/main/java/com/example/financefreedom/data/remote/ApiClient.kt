package com.example.financefreedom.data.remote

import com.example.financefreedom.BuildConfig
import com.example.financefreedom.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    fun getApiService(sessionManager: SessionManager): FinanceApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val authInterceptor = Interceptor { chain ->
            val currentToken = sessionManager.getToken()
            val request = chain.request().newBuilder().apply {
                if (!currentToken.isNullOrBlank()) {
                    addHeader("Authorization", "Bearer $currentToken")
                }
            }.build()
            val response = chain.proceed(request)
            if (response.code == 401) {
                sessionManager.clear()
            }
            response
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(FinanceApiService::class.java)
    }
}
