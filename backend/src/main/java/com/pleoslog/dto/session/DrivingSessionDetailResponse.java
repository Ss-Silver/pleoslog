package com.pleoslog.dto.session;

import com.pleoslog.domain.session.entity.DriveRoute;
import com.pleoslog.domain.session.entity.DrivingSession;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DrivingSessionDetailResponse {

    private DrivingSessionResponse session;
    private List<RoutePointResponse> routePoints;

    @Getter
    @Builder
    public static class RoutePointResponse {
        private Long id;
        private Double latitude;
        private Double longitude;
        private Double speed;
        private java.time.LocalDateTime recordedAt;

        public static RoutePointResponse from(DriveRoute route) {
            return RoutePointResponse.builder()
                    .id(route.getId())
                    .latitude(route.getLatitude())
                    .longitude(route.getLongitude())
                    .speed(route.getSpeed())
                    .recordedAt(route.getRecordedAt())
                    .build();
        }
    }

    public static DrivingSessionDetailResponse of(DrivingSession session, List<DriveRoute> routes) {
        return DrivingSessionDetailResponse.builder()
                .session(DrivingSessionResponse.from(session))
                .routePoints(routes.stream().map(RoutePointResponse::from).toList())
                .build();
    }
}
