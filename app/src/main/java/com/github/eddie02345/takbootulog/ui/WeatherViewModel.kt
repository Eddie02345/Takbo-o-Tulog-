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

    // Added lat and lon parameters that default to Baguio City coordinates
    fun fetchWeather(
        context: Context,
        lat: Double = 16.4023,
        lon: Double = 120.5960
    ) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading

            val result = repository.getCurrentForecast(context = context, lat = lat, lon = lon)

            result.onSuccess { pair ->
                val forecast = pair.first
                val cityName = pair.second
                val verdict = DecisionEngine.evaluate(forecast)

                _uiState.value = WeatherUiState.Success(forecast, verdict, cityName)
            }.onFailure { exception ->
                _uiState.value = WeatherUiState.Error(
                    message = exception.localizedMessage ?: "Hindi makakonekta sa server, boss."
                )
            }
        }
    }
}