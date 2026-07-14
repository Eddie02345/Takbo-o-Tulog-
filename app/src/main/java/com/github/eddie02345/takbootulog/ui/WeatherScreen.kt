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

    // 1. Set up the permission request handler
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        coroutineScope.launch {
            if (fineGranted || coarseGranted) {
                // GPS Permission allowed -> fetch coordinates
                val location = LocationHelper.getLastKnownLocation(context)
                if (location != null) {
                    viewModel.fetchWeather(context, location.latitude, location.longitude)
                } else {
                    viewModel.fetchWeather(context) // Fallback to Baguio if location returned null
                }
            } else {
                // Permission Denied -> fallback to Baguio
                viewModel.fetchWeather(context)
            }
        }
    }

    // 2. Trigger permission prompt automatically on app startup
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
                    Text(
                        text = "Checking conditions...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            is WeatherUiState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Disaster Strike! 😭",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.message,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            // Re-request flow on click
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
        Text(
            text = "📍 $cityName",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 16.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            // FIXED OVERLAP TEXT HERE
            Text(
                text = verdict.title,
                fontSize = 48.sp,              // Slightly downsized so it is less cramped
                lineHeight = 54.sp,            // EXPLICIT LINE HEIGHT PREVENTS WRAP OVERLAP
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center   // Centered when wrapped
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = verdict.description,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Current Weather Stats",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    HorizontalDivider(thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.5f))

                    MetricRow(label = "🌡️ Temperature", value = "${forecast.temperature}°C")
                    MetricRow(label = "💧 Humidity", value = "${forecast.relativeHumidity}%")
                    MetricRow(label = "☀️ UV Index", value = "${forecast.uvIndex}")
                    MetricRow(label = "🌧️ Rain Probability", value = "${forecast.rainProbability}%")
                    MetricRow(label = "🪣 Rain Volume", value = "${forecast.rainVolume} mm")
                }
            }

            FilledIconButton(
                onClick = onRefresh,
                modifier = Modifier.size(56.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh data",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.Normal, color = Color.Black)
        Text(text = value, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}