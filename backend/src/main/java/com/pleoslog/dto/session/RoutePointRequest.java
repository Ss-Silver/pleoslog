package com.pleoslog.dto.session;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutePointRequest {

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    private Double speed;

    private LocalDateTime recordedAt;
}
