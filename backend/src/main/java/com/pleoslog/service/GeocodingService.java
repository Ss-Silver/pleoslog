package com.pleoslog.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class GeocodingService {

    // 데모 및 네트워크 두절 시 즉각적인 고정밀 주소 변환을 위한 주요 랜드마크 Mock 캐시
    private static final Map<String, String> PRESET_ADDRESSES = Map.of(
            "gangnam", "서울특별시 강남구 강남대로 396 (강남역 2번 출구)",
            "pangyo", "경기도 성남시 분당구 판교역로 146번길 20 (현대백화점 판교점)",
            "yangjae", "서울특별시 서초구 헌릉로 12 (현대자동차그룹 본사)",
            "namyang", "경기도 화성시 남양읍 현대연구소로 150 (현대차 남양연구소)",
            "yeouido", "서울특별시 영등포구 여의대로 108 (더현대 서울)"
    );

    /**
     * 위도/경도 좌표를 도로명 주소 및 대표 건물명으로 변환
     */
    public String reverseGeocode(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return "위치 정보 없음";
        }

        // 강남역 일대 (37.4979, 127.0276 부근)
        if (isNear(latitude, longitude, 37.4979, 127.0276, 0.03)) {
            return PRESET_ADDRESSES.get("gangnam");
        }
        // 판교 일대 (37.3947, 127.1112 부근)
        if (isNear(latitude, longitude, 37.3947, 127.1112, 0.03)) {
            return PRESET_ADDRESSES.get("pangyo");
        }
        // 현대차 양재 본사 일대 (37.4636, 127.0426 부근)
        if (isNear(latitude, longitude, 37.4636, 127.0426, 0.03)) {
            return PRESET_ADDRESSES.get("yangjae");
        }
        // 남양연구소 일대 (37.1895, 126.8288 부근)
        if (isNear(latitude, longitude, 37.1895, 126.8288, 0.03)) {
            return PRESET_ADDRESSES.get("namyang");
        }

        // 기본 좌표 포맷팅
        return String.format("대한민국 (위도: %.4f, 경도: %.4f)", latitude, longitude);
    }

    private boolean isNear(double lat1, double lng1, double lat2, double lng2, double threshold) {
        return Math.abs(lat1 - lat2) <= threshold && Math.abs(lng1 - lng2) <= threshold;
    }
}
