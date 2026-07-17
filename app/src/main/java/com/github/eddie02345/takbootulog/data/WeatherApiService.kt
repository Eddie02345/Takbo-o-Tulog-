package com.github.eddie02345.takbootulog.data

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getHourlyForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("hourly") hourlyMetrics: String = "temperature_2m,apparent_temperature,relative_humidity_2m,precipitation_probability,precipitation,weather_code,wind_speed_10m,cloud_cover,uv_index",
        @Query("timezone") timezone: String = "Asia/Manila"
    ): WeatherResponseDto
    companion object {
        const val BASE_URL = "https://api.open-meteo.com/"
    }
}