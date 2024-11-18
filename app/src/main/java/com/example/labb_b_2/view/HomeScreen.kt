package com.example.labb_b_2

import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import com.example.labb_b_2.viewModel.WeatherViewModel

class HomeScreen {

    fun setupHomeScreen(viewModel: WeatherViewModel, latitudeInput: EditText, longitudeInput: EditText,
                        fetchWeatherButton: Button, weatherOutput: TextView) {

        // Observe LiveData from WeatherViewModel
        viewModel.weatherData.observeForever { weatherResponse ->
            weatherResponse?.let {
                // Combine the weather details into a readable format
                val forecastText = it.daily.time.indices.joinToString("\n") { index ->
                    val date = it.daily.time[index]
                    val maxTemp = it.daily.temperature_2m_max[index]
                    val minTemp = it.daily.temperature_2m_min[index]
                    val weatherCode = it.daily.weather_code[index]
                    "Date: $date, Max Temp: $maxTemp°C, Min Temp: $minTemp°C, Weather Code: $weatherCode"
                }

                // Set the formatted forecast to the TextView
                weatherOutput.text = forecastText
            }
        }

        // Observe error messages
        viewModel.error.observeForever { error ->
            error?.let {
                Toast.makeText(weatherOutput.context, it, Toast.LENGTH_SHORT).show()
            }
        }

        // Button click logic
        fetchWeatherButton.setOnClickListener {
            val lat = latitudeInput.text.toString().toFloatOrNull()
            val lon = longitudeInput.text.toString().toFloatOrNull()

            if (lat != null && lon != null) {
                viewModel.fetchWeather(lon, lat)
                Log.d("WeatherRepository", "Fetching weather data for coordinates: lon = $lon, lat = $lat")
            } else {
                Toast.makeText(weatherOutput.context, "Invalid coordinates", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
