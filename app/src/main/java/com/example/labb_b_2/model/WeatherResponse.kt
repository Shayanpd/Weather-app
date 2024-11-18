package com.example.labb_b_2.model



data class WeatherResponse(
    val daily: DailyWeather
)

data class DailyWeather(
    val time: List<String>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>,
    val cloud_cover: List<Double>, //TODO: vettefan det finns bara hourly cloud cover tror det är bättre att använda weather_code nedan för att visa
    val weather_code: List<Double>
)
