package com.github.eddie02345.takbootulog.data

import android.content.Context
import android.location.Geocoder
import com.github.eddie02345.takbootulog.domain.HourlyForecast
import com.github.eddie02345.takbootulog.domain.WeatherRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class WeatherRepositoryImpl(
    private val apiService: WeatherApiService
) : WeatherRepository {

    override suspend fun getCurrentForecast(context: Context, lat: Double, lon: Double): Result<Pair<HourlyForecast, String>> {
        return try {
            // 1. Fetch the weather data from the API
            val response = apiService.getHourlyForecast(lat = lat, lon = lon)
            val hourlyData = response.hourly
            val targetIndex = findCurrentHourIndex(hourlyData.time)

            val forecast = HourlyForecast(
                temperature = hourlyData.temperatures[targetIndex],
                rainProbability = hourlyData.rainProbabilities[targetIndex],
                rainVolume = hourlyData.rainVolumes[targetIndex],
                uvIndex = hourlyData.uvIndices[targetIndex],
                relativeHumidity = hourlyData.humidities[targetIndex]
            )

            // 2. Reverse geocode the coordinates to get the real City/Town name
            val cityName = try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                // Grab the locality (City/Town). Fallback to adminArea (Province) if null.
                addresses?.firstOrNull()?.locality ?: addresses?.firstOrNull()?.adminArea ?: "Unknown Location"
            } catch (e: Exception) {
                "Gps Location" // Fallback string if geocoder network fails
            }

            Result.success(Pair(forecast, cityName.uppercase()))
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
            } catch (e: Exception) { }
        }
        return closestIndex
    }
}