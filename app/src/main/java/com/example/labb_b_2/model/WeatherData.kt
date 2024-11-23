package com.example.labb_b_2.model

data class HourlyData(
    val time: String,
    val temperature: Double,
    val cloudCover: Double
)

data class DailyData(
    val date: String,
    val maxTemperature: Double,
    val minTemperature: Double,
    val weatherCode: Double,
    val dayName: String
)