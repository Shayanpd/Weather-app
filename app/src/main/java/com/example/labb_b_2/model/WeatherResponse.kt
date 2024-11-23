package com.example.labb_b_2.model

data class WeatherResponse(
    val daily: DailyWeather,
    val hourly: HourlyWeather, // Add hourly data
    var placeName: String?
)

data class DailyWeather(
    val time: List<String>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>,
    val weather_code: List<Double>
)

data class HourlyWeather(
    val time: List<String>,          // Hourly timestamps
    val temperature_2m: List<Double>, // Hourly temperatures
    val cloudcover: List<Double>     // Hourly cloud cover
)
