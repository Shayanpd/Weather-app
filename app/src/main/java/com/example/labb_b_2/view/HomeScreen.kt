package com.example.labb_b_2

import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.labb_b_2.viewModel.WeatherViewModel

class HomeScreen {

    fun setupHomeScreen(
        viewModel: WeatherViewModel,
        locationInput: EditText,
        fetchWeatherButton: Button,
        weatherOutput: TextView,
        placeNameView: TextView // New TextView for displaying the place name
    ) {
        // Observe the place name and update the UI
        viewModel.placeName.observeForever { placeName ->
            placeNameView.text = "Location: $placeName"
        }

        // Observe hourly data for today
        viewModel.todayHourlyData.observeForever { hourlyData ->
            if (!hourlyData.isNullOrEmpty()) {
                val hourlyText = hourlyData.joinToString("\n") { data ->
                    "Time: ${data.time}, Temp: ${data.temperature}°C, Cloud Cover: ${data.cloudCover}%"
                }
                weatherOutput.text = "Hourly Forecast (Today):\n$hourlyText\n\n"
            } else {
                weatherOutput.text = "Hourly data is unavailable.\n\n"
            }
        }

        // Observe daily data for the rest of the week
        viewModel.weekDailyData.observeForever { dailyData ->
            if (!dailyData.isNullOrEmpty()) {
                val dailyText = dailyData.joinToString("\n") { data ->
                    "${data.dayName}: Max Temp: ${data.maxTemperature}°C, Min Temp: ${data.minTemperature}°C"
                }
                weatherOutput.append("Daily Forecast:\n$dailyText")
            } else {
                weatherOutput.append("Daily data is unavailable.")
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
