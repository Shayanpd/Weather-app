package com.example.labb_b_2.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.labb_b_2.model.GeocodingResponse
import com.example.labb_b_2.model.WeatherResponse
import com.example.labb_b_2.repository.GeocodeRepository
import com.example.labb_b_2.repository.WeatherRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class WeatherViewModel : ViewModel() {
    private val weatherRepository = WeatherRepository()
    private val geocodeRepository = GeocodeRepository()

    private val _weatherData = MutableLiveData<WeatherResponse>()
    val weatherData: LiveData<WeatherResponse> get() = _weatherData

    // LiveData for geocoding data
    private val _coordinates = MutableLiveData<Pair<Float, Float>>()
    val coordinates: LiveData<Pair<Float, Float>> get() = _coordinates

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun fetchWeather(lon: Float, lat: Float) {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&daily=temperature_2m_max,temperature_2m_min,weather_code&timezone=auto"
        Log.d("WeatherViewModel", "Request URL: $url")

        weatherRepository.fetchWeatherForecast(lon, lat).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                if (response.isSuccessful) {
                    // Log the full response body (weather data)
                    val weatherResponse = response.body()
                    Log.d("WeatherViewModel", "Weather data fetched successfully: $weatherResponse") // Log the fetched data

                    // If response body is not null, update LiveData
                    weatherResponse?.let {
                        _weatherData.value = it
                    }
                } else {
                    // Log failure message
                    Log.e("WeatherViewModel", "Failed to fetch data: ${response.message()}")
                    _error.value = "Failed to fetch data: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                // Log error during the API call
                Log.e("WeatherViewModel", "Error fetching weather: ${t.message}")
                _error.value = "Error: ${t.message}"
            }
        })
    }
    fun fetchCoordinates(query: String) {
        geocodeRepository.fetchCoordinates(query).enqueue(object : Callback<List<GeocodingResponse>> {
            override fun onResponse(
                call: Call<List<GeocodingResponse>>,
                response: Response<List<GeocodingResponse>>
            ) {
                if (response.isSuccessful) {
                    val results = response.body()
                    if (!results.isNullOrEmpty()) {
                        val firstResult = results[0]
                        Log.d("WeatherViewModel", "Coordinates fetched: ${firstResult.lat}, ${firstResult.lon}")
                        _coordinates.value = Pair(firstResult.lat.toFloat(), firstResult.lon.toFloat())
                    } else {
                        Log.e("WeatherViewModel", "No results found for: $query")
                        _error.value = "No results found for: $query"
                    }
                } else {
                    Log.e("WeatherViewModel", "Failed to fetch coordinates: ${response.message()}")
                    _error.value = "Failed to fetch coordinates: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<List<GeocodingResponse>>, t: Throwable) {
                Log.e("WeatherViewModel", "Error fetching coordinates: ${t.message}")
                _error.value = "Error: ${t.message}"
            }
        })
    }
    fun <T> LiveData<T>.observeOnce(observer: Observer<T>) {
        val wrapperObserver = object : Observer<T> {
            override fun onChanged(t: T) {
                // Call the original observer's onChanged
                observer.onChanged(t)
                // Remove the observer after the first change
                removeObserver(this)
            }
        }
        // Start observing with the wrapper
        observeForever(wrapperObserver)
    }

    // Combined function to fetch weather by location name
    fun fetchWeatherByLocationName(location: String) {
        fetchCoordinates(location)
        coordinates.observeOnce { coords ->
            fetchWeather(coords.second, coords.first)
        }
    }
}
