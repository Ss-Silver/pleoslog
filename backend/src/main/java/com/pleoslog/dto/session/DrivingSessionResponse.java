package com.pleoslog.dto.session;

import com.pleoslog.domain.session.entity.DrivePurpose;
import com.pleoslog.domain.session.entity.DrivingSession;
import com.pleoslog.domain.session.entity.SessionStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
public class DrivingSessionResponse {

    private Long sessionId;
    private Long vehicleId;
    private String vehicleNumber;
    private String modelName;
    private String userName;
    private String department;
    private SessionStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Double startOdometer;
    private Double endOdometer;
    private Double drivenDistance;
    private String startAddress;
    private String endAddress;
    private DrivePurpose purposeType;
    private String purposeDescription;
    private String notes;

    public static DrivingSessionResponse from(DrivingSession session) {
        return DrivingSessionResponse.builder()
                .sessionId(session.getId())
                .vehicleId(session.getVehicle().getId())
                .vehicleNumber(session.getVehicle().getVehicleNumber())
                .modelName(session.getVehicle().getModelName())
                .userName(session.getUser() != null ? session.getUser().getName() : "-")
                .department(session.getUser() != null ? session.getUser().getDepartment() : "-")
                .status(session.getSessionStatus())
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .startOdometer(session.getStartOdometer())
                .endOdometer(session.getEndOdometer())
                .drivenDistance(session.getDrivenDistance())
                .startAddress(session.getStartAddress())
                .endAddress(session.getEndAddress())
                .purposeType(session.getPurposeType())
                .purposeDescription(session.getPurposeType() != null ? session.getPurposeType().getDescription() : "-")
                .notes(session.getNotes())
                .build();
    }
}
