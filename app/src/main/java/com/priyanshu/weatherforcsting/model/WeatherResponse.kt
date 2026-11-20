package com.priyanshu.weatherforcsting.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(

    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val current: CurrentWeather,
    val hourly: HourlyWeatherResponse,
    val daily: DailyWeatherResponse
)

data class CurrentWeather(

    @SerializedName("temperature_2m")
    val temperature: Double,

    @SerializedName("relative_humidity_2m")
    val humidity: Int,

    @SerializedName("apparent_temperature")
    val feelsLike: Double,

    @SerializedName("is_day")
    val isDay: Int,

    @SerializedName("weather_code")
    val weatherCode: Int,

    @SerializedName("wind_speed_10m")
    val windSpeed: Double
)

data class HourlyWeatherResponse(

    val time: List<String>,

    @SerializedName("temperature_2m")
    val temperature: List<Double>,

    @SerializedName("weather_code")
    val weatherCode: List<Int>
)

data class DailyWeatherResponse(

    val time: List<String>,

    @SerializedName("temperature_2m_max")
    val maxTemperature: List<Double>,

    @SerializedName("temperature_2m_min")
    val minTemperature: List<Double>,

    @SerializedName("weather_code")
    val weatherCode: List<Int>,

    val sunrise: List<String>,

    val sunset: List<String>
)
