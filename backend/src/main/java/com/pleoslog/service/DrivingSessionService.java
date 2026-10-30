package com.pleoslog.service;

import com.pleoslog.domain.session.entity.DriveRoute;
import com.pleoslog.domain.session.entity.DrivingSession;
import com.pleoslog.domain.session.entity.SessionStatus;
import com.pleoslog.domain.session.repository.DriveRouteRepository;
import com.pleoslog.domain.session.repository.DrivingSessionRepository;
import com.pleoslog.domain.user.entity.User;
import com.pleoslog.domain.user.repository.UserRepository;
import com.pleoslog.domain.vehicle.entity.Vehicle;
import com.pleoslog.domain.vehicle.repository.VehicleRepository;
import com.pleoslog.dto.session.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DrivingSessionService {

    private final DrivingSessionRepository sessionRepository;
    private final DriveRouteRepository routeRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final GeocodingService geocodingService;

    /**
     * D단 진입 감지: 신규 주행 세션 시작
     */
    @Transactional
    public DrivingSessionResponse startSession(StartSessionRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 차량 ID: " + request.getVehicleId()));

        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId()).orElse(null);
        }

        // 이미 진행 중인 세션이 있는지 확인 (있으면 기존 것 리턴 또는 마무리)
        var ongoing = sessionRepository.findFirstByVehicleIdAndSessionStatusOrderByStartedAtDesc(
                vehicle.getId(), SessionStatus.DRIVING
        );
        if (ongoing.isPresent()) {
            log.info("이미 진행 중인 세션 존재 (ID: {}). 해당 세션을 반환합니다.", ongoing.get().getId());
            return DrivingSessionResponse.from(ongoing.get());
        }

        String startAddress = request.getStartAddress();
        if (startAddress == null || startAddress.isBlank()) {
            startAddress = geocodingService.reverseGeocode(request.getStartLatitude(), request.getStartLongitude());
        }

        Double startOdo = request.getStartOdometer();
        if (startOdo == null || startOdo <= 0) {
            startOdo = vehicle.getTotalOdometer();
        }

        DrivingSession session = DrivingSession.builder()
                .vehicle(vehicle)
                .user(user)
                .sessionStatus(SessionStatus.DRIVING)
                .startedAt(request.getStartedAt() != null ? request.getStartedAt() : LocalDateTime.now())
                .startOdometer(startOdo)
                .startLatitude(request.getStartLatitude())
                .startLongitude(request.getStartLongitude())
                .startAddress(startAddress)
                .build();

        DrivingSession saved = sessionRepository.save(session);
        log.info("새 주행 세션 시작 (ID: {}, 차량: {}, 출발 ODO: {} km)", saved.getId(), vehicle.getVehicleNumber(), startOdo);
        return DrivingSessionResponse.from(saved);
    }

    /**
     * 주행 중 GPS 좌표 배치 수집
     */
    @Transactional
    public int recordRoutes(Long sessionId, BatchRouteRequest request) {
        DrivingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션 ID: " + sessionId));

        List<DriveRoute> routes = request.getPoints().stream()
                .map(p -> DriveRoute.builder()
                        .session(session)
                        .latitude(p.getLatitude())
                        .longitude(p.getLongitude())
                        .speed(p.getSpeed())
                        .recordedAt(p.getRecordedAt() != null ? p.getRecordedAt() : LocalDateTime.now())
                        .build())
                .toList();

        routeRepository.saveAll(routes);
        return routes.size();
    }

    /**
     * P단 정차 및 음성 확인: 세션 종료
     */
    @Transactional
    public DrivingSessionResponse completeSession(Long sessionId, CompleteSessionRequest request) {
        DrivingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션 ID: " + sessionId));

        String endAddress = request.getEndAddress();
        if (endAddress == null || endAddress.isBlank()) {
            endAddress = geocodingService.reverseGeocode(request.getEndLatitude(), request.getEndLongitude());
        }

        session.completeSession(
                request.getEndedAt() != null ? request.getEndedAt() : LocalDateTime.now(),
                request.getEndOdometer(),
                request.getEndLatitude(),
                request.getEndLongitude(),
                endAddress,
                request.getPurposeType(),
                request.getNotes()
        );

        // 차량의 최종 누적 ODO 업데이트
        Vehicle vehicle = session.getVehicle();
        vehicle.updateOdometer(request.getEndOdometer());

        log.info("주행 세션 완료 (ID: {}, 주행거리: {} km, 목적: {})",
                session.getId(), session.getDrivenDistance(), session.getPurposeType());

        return DrivingSessionResponse.from(session);
    }

    /**
     * 특정 차량의 운행기록 목록 조회
     */
    public List<DrivingSessionResponse> getSessionsByVehicle(Long vehicleId) {
        return sessionRepository.findByVehicleIdOrderByStartedAtDesc(vehicleId).stream()
                .map(DrivingSessionResponse::from)
                .toList();
    }

    /**
     * 특정 주행 세션 상세 및 주행 궤적 조회
     */
    public DrivingSessionDetailResponse getSessionDetail(Long sessionId) {
        DrivingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션 ID: " + sessionId));
        List<DriveRoute> routes = routeRepository.findBySessionIdOrderByRecordedAtAsc(sessionId);
        return DrivingSessionDetailResponse.of(session, routes);
    }
}
