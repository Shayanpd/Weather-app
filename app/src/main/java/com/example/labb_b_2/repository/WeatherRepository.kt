package com.example.labb_b_2.repository

import com.example.labb_b_2.model.WeatherResponse
import retrofit2.Call

class WeatherRepository {
    fun fetchWeatherForecast(lon: Float, lat: Float): Call<WeatherResponse> {
        return RetrofitInstance.api.getWeatherForecast(lon, lat)
    }
}
