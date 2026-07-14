package com.github.eddie02345.takbootulog.data

import com.google.gson.annotations.SerializedName

data class WeatherResponseDto (
    @SerializedName("hourly")
    val hourly: HourlyDataDto
)

data class HourlyDataDto(
    @SerializedName("time")
    val time: List<String>,

    @SerializedName("temperature_2m")
    val temperatures: List<Double>,

    @SerializedName("apparent_temperature")
    val apparentTemperatures: List<Double>,

    @SerializedName("relative_humidity_2m")
    val humidities: List<Int>,

    @SerializedName("precipitation_probability")
    val rainProbabilities: List<Int>,

    @SerializedName("precipitation")
    val rainVolumes: List<Double>,

    @SerializedName("uv_index")
    val uvIndices: List<Double>
)