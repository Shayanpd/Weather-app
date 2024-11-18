package com.example.labb_b_2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.example.labb_b_2.viewModel.WeatherViewModel

class MainActivity : ComponentActivity() {

    // ViewModel instance
    private val weatherViewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // Your XML layout

        // Find views
        val latitudeInput = findViewById<EditText>(R.id.latitudeInput)
        val longitudeInput = findViewById<EditText>(R.id.longitudeInput)
        val fetchWeatherButton = findViewById<Button>(R.id.fetchWeatherButton)
        val weatherOutput = findViewById<TextView>(R.id.weatherOutput)

        // Initialize HomeScreen and pass the required views and ViewModel
        val homeScreen = HomeScreen()
        homeScreen.setupHomeScreen(weatherViewModel, latitudeInput, longitudeInput, fetchWeatherButton, weatherOutput)
    }
}
