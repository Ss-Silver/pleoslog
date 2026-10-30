package com.pleoslog.car

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.pleoslog.car.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var currentGear: String = "P"
    private var currentSpeed: Int = 0
    private var currentOdometer: Double = 14500.0
    private var isDrivingSessionActive: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        updateGearUI("P")
        updateSpeedUI(0)
        updateOdometerUI(currentOdometer)
        updateSessionStatusUI(false)
    }

    private fun setupListeners() {
        // D단 전환 (출발 시뮬레이션)
        binding.btnShiftD.setOnClickListener {
            shiftToDrive()
        }

        // P단 전환 (도착 및 정차 시뮬레이션)
        binding.btnShiftP.setOnClickListener {
            shiftToPark()
        }

        // 속도 변경 시뮬레이션
        binding.btnToggleSpeed.setOnClickListener {
            toggleSpeed()
        }
    }

    private fun shiftToDrive() {
        currentGear = "D"
        isDrivingSessionActive = true
        currentSpeed = 45
        updateGearUI("D")
        updateSpeedUI(currentSpeed)
        updateSessionStatusUI(true)

        Toast.makeText(this, "D단 진입 감지: 주행 세션이 자동으로 시작되었습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun shiftToPark() {
        currentGear = "P"
        isDrivingSessionActive = false
        currentSpeed = 0
        currentOdometer += 24.3 // 시연용 주행거리 가산 (24.3km)

        updateGearUI("P")
        updateSpeedUI(0)
        updateOdometerUI(currentOdometer)
        updateSessionStatusUI(false)

        Toast.makeText(this, "P단 정차 감지: 주행 세션 완료 (도착 ODO: ${String.format("%.1f", currentOdometer)} km)", Toast.LENGTH_LONG).show()
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

        // Reset all to inactive
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
}
