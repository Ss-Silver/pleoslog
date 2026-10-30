package com.pleoslog.car.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.*
import java.util.Locale

class CarLocationManager(private val context: Context) {

    private val tag = "CarLocationManager"
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    // Current State
    var currentLatitude: Double = 37.4115 // Pleos emulator default: Seongnam Siheung-dong
        private set
    var currentLongitude: Double = 127.1002
        private set
    var currentAddress: String = "성남시 수정구 시흥동"
        private set

    var onLocationChanged: ((Double, Double, String) -> Unit)? = null

    private var locationListener: LocationListener? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var simulationJob: Job? = null

    // Realistic Waypoints for demo (Siheung-dong -> Pangyo Techno Valley -> Yangjae HQ)
    private val demoWaypoints = listOf(
        Pair(37.4115, 127.1002) to "성남시 수정구 시흥동",
        Pair(37.4080, 127.1015) to "성남시 수정구 판교제2테크노밸리",
        Pair(37.4020, 127.1060) to "성남시 분당구 삼평동 봇들마을",
        Pair(37.3948, 127.1114) to "성남시 분당구 판교역로 카카오 판교아지트",
        Pair(37.4350, 127.0500) to "과천시 갈현동 지식정보타운",
        Pair(37.4690, 127.0400) to "서울 서초구 양재동 현대자동차 본사"
    )
    private var currentWaypointIndex = 0

    @SuppressLint("MissingPermission")
    fun startListening() {
        try {
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    updateRealLocation(location.latitude, location.longitude)
                }

                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }
            locationListener = listener

            // Register for both GPS and Network provider
            locationManager?.let { lm ->
                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 5f, listener)
                }
                if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000L, 5f, listener)
                }
                // Initial last known location
                val lastKnown = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                lastKnown?.let { updateRealLocation(it.latitude, it.longitude) }
            }
        } catch (e: SecurityException) {
            Log.w(tag, "Location permission not granted: ${e.message}")
        } catch (e: Exception) {
            Log.w(tag, "Error starting location listener: ${e.message}")
        }
    }

    private fun updateRealLocation(lat: Double, lon: Double) {
        currentLatitude = lat
        currentLongitude = lon
        scope.launch(Dispatchers.IO) {
            val address = reverseGeocode(lat, lon)
            withContext(Dispatchers.Main) {
                currentAddress = address
                onLocationChanged?.invoke(lat, lon, address)
            }
        }
    }

    fun startDrivingSimulation(speedKmh: Int) {
        simulationJob?.cancel()
        simulationJob = scope.launch {
            while (isActive) {
                delay(3000L) // Advance every 3 seconds while driving
                currentWaypointIndex = (currentWaypointIndex + 1) % demoWaypoints.size
                val (coords, addr) = demoWaypoints[currentWaypointIndex]
                currentLatitude = coords.first
                currentLongitude = coords.second
                currentAddress = addr
                Log.d(tag, "Simulated location advance: $addr ($currentLatitude, $currentLongitude)")
                onLocationChanged?.invoke(currentLatitude, currentLongitude, currentAddress)
            }
        }
    }

    fun stopDrivingSimulation() {
        simulationJob?.cancel()
        simulationJob = null
    }

    private fun reverseGeocode(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.KOREA)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Tiramisu async
                var resolved = ""
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    resolved = "${addr.locality ?: addr.adminArea ?: ""} ${addr.subLocality ?: addr.thoroughfare ?: ""}".trim()
                }
                if (resolved.isNotBlank()) resolved else "${String.format("%.4f", lat)}, ${String.format("%.4f", lon)}"
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    "${addr.locality ?: addr.adminArea ?: ""} ${addr.subLocality ?: addr.thoroughfare ?: ""}".trim()
                } else {
                    "${String.format("%.4f", lat)}, ${String.format("%.4f", lon)}"
                }
            }
        } catch (e: Exception) {
            "${String.format("%.4f", lat)}, ${String.format("%.4f", lon)}"
        }
    }

    fun stop() {
        try {
            locationListener?.let { locationManager?.removeUpdates(it) }
        } catch (e: Exception) {
            Log.w(tag, "Error removing location updates: ${e.message}")
        }
        stopDrivingSimulation()
        scope.cancel()
    }
}
