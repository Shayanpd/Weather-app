package com.example.labb_b_2

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.labb_b_2.viewModel.WeatherViewModel
import com.example.labb_b_2.model.WeatherResponse
import com.example.labb_b_2.viewModel.getWeatherIconResource
import com.example.labb_b_2.repository.SharedPreferencesHelper
import com.google.gson.Gson
import android.view.Gravity
import androidx.compose.ui.graphics.Color

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
                val currentTempText =
                    "${it.hourly.temperature_2m[0]}°C" // Assuming the first hourly data is the current temperature
                currentTemp.text = currentTempText

                Log.d("HomeScreen", "WeatherData placeName: ${weatherResponse?.placeName}")

                placeNameView.text = "Location: ${it.placeName}"


                // Set weather icon based on the current weather code
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
                    textView.text =
                        "Time: ${data.time}, Temp: ${data.temperature}°C, Cloud Cover: ${data.cloudCover}%"
                    hourlyContainer.addView(textView)

                    if (data.time == hourlyData[0].time) { // Check if this is the first hourly data (current time)
                        currentCloudCover.text = "Cloud Cover: ${data.cloudCover}%"
                    }
                }
            }
        }

        // Observe daily data for the week
        // Observe daily data for the week
        viewModel.weekDailyData.observeForever { dailyData ->
            dailyData?.let {
                weeklyContainer.removeAllViews() // Clear previous views
                it.forEach { data ->
                    val dayLayout = LinearLayout(weeklyContainer.context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { setMargins(0, 8, 0, 8) }
                    }

                    // Weather icon
                    val weatherImageView = ImageView(weeklyContainer.context).apply {
                        setImageResource(getWeatherIconResource(data.weatherCode.toInt()))
                        layoutParams =
                            LinearLayout.LayoutParams(48.dpToPx(context), 48.dpToPx(context))
                                .apply {
                                    setMargins(16, 0, 16, 0)
                                }
                    }

                    // Full date and temperature text
                    val dayTextView = TextView(weeklyContainer.context).apply {
                        text =
                            "${data.date}: Max: ${data.maxTemperature}°C / Min: ${data.minTemperature}°C"
                        textSize = 16f
                        layoutParams =
                            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    }

                    // Add icon and text to layout
                    dayLayout.addView(weatherImageView)
                    dayLayout.addView(dayTextView)

                    // Add the day layout to the weekly container
                    weeklyContainer.addView(dayLayout)
                }
            }
        }

        // Button click listener
        fetchWeatherButton.setOnClickListener {
            val location = locationInput.text.toString().trim()
            if (location.isNotEmpty()) {
                if (isInternetAvailable(fetchWeatherButton.context)) {
                    // If there's internet, fetch weather data from the network
                    viewModel.fetchWeatherByLocationName(location, fetchWeatherButton.context)

                } else {
                    // If no internet, load cached data and show a message
                    Toast.makeText(
                        fetchWeatherButton.context,
                        "No internet connection. Showing cached data.",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadCachedWeather(fetchWeatherButton.context, viewModel)
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

    // Check if the internet is available
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Load cached weather data from SharedPreferences
    @RequiresApi(Build.VERSION_CODES.O)
    fun loadCachedWeather(context: Context, viewModel: WeatherViewModel) {
        // Retrieve cached data from SharedPreferences
        val cachedData = SharedPreferencesHelper.getWeatherData(context)
        if (cachedData != null) {
            Log.d("HomeScreen", "Cached JSON data loaded: $cachedData")

            // Convert the cached JSON into a WeatherResponse object
            val weatherResponse = Gson().fromJson(cachedData, WeatherResponse::class.java)
            Log.d("HomeScreen", "Deserialized WeatherResponse from cache: $weatherResponse")

            // Ensure that placeName is correctly set
            if (weatherResponse.placeName != null) {
                Log.d("HomeScreen", "Cached placeName: ${weatherResponse.placeName}")

            } else {
                Log.w("HomeScreen", "Cached data has null placeName!")
            }

            // Update the ViewModel with the cached weather response
            viewModel.fetchWeatherFromCache(weatherResponse)
            Log.d("HomeScreen", "Weather data updated in ViewModel from cache.")
            Log.d("HomeScreen", "Cached placeName: ${weatherResponse.placeName}")
        } else {
            Log.w("HomeScreen", "No cached data found in SharedPreferences.")
            Toast.makeText(context, "No cached data available.", Toast.LENGTH_SHORT).show()
        }
    }
}
// Extension function to convert dp to px
fun Int.dpToPx(context: Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
}