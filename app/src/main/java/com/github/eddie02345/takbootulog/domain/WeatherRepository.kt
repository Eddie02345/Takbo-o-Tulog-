package com.github.eddie02345.takbootulog.domain

interface WeatherRepository {
    suspend fun getCurrentForecast(lat: Double, lon: Double): Result<HourlyForecast>
}