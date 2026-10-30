package com.pleoslog.domain.session.entity;

public enum SessionStatus {
    DRIVING,    // 주행 중 (D단 진입 후 트래킹 중)
    COMPLETED,  // 정상 종료 (P단 정차 후 세션 저장 완료)
    CANCELLED   // 취소됨 (500m 미만 단순 이동 또는 취소)
}
