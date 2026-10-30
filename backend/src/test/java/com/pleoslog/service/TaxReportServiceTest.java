package com.pleoslog.service;

import com.pleoslog.domain.company.entity.Company;
import com.pleoslog.domain.session.entity.DrivePurpose;
import com.pleoslog.domain.session.entity.DrivingSession;
import com.pleoslog.domain.session.entity.SessionStatus;
import com.pleoslog.domain.session.repository.DrivingSessionRepository;
import com.pleoslog.domain.user.entity.User;
import com.pleoslog.domain.user.entity.UserRole;
import com.pleoslog.domain.vehicle.entity.Vehicle;
import com.pleoslog.domain.vehicle.repository.VehicleRepository;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TaxReportServiceTest {

    @Mock
    private DrivingSessionRepository sessionRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private TaxReportService taxReportService;

    @Test
    @DisplayName("국세청 법정 운행기록부(제16호의2서식) 엑셀이 정상적으로 생성된다")
    void generateTaxReportExcel_success() throws IOException {
        // given
        Company company = Company.builder()
                .id(1L)
                .companyName("(주)플레오스")
                .businessNumber("123-81-20260")
                .representativeName("김현대")
                .build();

        Vehicle vehicle = Vehicle.builder()
                .id(1L)
                .company(company)
                .vehicleNumber("12가 3456")
                .modelName("아이오닉 6")
                .totalOdometer(14500.0)
                .build();

        User user = User.builder()
                .id(1L)
                .company(company)
                .name("홍길동")
                .department("자율주행팀")
                .role(UserRole.DRIVER)
                .build();

        DrivingSession session = DrivingSession.builder()
                .id(1L)
                .vehicle(vehicle)
                .user(user)
                .sessionStatus(SessionStatus.COMPLETED)
                .startedAt(LocalDateTime.of(2026, 10, 25, 9, 0))
                .endedAt(LocalDateTime.of(2026, 10, 25, 10, 0))
                .startOdometer(14400.0)
                .endOdometer(14424.3)
                .drivenDistance(24.3)
                .startAddress("강남역")
                .endAddress("판교현백")
                .purposeType(DrivePurpose.BUSINESS)
                .build();

        given(vehicleRepository.findById(1L)).willReturn(Optional.of(vehicle));
        given(sessionRepository.findCompletedSessionsByVehicleAndDateRange(eq(1L), any(), any()))
                .willReturn(List.of(session));

        // when
        byte[] excelBytes = taxReportService.generateTaxReportExcel(1L, 2026);

        // then
        assertThat(excelBytes).isNotNull();
        assertThat(excelBytes.length).isGreaterThan(0);

        // POI Workbook으로 읽어서 서식 검증
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
            Sheet sheet = workbook.getSheet("업무용승용차 운행기록부");
            assertThat(sheet).isNotNull();
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("업무용승용차 운행기록부");
        }
    }
}
