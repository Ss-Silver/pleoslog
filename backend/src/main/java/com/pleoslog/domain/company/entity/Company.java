package com.pleoslog.domain.company.entity;

import com.pleoslog.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "companies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Company extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_number", nullable = false, unique = true, length = 20)
    private String businessNumber; // 사업자등록번호 (예: 123-45-67890)

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName; // 상호 / 법인명

    @Column(name = "representative_name", nullable = false, length = 50)
    private String representativeName; // 대표자 성명
}
