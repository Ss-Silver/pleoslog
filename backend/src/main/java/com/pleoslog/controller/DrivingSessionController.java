package com.pleoslog.controller;

import com.pleoslog.domain.common.ApiResponse;
import com.pleoslog.dto.session.*;
import com.pleoslog.service.DrivingSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "1. Driving Session API", description = "차량 주행 세션 라이프사이클 제어 (D단 시작 / 좌표 수집 / P단 종료)")
@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class DrivingSessionController {

    private final DrivingSessionService sessionService;

    @Operation(summary = "주행 세션 시작 (D단 진입 감지)", description = "차량 기어가 D단으로 전환되거나 출발 시 호출하여 신규 주행 세션을 생성합니다.")
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<DrivingSessionResponse>> startSession(
            @RequestBody @Valid StartSessionRequest request) {
        DrivingSessionResponse response = sessionService.startSession(request);
        return ResponseEntity.ok(ApiResponse.success("주행 세션이 정상적으로 시작되었습니다.", response));
    }

    @Operation(summary = "주행 중 GPS 좌표 배치 기록", description = "주행 중 주기적으로 수집된 GPS 위경도/속도 좌표 목록을 전송합니다.")
    @PostMapping("/{sessionId}/routes")
    public ResponseEntity<ApiResponse<String>> recordRoutes(
            @PathVariable Long sessionId,
            @RequestBody @Valid BatchRouteRequest request) {
        int count = sessionService.recordRoutes(sessionId, request);
        return ResponseEntity.ok(ApiResponse.success(String.format("GPS 좌표 %d건이 정상 기록되었습니다.", count), null));
    }

    @Operation(summary = "주행 세션 종료 (P단 정차 및 음성 확인)", description = "목적지 도착 후 P단 정차 및 음성 대화를 통해 확정된 목적과 최종 ODO를 기록하고 세션을 종료합니다.")
    @PostMapping("/{sessionId}/complete")
    public ResponseEntity<ApiResponse<DrivingSessionResponse>> completeSession(
            @PathVariable Long sessionId,
            @RequestBody @Valid CompleteSessionRequest request) {
        DrivingSessionResponse response = sessionService.completeSession(sessionId, request);
        return ResponseEntity.ok(ApiResponse.success("주행 세션이 성공적으로 완료 및 저장되었습니다.", response));
    }

    @Operation(summary = "차량별 운행기록 목록 조회", description = "특정 차량의 과거 운행 세션 이력을 최신순으로 조회합니다.")
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<ApiResponse<List<DrivingSessionResponse>>> getSessionsByVehicle(
            @PathVariable Long vehicleId) {
        List<DrivingSessionResponse> responses = sessionService.getSessionsByVehicle(vehicleId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @Operation(summary = "주행 세션 상세 및 주행 궤적 조회", description = "특정 운행 세션의 메타데이터와 주행 중 수집된 지도 GPS 궤적 좌표 목록을 조회합니다.")
    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<DrivingSessionDetailResponse>> getSessionDetail(
            @PathVariable Long sessionId) {
        DrivingSessionDetailResponse response = sessionService.getSessionDetail(sessionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
