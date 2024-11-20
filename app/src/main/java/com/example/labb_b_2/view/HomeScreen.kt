package com.example.labb_b_2

import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import com.example.labb_b_2.viewModel.WeatherViewModel

class HomeScreen {

    fun setupHomeScreen(
        viewModel: WeatherViewModel,
        locationInput: EditText,
        fetchWeatherButton: Button,
        weatherOutput: TextView
    ) {
        // Observe weather data and update the UI
        viewModel.weatherData.observeForever { weatherResponse ->
            weatherResponse?.let {
                val forecastText = it.daily.time.indices.joinToString("\n") { index ->
                    val date = it.daily.time[index]
                    val maxTemp = it.daily.temperature_2m_max[index]
                    val minTemp = it.daily.temperature_2m_min[index]
                    val weatherCode = it.daily.weather_code[index]
                    "Date: $date, Max Temp: $maxTemp°C, Min Temp: $minTemp°C, Weather Code: $weatherCode"
                }
                weatherOutput.text = forecastText
            }
        }

        // Observe error messages
        viewModel.error.observeForever { error ->
            error?.let {
                Toast.makeText(weatherOutput.context, it, Toast.LENGTH_SHORT).show()
            }
        }

        // Button click listener
        fetchWeatherButton.setOnClickListener {
            val location = locationInput.text.toString().trim()

            if (location.isNotEmpty()) {
                // Fetch weather by location name
                viewModel.fetchWeatherByLocationName(location)
                Log.d("HomeScreen", "Fetching weather data for location: $location")
            } else {
                Toast.makeText(weatherOutput.context, "Invalid location name", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
