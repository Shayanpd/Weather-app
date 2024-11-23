package com.example.labb_b_2.viewModel

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.labb_b_2.R
import com.example.labb_b_2.model.*
import com.example.labb_b_2.repository.GeocodeRepository
import com.example.labb_b_2.repository.SharedPreferencesHelper
import com.example.labb_b_2.repository.WeatherRepository
import com.google.gson.Gson
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class WeatherViewModel : ViewModel() {

    private val weatherRepository = WeatherRepository()
    private val geocodeRepository = GeocodeRepository()

    private val _placeName = MutableLiveData<String>()

    private val _coordinates = MutableLiveData<Pair<Float, Float>>()

    private val _error = MutableLiveData<String>()

    // Raw weather data
    private val _weatherData = MutableLiveData<WeatherResponse>()
    val weatherData: LiveData<WeatherResponse> get() = _weatherData

    // Processed hourly data for today
    private val _todayHourlyData = MutableLiveData<List<HourlyData>>()
    val todayHourlyData: LiveData<List<HourlyData>> get() = _todayHourlyData

    // Processed daily data for the week
    private val _weekDailyData = MutableLiveData<List<DailyData>>()
    val weekDailyData: LiveData<List<DailyData>> get() = _weekDailyData


    // Fetch weather by longitude and latitude
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchWeather(lon: Float, lat: Float, context: Context) {
        weatherRepository.fetchWeatherForecast(
            lon = lon,
            lat = lat,
            onSuccess = { weatherResponse ->
                _weatherData.postValue(weatherResponse)

                processWeatherData(weatherResponse) // Process the raw data

                weatherResponse.placeName = _placeName.value
                // Save the weather data in SharedPreferences
                val gson = Gson()
                val weatherJson = gson.toJson(weatherResponse)
                SharedPreferencesHelper.saveWeatherData(context, weatherJson)
            },
            onError = { errorMessage ->
                _error.postValue(errorMessage)
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadCachedWeather(context: Context) {
        // Retrieve cached data from SharedPreferences
        val cachedData = SharedPreferencesHelper.getWeatherData(context)
        if (cachedData != null) {
            Log.d("WeatherViewModel", "Cached JSON data loaded: $cachedData")

            // Convert the cached JSON into a WeatherResponse object
            val weatherResponse = Gson().fromJson(cachedData, WeatherResponse::class.java)
            Log.d("WeatherViewModel", "Deserialized WeatherResponse from cache: $weatherResponse")

            // Ensure that placeName is correctly set
            if (weatherResponse.placeName != null) {
                Log.d("WeatherViewModel", "Cached placeName: ${weatherResponse.placeName}")
            } else {
                Log.w("WeatherViewModel", "Cached data has null placeName!")
            }

            // Update the ViewModel with the cached weather response
            fetchWeatherFromCache(weatherResponse)
            Log.d("WeatherViewModel", "Weather data updated in ViewModel from cache.")
        } else {
            Log.w("WeatherViewModel", "No cached data found in SharedPreferences.")
            // You can handle the absence of cached data here, e.g., by notifying the user.
        }
    }

    // Process cached weather data

    // Fetch coordinates based on location name
    fun fetchCoordinates(query: String, onSuccess: (Pair<Float, Float>) -> Unit) {
        geocodeRepository.fetchCoordinates(query).enqueue(object : retrofit2.Callback<List<GeocodingResponse>> {
            override fun onResponse(
                call: retrofit2.Call<List<GeocodingResponse>>,
                response: retrofit2.Response<List<GeocodingResponse>>
            ) {
                if (response.isSuccessful) {
                    val results = response.body()
                    if (!results.isNullOrEmpty()) {
                        val firstResult = results[0]
                        _placeName.value = firstResult.display_name
                        Log.d("WeatherViewModel","fetch coordnates display name: ${_placeName.value}")
                        val coordinates = Pair(firstResult.lat.toFloat(), firstResult.lon.toFloat())
                        _coordinates.postValue(coordinates) // Update coordinates LiveData
                        onSuccess(coordinates) // Trigger the onSuccess callback
                    } else {
                        _error.postValue("No results found for: $query")
                    }
                } else {
                    _error.postValue("Failed to fetch coordinates: ${response.message()}")
                }
            }

            override fun onFailure(call: retrofit2.Call<List<GeocodingResponse>>, t: Throwable) {
                _error.postValue("Error: ${t.message}")
            }
        })
    }

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Combined function to fetch weather by location name
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchWeatherByLocationName(location: String, context: Context) {
        fetchCoordinates(location) { coords ->
            fetchWeather(coords.first, coords.second, context) // Fetch weather by coordinates

            // Once the weather data is fetched, update WeatherResponse with placeName
            val weatherResponse = _weatherData.value?.copy(
                placeName = _placeName.value // Ensure placeName is set here
            ) ?: WeatherResponse(
                daily = DailyWeather(emptyList(), emptyList(), emptyList(), emptyList()),
                hourly = HourlyWeather(emptyList(), emptyList(), emptyList()),
                placeName = _placeName.value // Ensure it's set in the fallback case as well
            )

            Log.d("WeatherViewModel", "Place name before saving: ${_placeName.value}")
            Log.d("WeatherViewModel", "WeatherResponse placeName before saving: ${_weatherData.value?.placeName}")

            // Save the updated WeatherResponse in SharedPreferences
            val gson = Gson()
            val weatherJson = gson.toJson(weatherResponse)

            SharedPreferencesHelper.saveWeatherData(context, weatherJson)
            Log.d("WeatherViewModel", "Cached WeatherResponse: $weatherResponse")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchWeatherFromCache(weatherResponse: WeatherResponse) {

        weatherResponse.placeName?.let {
            _placeName.postValue(it)
        }
        Log.d("HomeScreen", "Cached placeName: ${weatherResponse.placeName}")
        _weatherData.postValue(weatherResponse)

        processWeatherData(weatherResponse) // Process the cached data
        Log.d("HomeScreen", "Cached placeName: ${weatherResponse.placeName}")



        Log.d("WeatherViewModel", "Cached WeatherResponse placeName: ${weatherResponse}")

    }

    // Process raw weather data
    @RequiresApi(Build.VERSION_CODES.O)
    private fun processWeatherData(weatherResponse: WeatherResponse) {


        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val todayDate = LocalDate.now()
        val todayFormatted = todayDate.format(formatter)



        // Process hourly data for today
        val hourlyData = weatherResponse.hourly.time.indices.mapNotNull { index ->
            if (weatherResponse.hourly.time[index].startsWith(todayFormatted)) {
                HourlyData(
                    time = weatherResponse.hourly.time[index],
                    temperature = weatherResponse.hourly.temperature_2m[index],
                    cloudCover = weatherResponse.hourly.cloudcover[index]
                )
            } else null
        }
        _todayHourlyData.postValue(hourlyData)

        // Process daily data for the week
        val dailyData = weatherResponse.daily.time.indices.map { index ->
            val date = LocalDate.parse(weatherResponse.daily.time[index], formatter)
            val dayName = when (date) {
                todayDate -> "Today"
                todayDate.plusDays(1) -> "Tomorrow"
                else -> date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
            }

            // Format the full date (e.g., "Friday, November 23, 2024")
            val fullDate = date.format(DateTimeFormatter.ofPattern("EEEE - MMMM d, yyyy"))

            DailyData(
                date = fullDate, // Use the full date format here
                maxTemperature = weatherResponse.daily.temperature_2m_max[index],
                minTemperature = weatherResponse.daily.temperature_2m_min[index],
                weatherCode = weatherResponse.daily.weather_code[index],
                dayName = dayName // Only store "Today", "Tomorrow", or the weekday name
            )
        }
        _weekDailyData.postValue(dailyData)
    }

}


// Weather icons
fun getWeatherIconResource(code: Int): Int {
    return when (code) {
        0 -> R.drawable.sun // Clear sky
        1 -> R.drawable.mostly_sunny // Mainly clear
        2 -> R.drawable.cloudy // Overcast
        3 -> R.drawable.partly_cloudy // Partly cloudy

        // For rain codes, we use the same icon for light to heavy rain
        in 51..67 -> R.drawable.rain // Any drizzle or rain (light to torrential rain)

        // For snow codes, we use the same icon for light to extreme snow
        in 71..86 -> R.drawable.snow // Any snow (light to extreme snow)

        // For thunderstorm codes, we use the thunderstorm icon
        in 95..99 -> R.drawable.thunderstorm // Any thunderstorm (from regular to severe)

        // Fallback for unknown or error codes
        else -> R.drawable.unknown // Fallback icon for unrecognized or missing codes
    }
}

