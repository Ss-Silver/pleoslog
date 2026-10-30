package com.pleoslog.dto.session;

import com.pleoslog.domain.session.entity.DrivePurpose;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteSessionRequest {

    @NotNull(message = "도착 시 계기판 누적거리(ODO)는 필수입니다.")
    private Double endOdometer;

    private Double endLatitude;
    private Double endLongitude;
    private String endAddress;

    private DrivePurpose purposeType; // COMMUTE, BUSINESS, PERSONAL

    private String notes;

    private LocalDateTime endedAt;
}
