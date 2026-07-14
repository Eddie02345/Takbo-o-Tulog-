package com.github.eddie02345.takbootulog.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory 

object NetworkModule {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(WeatherApiService.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: WeatherApiService = retrofit.create(WeatherApiService::class.java)

    val repository: com.github.eddie02345.takbootulog.domain.WeatherRepository by lazy {
        WeatherRepositoryImpl(apiService)
    }
}