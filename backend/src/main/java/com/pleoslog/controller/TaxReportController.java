package com.pleoslog.controller;

import com.pleoslog.service.TaxReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Tag(name = "2. Tax Report API", description = "국세청 법정 운행기록부(제16호의2서식) 엑셀 생성 및 다운로드")
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class TaxReportController {

    private final TaxReportService taxReportService;

    @Operation(summary = "국세청 법정 운행기록부(제16호의2서식) 엑셀 다운로드",
               description = "차량 ID와 과세연도를 기준으로 법정 서식 양식의 엑셀(.xlsx) 파일을 즉시 생성하여 다운로드합니다.")
    @GetMapping("/tax-excel")
    public ResponseEntity<byte[]> downloadTaxReportExcel(
            @RequestParam Long vehicleId,
            @RequestParam(required = false) Integer year) throws IOException {

        int targetYear = (year != null) ? year : LocalDate.now().getYear();
        byte[] excelBytes = taxReportService.generateTaxReportExcel(vehicleId, targetYear);

        String fileName = String.format("업무용승용차_운행기록부_%d년_차량%d.xlsx", targetYear, vehicleId);
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
