package com.github.eddie02345.takbootulog.data

import com.github.eddie02345.takbootulog.domain.HourlyForecast
import com.github.eddie02345.takbootulog.domain.WeatherRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class WeatherRepositoryImpl(
    private val apiService: WeatherApiService
) : WeatherRepository {

    override suspend fun getCurrentForecast(lat: Double, lon: Double): Result<HourlyForecast> {
        return try {
            val response = apiService.getHourlyForecast(lat = lat, lon = lon)
            val hourlyData = response.hourly

            // Find the index in the API array that matches the current hour
            val targetIndex = findCurrentHourIndex(hourlyData.time)

            val forecast = HourlyForecast(
                temperature = hourlyData.temperatures[targetIndex],
                rainProbability = hourlyData.rainProbabilities[targetIndex],
                rainVolume = hourlyData.rainVolumes[targetIndex],
                uvIndex = hourlyData.uvIndices[targetIndex],
                relativeHumidity = hourlyData.humidities[targetIndex]
            )

            Result.success(forecast)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun findCurrentHourIndex(timeStrings: List<String>): Int {
        if (timeStrings.isEmpty()) return 0

        val now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS)
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

        var closestIndex = 0
        var minDiff = Long.MAX_VALUE

        timeStrings.forEachIndexed { index, timeStr ->
            try {
                val forecastTime = LocalDateTime.parse(timeStr, formatter)
                val diff = Math.abs(ChronoUnit.HOURS.between(now, forecastTime))
                if (diff < minDiff) {
                    minDiff = diff
                    closestIndex = index
                }
            } catch (e: Exception) {
                // Fallback if parsing fails
            }
        }
        return closestIndex
    }
}