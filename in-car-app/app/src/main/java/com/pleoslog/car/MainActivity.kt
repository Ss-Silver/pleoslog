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
    private var baseOdometer: Double = 14500.0
    private var currentOdometer: Double = 14524.3
    private var sessionStartOdometer: Double = 14500.0
    private var isDrivingSessionActive: Boolean = false
    private var activeSessionId: Long? = 1L
    private var routeTrackingJob: Job? = null

    private var sessionStartTime: Date = Date(System.currentTimeMillis() - 33 * 60 * 1000)
    private var sessionStartAddress: String = "서울 서초구 양재본사"
    private var sessionEndAddress: String = "성남시 수정구 시흥동"

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
        binding.tvStartAddr.text = "서울 서초구 양재본사 (10:15 / 14,500.0 km)"
        binding.tvRouteSummary.text = "성남시 수정구 시흥동 (10:48 / 14,524.3 km)"
        binding.tvLastTripDistance.text = "24.3 km"
        binding.tvOdometerValue.text = "${String.format("%,.1f", currentOdometer)} km"
        binding.tvSessionTimeRange.text = "오늘 10:15 ~ 10:48 (33분 주행)"
        binding.tvSessionStatusBadge.text = "국세청 제16호의2서식 자동 기록 완료 ✓"
        updateServerStatusUI(isDriving = false)
    }

    private fun setupListeners() {
        // D단 출발 시뮬레이션
        binding.btnShiftD.setOnClickListener {
            handleShiftToDrive(isManual = true)
        }

        // P단 정차 시뮬레이션
        binding.btnShiftP.setOnClickListener {
            handleShiftToPark(isManual = true)
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
        val mutedColor = ContextCompat.getColor(this, R.color.text_muted)

        // Reset all buttons to unselected
        binding.btnPurposeBusiness.background = unselectedBg
        binding.tvPurposeBusinessTitle.setTextColor(primaryColor)
        binding.tvPurposeBusinessSub.setTextColor(mutedColor)

        binding.btnPurposeCommute.background = unselectedBg
        binding.tvPurposeCommuteTitle.setTextColor(primaryColor)
        binding.tvPurposeCommuteSub.setTextColor(mutedColor)

        binding.btnPurposePersonal.background = unselectedBg
        binding.tvPurposePersonalTitle.setTextColor(primaryColor)
        binding.tvPurposePersonalSub.setTextColor(mutedColor)

        val purposeKoreanName = when (purpose) {
            "BUSINESS" -> {
                binding.btnPurposeBusiness.background = selectedBg
                binding.tvPurposeBusinessTitle.setTextColor(skyColor)
                binding.tvPurposeBusinessSub.setTextColor(skyColor)
                "일반 업무용 (100% 비용인정)"
            }
            "COMMUTE" -> {
                binding.btnPurposeCommute.background = selectedBg
                binding.tvPurposeCommuteTitle.setTextColor(skyColor)
                binding.tvPurposeCommuteSub.setTextColor(skyColor)
                "출·퇴근용 (법정 인정)"
            }
            "PERSONAL" -> {
                binding.btnPurposePersonal.background = selectedBg
                binding.tvPurposePersonalTitle.setTextColor(skyColor)
                binding.tvPurposePersonalSub.setTextColor(skyColor)
                "비업무용 (개인 용무)"
            }
            else -> purpose
        }

        Toast.makeText(this, "운행 목적이 '$purposeKoreanName'으로 국세청 서식에 반영되었습니다.", Toast.LENGTH_SHORT).show()
        updateBackendPurpose(purpose)
    }

    private fun updateBackendPurpose(purpose: String) {
        val sessionId = activeSessionId ?: 1L
        lifecycleScope.launch {
            try {
                val request = CompleteSessionRequest(
                    endOdometer = currentOdometer,
                    endAddress = sessionEndAddress,
                    purposeType = purpose,
                    notes = "운전자 정차 후 목적 확인: $purpose"
                )
                ApiClient.apiService.completeSession(sessionId, request)
                Log.i(tag, "Updated session #$sessionId purpose to $purpose in cloud.")
            } catch (e: Exception) {
                Log.w(tag, "Failed to update purpose in cloud: ${e.message}")
            }
        }
    }

    private fun initLocationManager() {
        locationManager = CarLocationManager(this).apply {
            onLocationChanged = { lat, lon, addr ->
                sessionEndAddress = addr
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
                        else -> {}
                    }
                }
            }

            onOdometerChanged = { odoKm ->
                runOnUiThread {
                    if (odoKm > 100.0) {
                        currentOdometer = odoKm
                    } else if (odoKm > 0.0) {
                        currentOdometer = baseOdometer + odoKm
                    }
                    binding.tvOdometerValue.text = "${String.format("%,.1f", currentOdometer)} km"
                }
            }

            onVinRetrieved = { vin ->
                runOnUiThread {
                    binding.tvVehicleInfo.text = "아이오닉 6 (12가 3456)"
                }
            }
        }

        vehicleManager.initialize()
    }

    private fun handleShiftToDrive(isManual: Boolean) {
        if (currentGear == "D") return

        currentGear = "D"
        isDrivingSessionActive = true
        sessionStartOdometer = currentOdometer
        sessionStartTime = Date()
        sessionStartAddress = locationManager.currentAddress

        locationManager.startDrivingSimulation(60)
        updateServerStatusUI(isDriving = true)

        val triggerSource = if (isManual) "시뮬레이션" else "Pleos VHAL"
        Toast.makeText(this, "[$triggerSource] D단 주행 감지: 화면 조작 없이 백그라운드에서 자동 기록 시작", Toast.LENGTH_SHORT).show()

        startBackendSession()
    }

    private fun handleShiftToPark(isManual: Boolean) {
        if (currentGear == "P") return

        currentGear = "P"
        isDrivingSessionActive = false
        if (isManual) {
            currentOdometer += 24.3 // 시연 시 주행거리 가산
        }

        val distance = maxOf(0.0, currentOdometer - sessionStartOdometer)
        sessionEndAddress = locationManager.currentAddress
        val endTime = Date()

        val timeFmt = SimpleDateFormat("HH:mm", Locale.KOREA)
        val startStr = timeFmt.format(sessionStartTime)
        val endStr = timeFmt.format(endTime)
        val diffMin = maxOf(1, ((endTime.time - sessionStartTime.time) / (60 * 1000)).toInt())

        // Update UI with newly completed session details
        binding.tvStartAddr.text = "$sessionStartAddress ($startStr / ${String.format("%,.1f", sessionStartOdometer)} km)"
        binding.tvRouteSummary.text = "$sessionEndAddress ($endStr / ${String.format("%,.1f", currentOdometer)} km)"
        binding.tvLastTripDistance.text = "${String.format("%.1f", distance)} km"
        binding.tvOdometerValue.text = "${String.format("%,.1f", currentOdometer)} km"
        binding.tvSessionTimeRange.text = "오늘 $startStr ~ $endStr ($diffMin 분 주행)"
        binding.tvSessionStatusBadge.text = "국세청 제16호의2서식 자동 기록 완료 ✓"

        locationManager.stopDrivingSimulation()
        updateServerStatusUI(isDriving = false)

        val triggerSource = if (isManual) "시뮬레이션" else "Pleos VHAL"
        Toast.makeText(this, "[$triggerSource] P단 정차 감지: 운행일지 자동 완료 (도착 ODO: ${String.format("%.1f", currentOdometer)} km)", Toast.LENGTH_LONG).show()

        completeBackendSession(distance)
    }

    private fun updateServerStatusUI(isDriving: Boolean) {
        if (isDriving) {
            binding.tvServerStatus.text = "🔵 백그라운드 자동 기록 중 (D단)"
            binding.tvServerStatus.setTextColor(ContextCompat.getColor(this, R.color.hyundai_sky))
        } else {
            binding.tvServerStatus.text = "🟢 자동 기록 대기 중 (정차)"
            binding.tvServerStatus.setTextColor(ContextCompat.getColor(this, R.color.status_green))
        }
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
                    startAddress = sessionStartAddress
                )
                val response = ApiClient.apiService.startSession(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    val session = response.body()?.data
                    activeSessionId = session?.id
                    Log.i(tag, "Backend session started silently: ID=${session?.id}")
                    startPeriodicRouteTracking(session?.id ?: 1L)
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
                delay(5000)
                if (!isDrivingSessionActive) break
                try {
                    val points = listOf(
                        RoutePointRequest(
                            latitude = locationManager.currentLatitude,
                            longitude = locationManager.currentLongitude,
                            speedKmh = 60,
                            odometerSnapshot = currentOdometer
                        )
                    )
                    ApiClient.apiService.recordRoutes(sessionId, BatchRouteRequest(points))
                } catch (e: Exception) {
                    Log.w(tag, "Route upload skipped: ${e.message}")
                }
            }
        }
    }

    private fun completeBackendSession(distanceKm: Double) {
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
                        endAddress = sessionEndAddress,
                        purposeType = selectedPurpose,
                        notes = "Pleos Connect Zero-Click 자동 기록"
                    )
                    val response = ApiClient.apiService.completeSession(sessionId, request)
                    if (response.isSuccessful) {
                        Log.i(tag, "Backend session #$sessionId completed in cloud: $sessionEndAddress")
                    }
                } catch (e: Exception) {
                    Log.w(tag, "Failed to complete backend session: ${e.message}")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        routeTrackingJob?.cancel()
        locationManager.stop()
        vehicleManager.release()
    }
}
