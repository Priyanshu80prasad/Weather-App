package com.priyanshu.weatherforcsting.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.priyanshu.weatherforcsting.databinding.FragmentHomeBinding
import com.priyanshu.weatherforcsting.network.GeocodingRetrofit
import com.priyanshu.weatherforcsting.network.RetrofitInstance
import com.priyanshu.weatherforcsting.utils.WeatherUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private var isCitySearched = false
    private var weatherRequestId = 0
    private val locationSettingsLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) {
            if (isAdded && !isCitySearched) {
                fetchCurrentLocation()
            }
        }

    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val fine = permissions[
                Manifest.permission.ACCESS_FINE_LOCATION
            ] == true

            val coarse = permissions[
                Manifest.permission.ACCESS_COARSE_LOCATION
            ] == true

            if (fine || coarse) {
                fetchCurrentLocation()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(
            inflater,
            container,
            false
        )

        binding.searchCity.setOnClickListener {
            showCitySearchDialog()
        }

        checkLocationPermission()

        return binding.root
    }

    private fun showCitySearchDialog() {

        val editText = EditText(requireContext())
        editText.hint = "Enter city name"
        editText.setSingleLine(true)

        AlertDialog.Builder(requireContext())
            .setTitle("Search City")
            .setView(editText)
            .setPositiveButton("Search") { _, _ ->

                val cityName = editText.text.toString().trim()

                if (cityName.isNotEmpty()) {
                    searchCity(cityName)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Enter city name",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun searchCity(cityName: String) {

        isCitySearched = true
        weatherRequestId++

        viewLifecycleOwner.lifecycleScope.launch {

            try {
                val response =
                    GeocodingRetrofit.api.searchCity(cityName)

                val city = response.results?.firstOrNull()

                if (city == null) {
                    Toast.makeText(
                        requireContext(),
                        "City not found",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                binding.cityName.text =
                    "${city.name}, ${city.country ?: ""}"

                getWeather(city.latitude, city.longitude)

            } catch (e: Exception) {

                Log.e("CITY_SEARCH", e.message ?: "Search error")

                Toast.makeText(
                    requireContext(),
                    "City search failed.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    private fun checkLocationPermission() {

        val fine = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fine || coarse) {
            fetchCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    private fun fetchCurrentLocation() {

        if (!isAdded || isCitySearched) return

        val fine = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fine && !coarse) {
            checkLocationPermission()
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000
        ).build()

        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
            .build()

        LocationServices.getSettingsClient(requireActivity())
            .checkLocationSettings(settingsRequest)
            .addOnSuccessListener {
                requestDeviceLocation()
            }
            .addOnFailureListener { exception ->

                if (exception is ResolvableApiException) {

                    val request = IntentSenderRequest.Builder(
                        exception.resolution
                    ).build()

                    locationSettingsLauncher.launch(request)
                }
            }
    }

    private fun requestDeviceLocation() {

        if (!isAdded || isCitySearched) return

        val fine = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fine && !coarse) return

        val client =
            LocationServices.getFusedLocationProviderClient(
                requireActivity()
            )

        val token = CancellationTokenSource()

        client.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            token.token
        ).addOnSuccessListener { location ->

            if (!isAdded || isCitySearched) return@addOnSuccessListener

            if (location != null) {
                showCurrentWeather(
                    location.latitude,
                    location.longitude
                )
            } else {

                client.lastLocation.addOnSuccessListener { lastLocation ->

                    if (!isAdded || isCitySearched) {
                        return@addOnSuccessListener
                    }

                    if (lastLocation != null) {
                        showCurrentWeather(
                            lastLocation.latitude,
                            lastLocation.longitude
                        )
                    }
                }
            }

        }.addOnFailureListener { error ->

            Log.e(
                "LOCATION_ERROR",
                error.message ?: "Location error"
            )
        }
    }

    private fun showCurrentWeather(
        latitude: Double,
        longitude: Double
    ) {
        if (!isAdded || isCitySearched) return

        getWeather(latitude, longitude)
        getCityName(latitude, longitude)
    }


    private fun getCityName(
        latitude: Double,
        longitude: Double
    ) {

        val requestId = weatherRequestId

        viewLifecycleOwner.lifecycleScope.launch {

            try {
                val cityName = withContext(Dispatchers.IO) {

                    val geocoder = Geocoder(
                        requireContext(),
                        Locale.getDefault()
                    )

                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(
                        latitude,
                        longitude,
                        1
                    )

                    val address = addresses?.firstOrNull()

                    address?.locality
                        ?: address?.subAdminArea
                        ?: address?.adminArea
                        ?: "Current Location"
                }

                if (
                    isAdded &&
                    !isCitySearched &&
                    requestId == weatherRequestId
                ) {
                    binding.cityName.text = cityName
                }

            } catch (e: Exception) {

                Log.e("CITY_NAME", e.message ?: "Geocoder error")
            }
        }
    }


    private fun getWeather(
        latitude: Double,
        longitude: Double
    ) {

        val requestId = ++weatherRequestId

        viewLifecycleOwner.lifecycleScope.launch {

            try {
                val response = RetrofitInstance.api.getWeather(
                    latitude = latitude,
                    longitude = longitude
                )

                if (
                    !isAdded ||
                    requestId != weatherRequestId
                ) {
                    return@launch
                }

                val current = response.current
                val daily = response.daily

                val weather = WeatherUtils.getWeatherCondition(
                    current.weatherCode
                )

                binding.weatherIcon.text = weather.second
                binding.weatherCondition.text = weather.first

                binding.currentTemp.text =
                    "${current.temperature}°"

                binding.feelsLike.text =
                    "Feels like ${current.feelsLike}°"

                binding.humidity.text =
                    "${current.humidity}%"

                binding.windSpeed.text =
                    "${current.windSpeed} km/h"

                if (
                    daily.maxTemperature.isNotEmpty() &&
                    daily.minTemperature.isNotEmpty()
                ) {
                    binding.highLow.text =
                        "H: ${daily.maxTemperature[0]}°  " +
                                "L: ${daily.minTemperature[0]}°"
                }

                if (daily.sunrise.isNotEmpty()) {
                    binding.sunrise.text =
                        daily.sunrise[0].substringAfter("T")
                }

                if (daily.sunset.isNotEmpty()) {
                    binding.sunset.text =
                        daily.sunset[0].substringAfter("T")
                }

            } catch (e: Exception) {

                Log.e("WEATHER_API", e.message ?: "Weather API error")

                if (isAdded && requestId == weatherRequestId) {
                    Toast.makeText(
                        requireContext(),
                        "Weather not load . Check Internet.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
