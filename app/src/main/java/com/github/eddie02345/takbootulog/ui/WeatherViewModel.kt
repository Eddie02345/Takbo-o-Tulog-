package com.github.eddie02345.takbootulog.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.eddie02345.takbootulog.data.NetworkModule
import com.github.eddie02345.takbootulog.domain.DecisionEngine
import com.github.eddie02345.takbootulog.domain.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val repository: WeatherRepository = NetworkModule.repository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    // We remove the fetchWeather call from init because it requires a Context now.
    // Compose will fire it automatically when the screen mounts.

    fun fetchWeather(context: Context) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading

            // Try changing these coordinates to 15.4381, 119.9056 to test it out!
            val result = repository.getCurrentForecast(context = context, lat = 16.4023, lon = 120.5960)

            result.onSuccess { pair ->
                val forecast = pair.first
                val cityName = pair.second
                val verdict = DecisionEngine.evaluate(forecast)

                _uiState.value = WeatherUiState.Success(forecast, verdict, cityName)
            }.onFailure { exception ->
                _uiState.value = WeatherUiState.Error(
                    message = exception.localizedMessage ?: "Hindi maka-connect sa server, boss."
                )
            }
        }
    }
}