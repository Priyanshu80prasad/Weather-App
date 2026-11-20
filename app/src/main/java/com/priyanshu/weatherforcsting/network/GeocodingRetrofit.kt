
package com.priyanshu.weatherforcsting.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GeocodingRetrofit {

    private const val BASE_URL = "https://geocoding-api.open-meteo.com/"

    val api: GeocodingApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeocodingApiService::class.java)
    }
}
