package com.pleoslog.controller;

import com.pleoslog.domain.common.ApiResponse;
import com.pleoslog.domain.company.repository.CompanyRepository;
import com.pleoslog.domain.session.repository.DriveRouteRepository;
import com.pleoslog.domain.session.repository.DrivingSessionRepository;
import com.pleoslog.domain.user.repository.UserRepository;
import com.pleoslog.domain.vehicle.repository.VehicleRepository;
import com.pleoslog.initializer.DataInitializer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "4. Demo & Presentation Utility API", description = "발표 및 시연용 원클릭 DB 리셋 및 샘플 데이터 주입")
@RestController
@RequestMapping("/api/v1/demo")
@RequiredArgsConstructor
public class DemoController {

    private final DriveRouteRepository routeRepository;
    private final DrivingSessionRepository sessionRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final DataInitializer dataInitializer;

    @Operation(summary = "시연용 DB 리셋 및 초기화", description = "기존 세션 및 데이터를 모두 삭제하고, 발표 시연에 최적화된 초기 상태로 데이터를 재주입합니다.")
    @PostMapping("/reset-seed")
    @Transactional
    public ResponseEntity<ApiResponse<String>> resetAndSeed() {
        routeRepository.deleteAllInBatch();
        sessionRepository.deleteAllInBatch();
        vehicleRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        companyRepository.deleteAllInBatch();

        dataInitializer.seedData();

        return ResponseEntity.ok(ApiResponse.success("시연용 DB가 성공적으로 초기화 및 재시딩되었습니다.", null));
    }
}
