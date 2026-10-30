package com.pleoslog.car.vehicle

import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.api.CarInfo
import ai.pleos.playground.vehicle.api.Gear
import ai.pleos.playground.vehicle.api.Odometer
import ai.pleos.playground.vehicle.constant.control.VehicleGear
import ai.pleos.playground.vehicle.listener.DistanceDrivenListener
import ai.pleos.playground.vehicle.listener.GearSelectionListener
import android.content.Context
import android.util.Log

class PleosVehicleManager(private val context: Context) {

    private val tag = "PleosVehicleManager"

    private var vehicle: Vehicle? = null
    private var gearApi: Gear? = null
    private var odoApi: Odometer? = null
    private var carInfoApi: CarInfo? = null

    // Callbacks for UI and Session Tracking
    var onGearChanged: ((VehicleGear) -> Unit)? = null
    var onOdometerChanged: ((Double) -> Unit)? = null // in kilometers
    var onVinRetrieved: ((String) -> Unit)? = null

    private var gearListener: GearSelectionListener? = null
    private var distanceListener: DistanceDrivenListener? = null

    var isInitialized: Boolean = false
        private set

    fun initialize() {
        try {
            Log.d(tag, "Connecting to Pleos Connect Vehicle SDK...")
            val v = Vehicle(context)
            v.initialize()
            vehicle = v

            gearApi = v.getGear()
            odoApi = v.getOdometer()
            carInfoApi = v.getCarInfo()

            setupListeners()
            queryInitialVehicleState()

            isInitialized = true
            Log.i(tag, "Pleos Vehicle SDK successfully connected and initialized.")
        } catch (e: Throwable) {
            Log.w(tag, "Pleos Vehicle SDK initialization failed (running in fallback/standalone mode): ${e.message}")
            isInitialized = false
        }
    }

    private fun setupListeners() {
        try {
            gearListener = object : GearSelectionListener {
                override fun onSelected(gear: VehicleGear?) {
                    gear?.let {
                        Log.d(tag, "Vehicle Gear changed via Pleos SDK: $it")
                        onGearChanged?.invoke(it)
                    }
                }

                override fun onFailed(e: Exception) {
                    Log.w(tag, "Gear listener error: ${e.message}")
                }
            }
            gearListener?.let { gearApi?.registerGearSelection(it) }

            distanceListener = object : DistanceDrivenListener {
                override fun onDistanceDrivenUpdated(value: Float?) {
                    value?.let { meters ->
                        val kilometers = meters.toDouble() / 1000.0
                        Log.d(tag, "Vehicle Distance Driven updated: ${kilometers}km ($meters m)")
                        onOdometerChanged?.invoke(kilometers)
                    }
                }

                override fun onFailed(e: Exception) {
                    Log.w(tag, "Distance listener error: ${e.message}")
                }
            }
            distanceListener?.let { odoApi?.registerDistanceDriven(it) }

        } catch (e: Throwable) {
            Log.w(tag, "Failed to register Pleos listeners: ${e.message}")
        }
    }

    private fun queryInitialVehicleState() {
        try {
            gearApi?.getCurrentGearSelectionState(
                onComplete = { gear ->
                    gear?.let {
                        Log.i(tag, "Initial vehicle gear: $it")
                        onGearChanged?.invoke(it)
                    }
                },
                onFailed = { e ->
                    Log.w(tag, "Failed to query initial gear: ${e.message}")
                }
            )

            carInfoApi?.getVINInfo(
                onComplete = { vin ->
                    vin?.let {
                        Log.i(tag, "Vehicle VIN: $it")
                        onVinRetrieved?.invoke(it)
                    }
                },
                onFailed = { e ->
                    Log.w(tag, "Failed to retrieve VIN: ${e.message}")
                }
            )
        } catch (e: Throwable) {
            Log.w(tag, "Failed to query initial vehicle properties: ${e.message}")
        }
    }

    fun release() {
        try {
            gearListener?.let { gearApi?.unregisterGearSelection(it) }
            distanceListener?.let { odoApi?.unregisterDistanceDriven(it) }
            vehicle?.release()
            Log.i(tag, "Pleos Vehicle SDK released successfully.")
        } catch (e: Throwable) {
            Log.w(tag, "Error releasing Pleos Vehicle SDK: ${e.message}")
        } finally {
            vehicle = null
            gearApi = null
            odoApi = null
            carInfoApi = null
            isInitialized = false
        }
    }
}
