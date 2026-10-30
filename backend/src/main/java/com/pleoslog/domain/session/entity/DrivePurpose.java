package com.pleoslog.domain.session.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DrivePurpose {
    COMMUTE("출·퇴근용"),
    BUSINESS("일반 업무용"),
    PERSONAL("비업무용");

    private final String description;
}
