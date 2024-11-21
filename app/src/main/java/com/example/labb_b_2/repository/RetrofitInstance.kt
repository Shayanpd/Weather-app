package com.example.labb_b_2.repository

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val PRODUCTION_BASE_URL = "https://api.open-meteo.com/v1/"
    private const val TEST_BASE_URL = "https://maceo.sth.kth.se/weather/" // not working??

    // Flag to switch between production and test server, to not get blacklisted by autoritities :(
    private const val USE_TEST_SERVER = false

    val api: WeatherService by lazy {
        Retrofit.Builder()
            .baseUrl(if (USE_TEST_SERVER) TEST_BASE_URL else PRODUCTION_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)
    }
}
