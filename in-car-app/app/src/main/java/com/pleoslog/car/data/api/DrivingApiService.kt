package com.pleoslog.car.data.api

import com.pleoslog.car.data.model.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface DrivingApiService {

    @POST("/api/v1/sessions/start")
    suspend fun startSession(
        @Body request: StartSessionRequest
    ): Response<ApiResponse<DrivingSessionResponse>>

    @POST("/api/v1/sessions/{sessionId}/routes")
    suspend fun recordRoutes(
        @Path("sessionId") sessionId: Long,
        @Body request: BatchRouteRequest
    ): Response<ApiResponse<String>>

    @POST("/api/v1/sessions/{sessionId}/complete")
    suspend fun completeSession(
        @Path("sessionId") sessionId: Long,
        @Body request: CompleteSessionRequest
    ): Response<ApiResponse<DrivingSessionResponse>>
}

object ApiClient {
    // 10.0.2.2 is Android Emulator's alias to host machine's 127.0.0.1
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    val apiService: DrivingApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DrivingApiService::class.java)
    }
}
