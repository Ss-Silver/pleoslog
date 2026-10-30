package com.pleoslog.car

import ai.pleos.playground.vehicle.constant.control.VehicleGear
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.pleoslog.car.data.api.ApiClient
import com.pleoslog.car.data.model.CompleteSessionRequest
import com.pleoslog.car.data.model.RoutePointRequest
import com.pleoslog.car.data.model.BatchRouteRequest
import com.pleoslog.car.data.model.StartSessionRequest
import com.pleoslog.car.databinding.ActivityMainBinding
import com.pleoslog.car.location.CarLocationManager
import com.pleoslog.car.vehicle.PleosVehicleManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val tag = "PleosLogMainActivity"

    private lateinit var binding: ActivityMainBinding
    private lateinit var vehicleManager: PleosVehicleManager
    private lateinit var locationManager: CarLocationManager

    private var currentGear: String = "P"
    private var currentSpeed: Int = 0
    private var currentOdometer: Double = 14500.0
    private var sessionStartOdometer: Double = 14500.0
    private var isDrivingSessionActive: Boolean = false
    private var activeSessionId: Long? = null
    private var routeTrackingJob: Job? = null
    private var tripTimerJob: Job? = null
    private var tripElapsedSeconds: Long = 0L

    // Driving Purpose: BUSINESS, COMMUTE, PERSONAL
    private var selectedPurpose: String = "BUSINESS"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setupListeners()
        setupPurposeSelectors()
        initLocationManager()
        initPleosVehicleSdk()
    }

    private fun initViews() {
        updateGearUI("P")
        updateSpeedUI(0)
        updateOdometerUI(currentOdometer)
        updateSessionStatusUI(false)
        binding.tvTripDistance.text = "0.0 km"
        binding.tvTripDuration.text = "00:00:00"
        binding.tvDrivingMode.text = "성남시 수정구 시흥동"
        binding.tvStartAddress.text = "출발 대기 중"
    }

    private fun setupListeners() {
        // D단 전환 (출발 시뮬레이션 버튼)
        binding.btnShiftD.setOnClickListener {
            handleShiftToDrive(isManual = true)
        }

        // P단 전환 (도착 및 정차 시뮬레이션 버튼)
        binding.btnShiftP.setOnClickListener {
            handleShiftToPark(isManual = true)
        }

        // 속도 변경 시뮬레이션
        binding.btnToggleSpeed.setOnClickListener {
            toggleSpeed()
        }
    }

    private fun setupPurposeSelectors() {
        binding.btnPurposeBusiness.setOnClickListener {
            selectPurpose("BUSINESS")
        }
        binding.btnPurposeCommute.setOnClickListener {
            selectPurpose("COMMUTE")
        }
        binding.btnPurposePersonal.setOnClickListener {
            selectPurpose("PERSONAL")
        }
    }

    private fun selectPurpose(purpose: String) {
        selectedPurpose = purpose
        val selectedBg = ContextCompat.getDrawable(this, R.drawable.bg_purpose_selected)
        val unselectedBg = ContextCompat.getDrawable(this, R.drawable.bg_purpose_unselected)
        val skyColor = ContextCompat.getColor(this, R.color.hyundai_sky)
        val primaryColor = ContextCompat.getColor(this, R.color.text_primary)

        // Reset all
        binding.btnPurposeBusiness.background = unselectedBg
        binding.tvPurposeBusinessTitle.setTextColor(primaryColor)
        binding.btnPurposeCommute.background = unselectedBg
        binding.tvPurposeCommuteTitle.setTextColor(primaryColor)
        binding.btnPurposePersonal.background = unselectedBg
        binding.tvPurposePersonalTitle.setTextColor(primaryColor)

        when (purpose) {
            "BUSINESS" -> {
                binding.btnPurposeBusiness.background = selectedBg
                binding.tvPurposeBusinessTitle.setTextColor(skyColor)
            }
            "COMMUTE" -> {
                binding.btnPurposeCommute.background = selectedBg
                binding.tvPurposeCommuteTitle.setTextColor(skyColor)
            }
            "PERSONAL" -> {
                binding.btnPurposePersonal.background = selectedBg
                binding.tvPurposePersonalTitle.setTextColor(skyColor)
            }
        }
    }

    private fun initLocationManager() {
        locationManager = CarLocationManager(this).apply {
            onLocationChanged = { lat, lon, addr ->
                runOnUiThread {
                    binding.tvDrivingMode.text = addr
                }
            }
        }
        locationManager.startListening()
    }

    private fun initPleosVehicleSdk() {
        vehicleManager = PleosVehicleManager(this).apply {
            onGearChanged = { gear ->
                runOnUiThread {
                    when (gear) {
                        VehicleGear.GEAR_DRIVE -> handleShiftToDrive(isManual = false)
                        VehicleGear.GEAR_PARK -> handleShiftToPark(isManual = false)
                        VehicleGear.GEAR_REVERSE -> updateGearUI("R")
                        VehicleGear.GEAR_NEUTRAL -> updateGearUI("N")
                        else -> {}
                    }
                }
            }

            onOdometerChanged = { odoKm ->
                runOnUiThread {
                    currentOdometer = odoKm
                    updateOdometerUI(currentOdometer)
                    if (isDrivingSessionActive) {
                        val trip = maxOf(0.0, currentOdometer - sessionStartOdometer)
                        binding.tvTripDistance.text = "${String.format("%.1f", trip)} km"
                    }
                }
            }

            onVinRetrieved = { vin ->
                runOnUiThread {
                    binding.tvVehicleInfo.text = "아이오닉 6 (VIN: ${vin.takeLast(6)})"
                }
            }
        }

        vehicleManager.initialize()

        if (vehicleManager.isInitialized) {
            binding.tvServerStatus.text = "PLEOS VHAL ONLINE"
            binding.tvServerStatus.setTextColor(ContextCompat.getColor(this, R.color.status_green))
        } else {
            binding.tvServerStatus.text = "STANDALONE / MOCK"
            binding.tvServerStatus.setTextColor(ContextCompat.getColor(this, R.color.hyundai_sky))
        }
    }

    private fun handleShiftToDrive(isManual: Boolean) {
        if (currentGear == "D") return

        currentGear = "D"
        isDrivingSessionActive = true
        sessionStartOdometer = currentOdometer
        if (currentSpeed == 0) currentSpeed = 45
        updateGearUI("D")
        updateSpeedUI(currentSpeed)
        updateSessionStatusUI(true)

        val timeStr = SimpleDateFormat("HH:mm", Locale.KOREA).format(Date())
        binding.tvStartAddress.text = "${locationManager.currentAddress} ($timeStr)"

        // Start location movement simulation while in DRIVE
        locationManager.startDrivingSimulation(currentSpeed)
        startTripDurationTimer()

        val triggerSource = if (isManual) "수동 버튼" else "Pleos CAN VHAL"
        Toast.makeText(this, "[$triggerSource] D단 출발 감지: 국세청 운행일지 자동 기록 시작 (Zero-Click)", Toast.LENGTH_SHORT).show()

        startBackendSession()
    }

    private fun handleShiftToPark(isManual: Boolean) {
        if (currentGear == "P") return

        currentGear = "P"
        isDrivingSessionActive = false
        currentSpeed = 0
        if (isManual) {
            currentOdometer += 24.3 // 수동 시연 시 24.3km 가산
        }

        val finalTrip = maxOf(0.0, currentOdometer - sessionStartOdometer)
        binding.tvTripDistance.text = "${String.format("%.1f", finalTrip)} km"

        // Stop simulation and timer
        locationManager.stopDrivingSimulation()
        stopTripDurationTimer()

        updateGearUI("P")
        updateSpeedUI(0)
        updateOdometerUI(currentOdometer)
        updateSessionStatusUI(false)

        val triggerSource = if (isManual) "수동 버튼" else "Pleos CAN VHAL"
        Toast.makeText(this, "[$triggerSource] P단 정차 감지: 운행 세션 종료 및 국세청 서식 저장 (도착 ODO: ${String.format("%.1f", currentOdometer)} km)", Toast.LENGTH_LONG).show()

        completeBackendSession()
    }

    private fun startTripDurationTimer() {
        tripTimerJob?.cancel()
        tripElapsedSeconds = 0L
        tripTimerJob = lifecycleScope.launch {
            while (isActive && isDrivingSessionActive) {
                delay(1000L)
                tripElapsedSeconds++
                val hours = tripElapsedSeconds / 3600
                val minutes = (tripElapsedSeconds % 3600) / 60
                val seconds = tripElapsedSeconds % 60
                binding.tvTripDuration.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            }
        }
    }

    private fun stopTripDurationTimer() {
        tripTimerJob?.cancel()
        tripTimerJob = null
    }

    private fun startBackendSession() {
        lifecycleScope.launch {
            try {
                val request = StartSessionRequest(
                    vehicleId = 1L,
                    userId = 1L,
                    startOdometer = currentOdometer,
                    startLatitude = locationManager.currentLatitude,
                    startLongitude = locationManager.currentLongitude,
                    startAddress = locationManager.currentAddress
                )
                val response = ApiClient.apiService.startSession(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    val session = response.body()?.data
                    activeSessionId = session?.id
                    Log.i(tag, "Backend session started: ID=${session?.id}")
                    startPeriodicRouteTracking(session?.id ?: 1L)
                } else {
                    Log.w(tag, "Failed to start backend session: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.w(tag, "Backend sync skipped (offline mode): ${e.message}")
            }
        }
    }

    private fun startPeriodicRouteTracking(sessionId: Long) {
        routeTrackingJob?.cancel()
        routeTrackingJob = lifecycleScope.launch {
            while (isActive && isDrivingSessionActive) {
                delay(5000) // Every 5 seconds
                if (!isDrivingSessionActive) break

                try {
                    val points = listOf(
                        RoutePointRequest(
                            latitude = locationManager.currentLatitude,
                            longitude = locationManager.currentLongitude,
                            speedKmh = currentSpeed,
                            odometerSnapshot = currentOdometer
                        )
                    )
                    ApiClient.apiService.recordRoutes(sessionId, BatchRouteRequest(points))
                    Log.d(tag, "Recorded GPS breadcrumb: lat=${locationManager.currentLatitude}, lon=${locationManager.currentLongitude} (${locationManager.currentAddress})")
                } catch (e: Exception) {
                    Log.w(tag, "Route upload skipped: ${e.message}")
                }
            }
        }
    }

    private fun completeBackendSession() {
        routeTrackingJob?.cancel()
        routeTrackingJob = null

        val sessionId = activeSessionId
        lifecycleScope.launch {
            if (sessionId != null) {
                try {
                    val request = CompleteSessionRequest(
                        endOdometer = currentOdometer,
                        endLatitude = locationManager.currentLatitude,
                        endLongitude = locationManager.currentLongitude,
                        endAddress = locationManager.currentAddress,
                        purposeType = selectedPurpose,
                        notes = "Pleos Connect Zero-Click 자동 기록"
                    )
                    val response = ApiClient.apiService.completeSession(sessionId, request)
                    if (response.isSuccessful) {
                        Log.i(tag, "Backend session #$sessionId successfully completed: ${locationManager.currentAddress} ($selectedPurpose)")
                    }
                } catch (e: Exception) {
                    Log.w(tag, "Failed to complete backend session: ${e.message}")
                } finally {
                    activeSessionId = null
                }
            }
        }
    }

    private fun toggleSpeed() {
        if (!isDrivingSessionActive && currentGear == "P") {
            Toast.makeText(this, "먼저 D단으로 변속해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        currentSpeed = when (currentSpeed) {
            0 -> 35
            35 -> 80
            80 -> 100
            else -> 0
        }
        updateSpeedUI(currentSpeed)
    }

    private fun updateGearUI(gear: String) {
        val inactiveBg = ContextCompat.getDrawable(this, R.drawable.bg_gear_badge)
        val inactiveTextColor = ContextCompat.getColor(this, R.color.gear_text_inactive)
        val activeTextColor = ContextCompat.getColor(this, R.color.white)

        binding.gearP.background = inactiveBg
        binding.gearP.setTextColor(inactiveTextColor)
        binding.gearR.background = inactiveBg
        binding.gearR.setTextColor(inactiveTextColor)
        binding.gearN.background = inactiveBg
        binding.gearN.setTextColor(inactiveTextColor)
        binding.gearD.background = inactiveBg
        binding.gearD.setTextColor(inactiveTextColor)

        when (gear) {
            "P" -> {
                binding.gearP.background = ContextCompat.getDrawable(this, R.drawable.bg_gear_active_p)
                binding.gearP.setTextColor(activeTextColor)
            }
            "D" -> {
                binding.gearD.background = ContextCompat.getDrawable(this, R.drawable.bg_gear_active_d)
                binding.gearD.setTextColor(activeTextColor)
            }
            "R" -> {
                binding.gearR.setTextColor(activeTextColor)
            }
            "N" -> {
                binding.gearN.setTextColor(activeTextColor)
            }
        }
    }

    private fun updateSpeedUI(speed: Int) {
        binding.tvCurrentSpeed.text = speed.toString()
    }

    private fun updateOdometerUI(odometer: Double) {
        binding.tvOdometerValue.text = "${String.format("%,.1f", odometer)} km"
    }

    private fun updateSessionStatusUI(isDriving: Boolean) {
        if (isDriving) {
            binding.tvSessionStatusBadge.text = getString(R.string.status_driving)
            binding.tvSessionStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_green))
        } else {
            binding.tvSessionStatusBadge.text = getString(R.string.status_idle)
            binding.tvSessionStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.hyundai_sky))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        routeTrackingJob?.cancel()
        tripTimerJob?.cancel()
        locationManager.stop()
        vehicleManager.release()
    }
}
