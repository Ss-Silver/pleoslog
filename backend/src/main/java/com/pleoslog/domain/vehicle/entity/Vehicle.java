package com.pleoslog.domain.vehicle.entity;

import com.pleoslog.domain.common.BaseTimeEntity;
import com.pleoslog.domain.company.entity.Company;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Vehicle extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "vehicle_number", nullable = false, unique = true, length = 20)
    private String vehicleNumber; // 차량번호 (예: 12가 3456)

    @Column(name = "model_name", nullable = false, length = 50)
    private String modelName; // 차종 (예: 아이오닉 6)

    @Column(name = "total_odometer", nullable = false)
    private Double totalOdometer; // 계기판 누적거리 (km 단위)

    @Column(name = "pairing_code", length = 6)
    private String pairingCode; // 인카 앱-모바일 페어링용 6자리 핀코드

    public void updateOdometer(Double newOdometer) {
        if (newOdometer != null && newOdometer > this.totalOdometer) {
            this.totalOdometer = newOdometer;
        }
    }

    public void updatePairingCode(String code) {
        this.pairingCode = code;
    }
}
