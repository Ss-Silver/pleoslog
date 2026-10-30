package com.pleoslog.controller;

import com.pleoslog.domain.common.ApiResponse;
import com.pleoslog.domain.vehicle.entity.Vehicle;
import com.pleoslog.domain.vehicle.repository.VehicleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "3. Vehicle & Pairing API", description = "차량 정보 조회 및 6자리 핀코드 기반 모바일 페어링")
@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleRepository vehicleRepository;

    @Getter
    @Builder
    public static class VehicleResponse {
        private Long id;
        private String vehicleNumber;
        private String modelName;
        private Double totalOdometer;
        private String pairingCode;
        private String companyName;

        public static VehicleResponse from(Vehicle v) {
            return VehicleResponse.builder()
                    .id(v.getId())
                    .vehicleNumber(v.getVehicleNumber())
                    .modelName(v.getModelName())
                    .totalOdometer(v.getTotalOdometer())
                    .pairingCode(v.getPairingCode())
                    .companyName(v.getCompany() != null ? v.getCompany().getCompanyName() : "-")
                    .build();
        }
    }

    @Operation(summary = "등록된 전체 차량 목록 조회", description = "시스템에 등록된 차량 목록 및 현재 ODO 계기판 거리를 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getAllVehicles() {
        List<VehicleResponse> list = vehicleRepository.findAll().stream()
                .map(VehicleResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "차량 단건 상세 조회", description = "차량 ID로 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicle(@PathVariable Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 차량 ID: " + id));
        return ResponseEntity.ok(ApiResponse.success(VehicleResponse.from(vehicle)));
    }

    @Operation(summary = "6자리 핀코드로 차량 조회 (페어링용)", description = "인카 앱 화면에 표출된 6자리 핀코드를 입력하여 차량을 찾아 페어링합니다.")
    @GetMapping("/pair/{pairingCode}")
    public ResponseEntity<ApiResponse<VehicleResponse>> pairVehicle(@PathVariable String pairingCode) {
        Vehicle vehicle = vehicleRepository.findByPairingCode(pairingCode)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 페어링 코드입니다: " + pairingCode));
        return ResponseEntity.ok(ApiResponse.success("차량 페어링에 성공했습니다.", VehicleResponse.from(vehicle)));
    }
}
