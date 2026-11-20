package com.priyanshu.weatherforcsting.model

import com.google.gson.annotations.SerializedName

data class GeocodingResponse(
    val results: List<CityResult>?
)

data class CityResult(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String?,
    @SerializedName("admin1")
    val state: String?
)
