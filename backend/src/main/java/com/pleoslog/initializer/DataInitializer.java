package com.pleoslog.initializer;

import com.pleoslog.domain.company.entity.Company;
import com.pleoslog.domain.company.repository.CompanyRepository;
import com.pleoslog.domain.session.entity.DrivePurpose;
import com.pleoslog.domain.session.entity.DrivingSession;
import com.pleoslog.domain.session.entity.SessionStatus;
import com.pleoslog.domain.session.repository.DriveRouteRepository;
import com.pleoslog.domain.session.repository.DrivingSessionRepository;
import com.pleoslog.domain.user.entity.User;
import com.pleoslog.domain.user.entity.UserRole;
import com.pleoslog.domain.user.repository.UserRepository;
import com.pleoslog.domain.vehicle.entity.Vehicle;
import com.pleoslog.domain.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final DrivingSessionRepository sessionRepository;
    private final DriveRouteRepository routeRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (companyRepository.count() == 0) {
            log.info("기본 시연용 데이터가 없어 자동 시딩(Seed)을 시작합니다...");
            seedData();
        }
    }

    @Transactional
    public void seedData() {
        // 1. 법인 등록
        Company company = Company.builder()
                .businessNumber("123-81-20260")
                .companyName("(주)플레오스 테크놀로지")
                .representativeName("김현대")
                .build();
        companyRepository.save(company);

        // 2. 사용자 등록
        User driver1 = User.builder()
                .company(company)
                .name("홍길동")
                .email("driver1@pleoslog.com")
                .department("자율주행선행개발팀")
                .role(UserRole.DRIVER)
                .build();
        User driver2 = User.builder()
                .company(company)
                .name("이몽룡")
                .email("driver2@pleoslog.com")
                .department("사업기획팀")
                .role(UserRole.ADMIN)
                .build();
        userRepository.save(driver1);
        userRepository.save(driver2);

        // 3. 차량 등록
        Vehicle car1 = Vehicle.builder()
                .company(company)
                .vehicleNumber("12가 3456")
                .modelName("아이오닉 6 (IONIQ 6)")
                .totalOdometer(14500.0)
                .pairingCode("729104")
                .build();
        Vehicle car2 = Vehicle.builder()
                .company(company)
                .vehicleNumber("58나 7890")
                .modelName("제네시스 GV80")
                .totalOdometer(32100.0)
                .pairingCode("831920")
                .build();
        vehicleRepository.save(car1);
        vehicleRepository.save(car2);

        // 4. 시연용 과거 법정 운행기록 샘플 데이터 등록 (2026년 기준)
        createPastSession(car1, driver1,
                LocalDateTime.of(2026, 10, 20, 8, 30),
                LocalDateTime.of(2026, 10, 20, 9, 15),
                14420.0, 14442.5,
                "서울특별시 송파구 잠실동 (자택)",
                "서울특별시 서초구 헌릉로 12 (현대자동차 양재 본사)",
                DrivePurpose.COMMUTE, "출근 운행");

        createPastSession(car1, driver1,
                LocalDateTime.of(2026, 10, 20, 14, 0),
                LocalDateTime.of(2026, 10, 20, 15, 10),
                14442.5, 14478.2,
                "서울특별시 서초구 헌릉로 12 (양재 본사)",
                "경기도 화성시 남양읍 현대연구소로 150 (남양연구소)",
                DrivePurpose.BUSINESS, "ADAS 기술 협력 미팅");

        createPastSession(car1, driver1,
                LocalDateTime.of(2026, 10, 21, 10, 15),
                LocalDateTime.of(2026, 10, 21, 11, 0),
                14478.2, 14500.0,
                "경기도 화성시 남양읍 현대연구소로 150",
                "경기도 성남시 분당구 판교역로 146번길 20 (현대백화점 판교점)",
                DrivePurpose.BUSINESS, "공모전 파트너사 세미나");

        log.info("시연용 데이터 시딩 완료: 법인 1개, 사용자 2명, 차량 2대, 과거 주행세션 3건");
    }

    private void createPastSession(Vehicle vehicle, User user,
                                   LocalDateTime start, LocalDateTime end,
                                   double startOdo, double endOdo,
                                   String startAddr, String endAddr,
                                   DrivePurpose purpose, String notes) {
        double dist = Math.round((endOdo - startOdo) * 10.0) / 10.0;
        DrivingSession s = DrivingSession.builder()
                .vehicle(vehicle)
                .user(user)
                .sessionStatus(SessionStatus.COMPLETED)
                .startedAt(start)
                .endedAt(end)
                .startOdometer(startOdo)
                .endOdometer(endOdo)
                .drivenDistance(dist)
                .startAddress(startAddr)
                .endAddress(endAddr)
                .purposeType(purpose)
                .notes(notes)
                .build();
        sessionRepository.save(s);
    }
}
