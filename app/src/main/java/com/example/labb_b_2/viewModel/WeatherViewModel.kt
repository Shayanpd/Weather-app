package com.example.labb_b_2.viewModel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.labb_b_2.R
import com.example.labb_b_2.model.*
import com.example.labb_b_2.repository.GeocodeRepository
import com.example.labb_b_2.repository.WeatherRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class WeatherViewModel : ViewModel() {

    private val weatherRepository = WeatherRepository()
    private val geocodeRepository = GeocodeRepository()

    private val _placeName = MutableLiveData<String>()
    val placeName: LiveData<String> get() = _placeName // Expose place name to observers

    private val _coordinates = MutableLiveData<Pair<Float, Float>>()
    val coordinates: LiveData<Pair<Float, Float>> get() = _coordinates

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

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
    fun fetchWeather(lon: Float, lat: Float) {
        weatherRepository.fetchWeatherForecast(
            lon = lon,
            lat = lat,
            onSuccess = { weatherResponse ->
                _weatherData.postValue(weatherResponse)
                processWeatherData(weatherResponse) // Process the raw data
            },
            onError = { errorMessage ->
                _error.postValue(errorMessage)
            }
        )
    }

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
                        _placeName.postValue(firstResult.display_name) // Update the place name LiveData
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

    // Combined function to fetch weather by location name
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchWeatherByLocationName(location: String) {
        fetchCoordinates(location) { coords ->
            fetchWeather(coords.first, coords.second)
        }
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
            DailyData(
                date = weatherResponse.daily.time[index],
                maxTemperature = weatherResponse.daily.temperature_2m_max[index],
                minTemperature = weatherResponse.daily.temperature_2m_min[index],
                weatherCode = weatherResponse.daily.weather_code[index],
                dayName = dayName
            )
        }
        _weekDailyData.postValue(dailyData)
    }


    // Extension function to observe LiveData once
    fun <T> LiveData<T>.observeOnce(observer: Observer<T>) {
        val wrapperObserver = object : Observer<T> {
            override fun onChanged(t: T) {
                observer.onChanged(t)
                removeObserver(this)
            }
        }
        observeForever(wrapperObserver)
    }
}

// Weather icons
fun getWeatherIconResource(code: Int): Int {
    return when (code) {
        0 -> R.drawable.sun // Clear sky
        1 -> R.drawable.mostly_sunny // Mainly clear
        2 -> R.drawable.cloudy // Overcast
        3 -> R.drawable.partly_cloudy // Partly cloudy
        61 -> R.drawable.rain // Light rain
        63 -> R.drawable.rain // Light rain
        65 -> R.drawable.rain // Light rain
        66 -> R.drawable.rain // Light rain
        67 -> R.drawable.rain // Light rain
        71 -> R.drawable.snow // Light snow
        73 -> R.drawable.snow // Light snow
        75 -> R.drawable.snow // Light snow
        77 -> R.drawable.snow // Light snow
        85 -> R.drawable.snow // Light snow
        86 -> R.drawable.snow // Light snow
        95 -> R.drawable.thunderstorm // Thunderstorm
        96 -> R.drawable.thunderstorm // Thunderstorm
        99 -> R.drawable.thunderstorm // Thunderstorm
        else -> R.drawable.unknown // Fallback icon
    }
}

// Data classes for UI-friendly data
data class HourlyData(
    val time: String,
    val temperature: Double,
    val cloudCover: Double
)

data class DailyData(
    val date: String,
    val maxTemperature: Double,
    val minTemperature: Double,
    val weatherCode: Double,
    val dayName: String
)
