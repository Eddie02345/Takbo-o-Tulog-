package com.github.eddie02345.takbootulog.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.eddie02345.takbootulog.domain.HourlyForecast
import com.github.eddie02345.takbootulog.domain.Verdict
import kotlinx.coroutines.launch

@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        coroutineScope.launch {
            if (fineGranted || coarseGranted) {
                val location = LocationHelper.getLastKnownLocation(context)
                if (location != null) {
                    viewModel.fetchWeather(context, location.latitude, location.longitude)
                } else {
                    viewModel.fetchWeather(context)
                }
            } else {
                viewModel.fetchWeather(context)
            }
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    val targetBackgroundColor = when (val state = uiState) {
        is WeatherUiState.Success -> Color(android.graphics.Color.parseColor(state.verdict.colorHex))
        is WeatherUiState.Error -> Color(0xFFD32F2F)
        WeatherUiState.Loading -> MaterialTheme.colorScheme.background
    }

    val animatedBgColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = tween(durationMillis = 500),
        label = "BgColorAnimation"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(animatedBgColor)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is WeatherUiState.Loading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Checking conditions...", style = MaterialTheme.typography.bodyMedium)
                }
            }
            is WeatherUiState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Disaster Strike! 😭", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = state.message, color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) {
                        Text("Subukan Ulit")
                    }
                }
            }
            is WeatherUiState.Success -> {
                SuccessContent(
                    cityName = state.cityName,
                    verdict = state.verdict,
                    forecast = state.forecast,
                    onRefresh = {
                        coroutineScope.launch {
                            val location = LocationHelper.getLastKnownLocation(context)
                            if (location != null) {
                                viewModel.fetchWeather(context, location.latitude, location.longitude)
                            } else {
                                viewModel.fetchWeather(context)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SuccessContent(
    cityName: String,
    verdict: Verdict,
    forecast: HourlyForecast,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Dynamic Header
        Text(
            text = "📍 $cityName",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.padding(top = 12.dp)
        )

        // Middle Section: Massive Clean Verdict Display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = verdict.title,
                fontSize = 56.sp,
                lineHeight = 62.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = verdict.description,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.95f),
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )
        }

        // Bottom Section: Cleaned up Weather Metrics Card & Refresh Action
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Current Weather Stats",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.Black.copy(alpha = 0.4f),
                        letterSpacing = 0.5.sp
                    )
                    HorizontalDivider(thickness = 1.dp, color = Color.Black.copy(alpha = 0.08f))

                    MetricRow(label = "🌡️ Temperature", value = "${forecast.temperature}°C")

                    // 🆕 NEW FEELS LIKE TEMPERATURE ROW
                    MetricRow(
                        label = "🥵 Feels Like",
                        value = "${forecast.feelsLikeTemperature}°C",
                        valueColor = if (forecast.feelsLikeTemperature > forecast.temperature) Color(0xFFE65100) else Color.Black
                    )

                    MetricRow(label = "💧 Humidity", value = "${forecast.relativeHumidity}%")
                    MetricRow(label = "☀️ UV Index", value = "${forecast.uvIndex}")
                    MetricRow(label = "🌧️ Rain Probability", value = "${forecast.rainProbability}%")
                    MetricRow(label = "🪣 Rain Volume", value = "${forecast.rainVolume} mm")
                }
            }

            FilledIconButton(
                onClick = onRefresh,
                modifier = Modifier.size(60.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(20.dp) // Clean squircle shape
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh data",
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            color = Color.Black.copy(alpha = 0.7f),
            fontSize = 15.sp
        )
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            fontSize = 16.sp
        )
    }
}