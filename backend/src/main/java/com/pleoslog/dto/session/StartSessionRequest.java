package com.pleoslog.dto.session;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartSessionRequest {

    @NotNull(message = "차량 ID는 필수입니다.")
    private Long vehicleId;

    private Long userId;

    @NotNull(message = "출발 시 계기판 누적거리(ODO)는 필수입니다.")
    private Double startOdometer;

    private Double startLatitude;
    private Double startLongitude;
    private String startAddress;

    private LocalDateTime startedAt;
}
