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

class WeatherViewModel : ViewModel() {
    private val weatherRepository = WeatherRepository()
    private val geocodeRepository = GeocodeRepository()

    private val _weatherData = MutableLiveData<WeatherResponse>()
    val weatherData: LiveData<WeatherResponse> get() = _weatherData

    private val _coordinates = MutableLiveData<Pair<Float, Float>>()
    val coordinates: LiveData<Pair<Float, Float>> get() = _coordinates

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // Fetch weather by longitude and latitude
    fun fetchWeather(lon: Float, lat: Float) {
        weatherRepository.fetchWeatherForecast(
            lon = lon,
            lat = lat,
            onSuccess = { weatherResponse ->
                _weatherData.postValue(weatherResponse) // Update weather data
            },
            onError = { errorMessage ->
                _error.postValue(errorMessage) // Update error message
            }
        )
    }

    // Fetch coordinates based on location name
    fun fetchCoordinates(query: String) {
        geocodeRepository.fetchCoordinates(query).enqueue(object : retrofit2.Callback<List<GeocodingResponse>> {
            override fun onResponse(
                call: retrofit2.Call<List<GeocodingResponse>>,
                response: retrofit2.Response<List<GeocodingResponse>>
            ) {
                if (response.isSuccessful) {
                    val results = response.body()
                    if (!results.isNullOrEmpty()) {
                        val firstResult = results[0]
                        Log.d("WeatherViewModel", "Coordinates fetched: ${firstResult.lat}, ${firstResult.lon}")
                        _coordinates.postValue(Pair(firstResult.lat.toFloat(), firstResult.lon.toFloat()))
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
    fun fetchWeatherByLocationName(location: String) {
        fetchCoordinates(location)
        coordinates.observeOnce { coords ->
            fetchWeather(coords.second, coords.first)
        }
    }

    // Extension function to observe LiveData once
    fun <T> LiveData<T>.observeOnce(observer: Observer<T>) {
        val wrapperObserver = object : Observer<T> {
            override fun onChanged(t: T) {
                observer.onChanged(t)
                removeObserver(this) // Stop observing after the first event
            }
        }
        observeForever(wrapperObserver)
    }
}
