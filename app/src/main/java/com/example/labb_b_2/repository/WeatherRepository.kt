package com.example.labb_b_2.repository

import com.example.labb_b_2.model.WeatherResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeatherRepository {

    fun fetchWeatherForecast(
        lon: Float,
        lat: Float,
        onSuccess: (WeatherResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        // Perform the network call
        RetrofitInstance.api.getWeatherForecast(lon, lat).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { weatherResponse ->
                        onSuccess(weatherResponse) // Pass the result to the onSuccess callback
                    } ?: onError("Empty response body")
                } else {
                    onError("Failed to fetch data: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                onError("Network error: ${t.message}")
            }
        })
    }

}
