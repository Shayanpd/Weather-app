package com.example.labb_b_2.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.labb_b_2.model.WeatherResponse
import com.example.labb_b_2.repository.WeatherRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class WeatherViewModel : ViewModel() {
    private val repository = WeatherRepository()

    private val _weatherData = MutableLiveData<WeatherResponse>()
    val weatherData: LiveData<WeatherResponse> get() = _weatherData

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun fetchWeather(lon: Float, lat: Float) {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&daily=temperature_2m_max,temperature_2m_min,weather_code&timezone=auto"
        Log.d("WeatherViewModel", "Request URL: $url")

        repository.fetchWeatherForecast(lon, lat).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                if (response.isSuccessful) {
                    // Log the full response body (weather data)
                    val weatherResponse = response.body()
                    Log.d("WeatherViewModel", "Weather data fetched successfully: $weatherResponse") // Log the fetched data

                    // If response body is not null, update LiveData
                    weatherResponse?.let {
                        _weatherData.value = it
                    }
                } else {
                    // Log failure message
                    Log.e("WeatherViewModel", "Failed to fetch data: ${response.message()}")
                    _error.value = "Failed to fetch data: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                // Log error during the API call
                Log.e("WeatherViewModel", "Error fetching weather: ${t.message}")
                _error.value = "Error: ${t.message}"
            }
        })
    }
}
