package com.github.eddie02345.takbootulog.ui

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

@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Automatically trigger the fetch when the screen first mounts
    LaunchedEffect(Unit) {
        viewModel.fetchWeather(context)
    }

    // Dynamic background color handling
    val targetBackgroundColor = when (val state = uiState) {
        is WeatherUiState.Success -> Color(android.graphics.Color.parseColor(state.verdict.colorHex))
        is WeatherUiState.Error -> Color(0xFFD32F2F) // Muted Red for system error
        WeatherUiState.Loading -> MaterialTheme.colorScheme.background
    }

    // Smooth color fading transition animation when states switch
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
                        onClick = { viewModel.fetchWeather(context) },
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
                    onRefresh = { viewModel.fetchWeather(context) }
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
        // Top Section: Dynamic City/Town Location Header
        Text(
            text = "📍 $cityName",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 16.dp)
        )

        // Middle Section: The Main Target Verdict and Relatable Description
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = verdict.title,
                fontSize = 54.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = verdict.description,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp
            )
        }

        // Bottom Section: The Weather Metrics Card Display & Action Button
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

            // Refresh IconButton
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