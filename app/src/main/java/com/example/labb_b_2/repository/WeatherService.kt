package com.example.labb_b_2.repository

import com.example.labb_b_2.model.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("forecast")
    fun getWeatherForecast(
        @Query("latitude") lat: Float,
        @Query("longitude") lon: Float,
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,weather_code",
        @Query("hourly") hourly: String = "temperature_2m,cloudcover",
        @Query("timezone") timezone: String = "auto"
    ): Call<WeatherResponse>
}
