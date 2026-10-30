package com.pleoslog.service;

import com.pleoslog.domain.company.entity.Company;
import com.pleoslog.domain.session.entity.DrivePurpose;
import com.pleoslog.domain.session.entity.DrivingSession;
import com.pleoslog.domain.session.repository.DrivingSessionRepository;
import com.pleoslog.domain.vehicle.entity.Vehicle;
import com.pleoslog.domain.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaxReportService {

    private final DrivingSessionRepository sessionRepository;
    private final VehicleRepository vehicleRepository;

    /**
     * 국세청 업무용승용차 운행기록방법에 관한 고시 [별지 제16호의2서식] 엑셀 바이트 배열 생성
     */
    public byte[] generateTaxReportExcel(Long vehicleId, int year) throws IOException {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("차량 정보를 찾을 수 없습니다: " + vehicleId));
        Company company = vehicle.getCompany();

        LocalDateTime startOfYear = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime endOfYear = LocalDate.of(year, 12, 31).atTime(23, 59, 59);

        List<DrivingSession> sessions = sessionRepository.findCompletedSessionsByVehicleAndDateRange(
                vehicleId, startOfYear, endOfYear
        );

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("업무용승용차 운행기록부");
            sheet.setDisplayGridlines(true);

            // 셀 스타일 정의
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle subTitleStyle = createSubTitleStyle(workbook);
            CellStyle metaLabelStyle = createHeaderStyle(workbook, IndexedColors.GREY_25_PERCENT.getIndex());
            CellStyle metaValueStyle = createValueStyle(workbook);
            CellStyle tableHeaderStyle = createHeaderStyle(workbook, IndexedColors.PALE_BLUE.getIndex());
            CellStyle dataStyle = createDataStyle(workbook, HorizontalAlignment.CENTER);
            CellStyle dataRightStyle = createDataStyle(workbook, HorizontalAlignment.RIGHT);
            CellStyle summaryStyle = createSummaryStyle(workbook);

            int rowIdx = 0;

            // 1. 대제목
            Row titleRow = sheet.createRow(rowIdx++);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("■ 법인세법 시행규칙 [별지 제16호의2서식]");
            titleCell.setCellStyle(subTitleStyle);

            Row mainTitleRow = sheet.createRow(rowIdx++);
            mainTitleRow.setHeightInPoints(40);
            Cell mainTitleCell = mainTitleRow.createCell(0);
            mainTitleCell.setCellValue("업무용승용차 운행기록부");
            mainTitleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 8));

            rowIdx++; // 여백 행

            // 2. 기본 정보 영역 (상호, 사업자번호, 차종, 차량번호, 과세기간)
            Row metaRow1 = sheet.createRow(rowIdx++);
            createMetaCell(metaRow1, 0, "① 과세기간", metaLabelStyle);
            createMetaCell(metaRow1, 1, String.format("%d.01.01 ~ %d.12.31", year, year), metaValueStyle);
            createMetaCell(metaRow1, 4, "② 상호(법인명)", metaLabelStyle);
            createMetaCell(metaRow1, 5, company != null ? company.getCompanyName() : "-", metaValueStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 1, 3));
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 5, 8));

            Row metaRow2 = sheet.createRow(rowIdx++);
            createMetaCell(metaRow2, 0, "③ 사업자등록번호", metaLabelStyle);
            createMetaCell(metaRow2, 1, company != null ? company.getBusinessNumber() : "-", metaValueStyle);
            createMetaCell(metaRow2, 4, "④ 차종 / ⑤ 차량번호", metaLabelStyle);
            createMetaCell(metaRow2, 5, String.format("%s / %s", vehicle.getModelName(), vehicle.getVehicleNumber()), metaValueStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 1, 3));
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 5, 8));

            rowIdx++; // 여백 행

            // 3. 운행 내역 테이블 헤더 (2개 행 병합 구조)
            Row h1 = sheet.createRow(rowIdx++);
            Row h2 = sheet.createRow(rowIdx++);
            h1.setHeightInPoints(24);
            h2.setHeightInPoints(24);

            String[] headers1 = {"사용일자", "사용자", "운행 내역", "", "", "업무용 사용거리(km)", "", "비업무용(km)", "비고 (경로)"};
            for (int i = 0; i < headers1.length; i++) {
                Cell c1 = h1.createCell(i);
                c1.setCellValue(headers1[i]);
                c1.setCellStyle(tableHeaderStyle);

                Cell c2 = h2.createCell(i);
                c2.setCellStyle(tableHeaderStyle);
            }

            // 서브 헤더 (h2)
            h2.getCell(2).setCellValue("⑥ 주행 전(km)");
            h2.getCell(3).setCellValue("⑦ 주행 후(km)");
            h2.getCell(4).setCellValue("⑧ 주행거리(km)");
            h2.getCell(5).setCellValue("㉮ 출·퇴근용");
            h2.getCell(6).setCellValue("㉯ 일반 업무용");

            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 2, rowIdx - 1, 0, 0)); // 사용일자
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 2, rowIdx - 1, 1, 1)); // 사용자
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 2, rowIdx - 2, 2, 4)); // 운행내역
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 2, rowIdx - 2, 5, 6)); // 업무용 사용거리
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 2, rowIdx - 1, 7, 7)); // 비업무용
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 2, rowIdx - 1, 8, 8)); // 비고

            // 4. 운행 내역 데이터 행 주입
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            double totalDistance = 0.0;
            double totalCommute = 0.0;
            double totalBusiness = 0.0;
            double totalPersonal = 0.0;

            for (DrivingSession s : sessions) {
                Row r = sheet.createRow(rowIdx++);
                r.setHeightInPoints(20);

                double dist = s.getDrivenDistance() != null ? s.getDrivenDistance() : 0.0;
                totalDistance += dist;

                double commuteDist = 0.0;
                double businessDist = 0.0;
                double personalDist = 0.0;

                if (s.getPurposeType() == DrivePurpose.COMMUTE) {
                    commuteDist = dist;
                    totalCommute += dist;
                } else if (s.getPurposeType() == DrivePurpose.PERSONAL) {
                    personalDist = dist;
                    totalPersonal += dist;
                } else {
                    businessDist = dist;
                    totalBusiness += dist;
                }

                String userName = s.getUser() != null ? s.getUser().getName() : "-";
                String routeSummary = String.format("%s → %s",
                        s.getStartAddress() != null ? s.getStartAddress() : "출발지",
                        s.getEndAddress() != null ? s.getEndAddress() : "도착지");

                createCell(r, 0, s.getStartedAt().format(dateFormatter), dataStyle);
                createCell(r, 1, userName, dataStyle);
                createNumericCell(r, 2, s.getStartOdometer(), dataRightStyle);
                createNumericCell(r, 3, s.getEndOdometer() != null ? s.getEndOdometer() : s.getStartOdometer() + dist, dataRightStyle);
                createNumericCell(r, 4, dist, dataRightStyle);
                createNumericCell(r, 5, commuteDist, dataRightStyle);
                createNumericCell(r, 6, businessDist, dataRightStyle);
                createNumericCell(r, 7, personalDist, dataRightStyle);
                createCell(r, 8, routeSummary, dataStyle);
            }

            // 5. 합계 행
            Row sumRow = sheet.createRow(rowIdx++);
            sumRow.setHeightInPoints(26);
            createCell(sumRow, 0, "합 계", summaryStyle);
            createCell(sumRow, 1, "-", summaryStyle);
            createCell(sumRow, 2, "-", summaryStyle);
            createCell(sumRow, 3, "-", summaryStyle);
            createNumericCell(sumRow, 4, totalDistance, summaryStyle);
            createNumericCell(sumRow, 5, totalCommute, summaryStyle);
            createNumericCell(sumRow, 6, totalBusiness, summaryStyle);
            createNumericCell(sumRow, 7, totalPersonal, summaryStyle);

            double workRatio = totalDistance > 0 ? ((totalCommute + totalBusiness) / totalDistance) * 100.0 : 100.0;
            createCell(sumRow, 8, String.format("업무사용비율: %.1f%%", workRatio), summaryStyle);

            // 열 너비 자동 조정 (Auto-fit)
            int[] colWidths = {3500, 3000, 3800, 3800, 3500, 3500, 3500, 3500, 12000};
            for (int i = 0; i < colWidths.length; i++) {
                sheet.setColumnWidth(i, colWidths[i]);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private CellStyle createTitleStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setFontHeightInPoints((short) 18);
        f.setBold(true);
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private CellStyle createSubTitleStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setFontHeightInPoints((short) 10);
        f.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        s.setFont(f);
        return s;
    }

    private CellStyle createHeaderStyle(Workbook wb, short bgColor) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setFontHeightInPoints((short) 10);
        f.setBold(true);
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setFillForegroundColor(bgColor);
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(s);
        return s;
    }

    private CellStyle createValueStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setAlignment(HorizontalAlignment.LEFT);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(s);
        return s;
    }

    private CellStyle createDataStyle(Workbook wb, HorizontalAlignment align) {
        CellStyle s = wb.createCellStyle();
        s.setAlignment(align);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(s);
        return s;
    }

    private CellStyle createSummaryStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.RIGHT);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(s);
        return s;
    }

    private void setBorder(CellStyle s) {
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
    }

    private void createMetaCell(Row row, int col, String val, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(val);
        c.setCellStyle(style);
    }

    private void createCell(Row row, int col, String val, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(val);
        c.setCellStyle(style);
    }

    private void createNumericCell(Row row, int col, Double val, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(val != null ? val : 0.0);
        c.setCellStyle(style);
    }
}
