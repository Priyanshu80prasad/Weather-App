
package com.priyanshu.weatherforcsting.utils

object WeatherUtils {

    fun getWeatherCondition(code: Int): Pair<String, String> {

        return when (code) {
            0 -> Pair("Clear Sky", "☀️")
            1 -> Pair("Mainly Clear", "🌤️")
            2 -> Pair("Partly Cloudy", "⛅")
            3 -> Pair("Overcast", "☁️")
            45, 48 -> Pair("Foggy", "🌫️")
            51, 53, 55 -> Pair("Drizzle", "🌦️")
            61, 63, 65 -> Pair("Rain", "🌧️")
            71, 73, 75 -> Pair("Snow", "❄️")
            80, 81, 82 -> Pair("Rain Showers", "🌧️")
            95, 96, 99 -> Pair("Thunderstorm", "⛈️")
            else -> Pair("Unknown", "🌡️")
        }
    }
}
