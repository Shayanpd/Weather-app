package com.example.labb_b_2.repository;


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GeocodeRetrofitInstance {
private const val BASE_URL = "https://geocode.maps.co/"


    val api: GeocodeService by lazy {
    Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeocodeService::class.java)
}
}
