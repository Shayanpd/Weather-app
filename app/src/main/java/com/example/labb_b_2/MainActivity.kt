package com.example.labb_b_2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.example.labb_b_2.viewModel.WeatherViewModel

class MainActivity : ComponentActivity() {

    private val weatherViewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Use your updated XML layout

        // Views
        val locationInput = findViewById<EditText>(R.id.locationInput)
        val fetchWeatherButton = findViewById<Button>(R.id.fetchWeatherButton)
        val placeNameView = findViewById<TextView>(R.id.placeName)
        val weatherIcon = findViewById<ImageView>(R.id.weatherIcon)
        val currentTemp = findViewById<TextView>(R.id.currentTemp)
        val hourlyContainer = findViewById<LinearLayout>(R.id.hourlyContainer)
        val weeklyContainer = findViewById<LinearLayout>(R.id.weeklyContainer)
        val currentCloudCover = findViewById<TextView>(R.id.currentCloudCover)

        // Initialize HomeScreen and set it up
        val homeScreen = HomeScreen()
        homeScreen.setupHomeScreen(
            weatherViewModel,
            locationInput,
            fetchWeatherButton,
            currentTemp,
            placeNameView,
            weatherIcon,
            hourlyContainer,
            weeklyContainer,
            currentCloudCover
        )
    }
}
