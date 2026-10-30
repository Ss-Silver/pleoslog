package com.pleoslog.car.data.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: T?
)

data class StartSessionRequest(
    @SerializedName("vehicleId") val vehicleId: Long,
    @SerializedName("userId") val userId: Long? = 1L,
    @SerializedName("startOdometer") val startOdometer: Double,
    @SerializedName("startLatitude") val startLatitude: Double? = 37.5665,
    @SerializedName("startLongitude") val startLongitude: Double? = 126.9780,
    @SerializedName("startAddress") val startAddress: String? = "서울 서초구 현대자동차 양재본사"
)

data class CompleteSessionRequest(
    @SerializedName("endOdometer") val endOdometer: Double,
    @SerializedName("endLatitude") val endLatitude: Double? = 37.5172,
    @SerializedName("endLongitude") val endLongitude: Double? = 127.0473,
    @SerializedName("endAddress") val endAddress: String? = "서울 강남구 역삼동 고객사 미팅",
    @SerializedName("purposeType") val purposeType: String = "BUSINESS",
    @SerializedName("notes") val notes: String? = "국세청 업무용 차량 운행일지 자동 기록 (Pleos Connect)"
)

data class RoutePointRequest(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("speedKmh") val speedKmh: Int? = 0,
    @SerializedName("odometerSnapshot") val odometerSnapshot: Double? = null
)

data class BatchRouteRequest(
    @SerializedName("points") val points: List<RoutePointRequest>
)

data class DrivingSessionResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("vehicleId") val vehicleId: Long,
    @SerializedName("vehicleNumber") val vehicleNumber: String?,
    @SerializedName("driverName") val driverName: String?,
    @SerializedName("startOdometer") val startOdometer: Double,
    @SerializedName("endOdometer") val endOdometer: Double?,
    @SerializedName("drivingDistance") val drivingDistance: Double?,
    @SerializedName("startAddress") val startAddress: String?,
    @SerializedName("endAddress") val endAddress: String?,
    @SerializedName("purposeType") val purposeType: String?,
    @SerializedName("status") val status: String
)
