package com.example.labb_b_2.repository


import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

import com.example.labb_b_2.model.GeocodingResponse

interface GeocodeService {
    @GET("search")
    fun getCoordinates(
        @Query("q") query: String,
        @Query("api_key") apiKey: String = "673d1166b5db8503993586heueb4bed"
    ): Call<List<GeocodingResponse>>
}