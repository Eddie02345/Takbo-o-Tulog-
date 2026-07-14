package com.github.eddie02345.takbootulog.domain

data class HourlyForecast(
    val temperature: Double,      // in °C
    val rainProbability: Int,     // in % (0 to 100)
    val rainVolume: Double,       // in mm
    val uvIndex: Double,          // UV level
    val relativeHumidity: Int     // in % (0 to 100)
)