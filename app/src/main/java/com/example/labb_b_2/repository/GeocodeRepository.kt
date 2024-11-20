package com.example.labb_b_2.repository


import com.example.labb_b_2.model.GeocodingResponse
import retrofit2.Call

class GeocodeRepository {
    fun fetchCoordinates(query: String): Call<List<GeocodingResponse>> {
        return GeocodeRetrofitInstance.api.getCoordinates(query.replace(" ", "+"))
    }
}