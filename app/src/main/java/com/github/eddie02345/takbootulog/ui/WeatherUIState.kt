package com.github.eddie02345.takbootulog.ui

import com.github.eddie02345.takbootulog.domain.HourlyForecast
import com.github.eddie02345.takbootulog.domain.Verdict

sealed interface WeatherUiState {
    object Loading : WeatherUiState

    data class Success(
        val forecast: HourlyForecast,
        val verdict: Verdict,
        val cityName: String // Added dynamically loaded city name here
    ) : WeatherUiState

    data class Error(
        val message: String
    ) : WeatherUiState
}