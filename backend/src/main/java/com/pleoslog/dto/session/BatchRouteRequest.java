package com.pleoslog.dto.session;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchRouteRequest {

    @NotEmpty(message = "좌표 목록은 비어 있을 수 없습니다.")
    @Valid
    private List<RoutePointRequest> points;
}
