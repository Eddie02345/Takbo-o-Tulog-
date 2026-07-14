package com.github.eddie02345.takbootulog.data

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getHourlyForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("hourly") hourlyMetrics: String = "temperature_2m,relative_humidity_2m,precipitation_probability,precipitation,uv_index",
        @Query("timezone") timezone: String = "Asia/Manila"
    ): WeatherResponseDto
    companion object {
        const val BASE_URL = "https://api.open-meteo.com/"
    }
}