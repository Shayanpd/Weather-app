package com.example.labb_b_2.repository

import android.content.Context
import com.google.gson.Gson

object SharedPreferencesHelper {

    private const val PREFS_NAME = "WeatherAppPrefs"
    private const val WEATHER_DATA_KEY = "weather_data"

    fun saveWeatherData(context: Context, weatherResponse: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(WEATHER_DATA_KEY, weatherResponse).apply()
    }

    fun getWeatherData(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(WEATHER_DATA_KEY, null)
    }
}
