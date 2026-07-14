package com.github.eddie02345.takbootulog.domain
import android.content.Context

interface WeatherRepository {
    suspend fun getCurrentForecast(
        context: Context,
        lat: Double,
        lon: Double
    ): Result<Pair<HourlyForecast, String>>
}