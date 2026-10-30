package com.pleoslog.domain.session.repository;

import com.pleoslog.domain.session.entity.DrivingSession;
import com.pleoslog.domain.session.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DrivingSessionRepository extends JpaRepository<DrivingSession, Long> {

    // 현재 진행 중인 주행 세션 찾기 (차량 기준)
    Optional<DrivingSession> findFirstByVehicleIdAndSessionStatusOrderByStartedAtDesc(Long vehicleId, SessionStatus status);

    // 차량별 특정 기간 운행기록 조회 (국세청 서식용 및 대시보드용)
    @Query("SELECT s FROM DrivingSession s " +
           "JOIN FETCH s.vehicle v " +
           "LEFT JOIN FETCH s.user u " +
           "WHERE s.vehicle.id = :vehicleId " +
           "AND s.sessionStatus = 'COMPLETED' " +
           "AND s.startedAt >= :startDate AND s.startedAt <= :endDate " +
           "ORDER BY s.startedAt ASC")
    List<DrivingSession> findCompletedSessionsByVehicleAndDateRange(
            @Param("vehicleId") Long vehicleId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // 차량별 최신 운행기록 목록 조회
    List<DrivingSession> findByVehicleIdOrderByStartedAtDesc(Long vehicleId);
}
