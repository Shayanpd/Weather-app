package com.example.labb_b_2

import android.content.Context
import android.os.Build
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.labb_b_2.viewModel.WeatherViewModel
import com.example.labb_b_2.viewModel.getWeatherIconResource
import android.view.Gravity

class HomeScreen {

    @RequiresApi(Build.VERSION_CODES.O)
    fun setupHomeScreen(
        viewModel: WeatherViewModel,
        locationInput: EditText,
        fetchWeatherButton: Button,
        currentTemp: TextView,
        placeNameView: TextView,
        weatherIcon: ImageView,
        hourlyContainer: LinearLayout,
        weeklyContainer: LinearLayout,
        currentCloudCover: TextView
    ) {

        // Observe current weather data
        viewModel.weatherData.observeForever { weatherResponse ->
            weatherResponse?.let {
                val currentTempText = "${it.hourly.temperature_2m[0]}°C"
                currentTemp.text = currentTempText
                placeNameView.text = "Location: ${it.placeName}"

                // Set weather icon
                val weatherIconResource = getWeatherIconResource(it.daily.weather_code[0].toInt())
                weatherIcon.setImageResource(weatherIconResource)
            }
        }

        // Observe hourly data for today
        viewModel.todayHourlyData.observeForever { hourlyData ->
            hourlyData?.let {
                hourlyContainer.removeAllViews() // Clear previous views
                it.forEach { data ->
                    val textView = TextView(hourlyContainer.context)
                    textView.text = "Time: ${data.time}, Temp: ${data.temperature}°C, Cloud Cover: ${data.cloudCover}%"
                    hourlyContainer.addView(textView)

                    if (data.time == hourlyData[0].time) {
                        currentCloudCover.text = "Cloud Cover: ${data.cloudCover}%"
                    }
                }
            }
        }

        // Observe daily data for the week
        viewModel.weekDailyData.observeForever { dailyData ->
            dailyData?.let {
                weeklyContainer.removeAllViews()
                it.forEach { data ->
                    val dayLayout = LinearLayout(weeklyContainer.context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                    }

                    val weatherImageView = ImageView(weeklyContainer.context).apply {
                        setImageResource(getWeatherIconResource(data.weatherCode.toInt()))
                        layoutParams = LinearLayout.LayoutParams(48.dpToPx(context), 48.dpToPx(context))
                    }

                    val dayTextView = TextView(weeklyContainer.context).apply {
                        text = "${data.date}: Max: ${data.maxTemperature}°C / Min: ${data.minTemperature}°C"
                        textSize = 16f
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    }

                    dayLayout.addView(weatherImageView)
                    dayLayout.addView(dayTextView)
                    weeklyContainer.addView(dayLayout)
                }
            }
        }

        // Button click listener
        fetchWeatherButton.setOnClickListener {
            val location = locationInput.text.toString().trim()
            if (location.isNotEmpty()) {
                if (viewModel.isInternetAvailable(fetchWeatherButton.context)) {
                    // If there's internet, fetch weather data from the network
                    viewModel.fetchWeatherByLocationName(location, fetchWeatherButton.context)
                } else {
                    // If no internet, load cached data
                    Toast.makeText(
                        fetchWeatherButton.context,
                        "No internet connection. Showing cached data.",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.loadCachedWeather(fetchWeatherButton.context)
                }
            } else {
                Toast.makeText(
                    fetchWeatherButton.context,
                    "Please enter a location",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
// Extension function to convert dp to px
fun Int.dpToPx(context: Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
}