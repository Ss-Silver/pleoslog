package com.pleoslog.domain.session.entity;

import com.pleoslog.domain.common.BaseTimeEntity;
import com.pleoslog.domain.user.entity.User;
import com.pleoslog.domain.vehicle.entity.Vehicle;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "driving_sessions", indexes = {
        @Index(name = "idx_session_vehicle_date", columnList = "vehicle_id, started_at"),
        @Index(name = "idx_session_status", columnList = "session_status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DrivingSession extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_status", nullable = false, length = 20)
    private SessionStatus sessionStatus;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "start_odometer", nullable = false)
    private Double startOdometer; // 출발 시 계기판 누적거리 (km)

    @Column(name = "end_odometer")
    private Double endOdometer; // 도착 시 계기판 누적거리 (km)

    @Column(name = "driven_distance")
    private Double drivenDistance; // 주행거리 = endOdometer - startOdometer (km)

    @Column(name = "start_latitude")
    private Double startLatitude;

    @Column(name = "start_longitude")
    private Double startLongitude;

    @Column(name = "start_address", length = 200)
    private String startAddress; // 출발지 도로명 주소 (대표 건물명 포함)

    @Column(name = "end_latitude")
    private Double endLatitude;

    @Column(name = "end_longitude")
    private Double endLongitude;

    @Column(name = "end_address", length = 200)
    private String endAddress; // 도착지 도로명 주소 (대표 건물명 포함)

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose_type", length = 20)
    private DrivePurpose purposeType; // 음성 대화 또는 기본값으로 확정된 목적

    @Column(name = "notes", length = 255)
    private String notes; // 비고

    public void completeSession(LocalDateTime endedAt, Double endOdometer, Double endLat, Double endLng,
                                String endAddress, DrivePurpose purpose, String notes) {
        this.sessionStatus = SessionStatus.COMPLETED;
        this.endedAt = endedAt != null ? endedAt : LocalDateTime.now();
        this.endOdometer = endOdometer;
        if (this.startOdometer != null && endOdometer != null) {
            double distance = endOdometer - this.startOdometer;
            this.drivenDistance = Math.round(Math.max(0.0, distance) * 10.0) / 10.0;
        }
        this.endLatitude = endLat;
        this.endLongitude = endLng;
        this.endAddress = endAddress;
        this.purposeType = purpose != null ? purpose : DrivePurpose.BUSINESS;
        this.notes = notes;
    }

    public void cancelSession(String reason) {
        this.sessionStatus = SessionStatus.CANCELLED;
        this.endedAt = LocalDateTime.now();
        this.notes = reason;
    }
}
