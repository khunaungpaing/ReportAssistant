package com.khun.reportassistant.controller;

import com.khun.reportassistant.models.DailyReport;
import com.khun.reportassistant.services.CalculateReport;
import com.khun.reportassistant.services.FilesIOService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FileController {
    private final FilesIOService filesIOService;
    private final CalculateReport calculateReport;

    @PostMapping("/upload")
    public ResponseEntity<Map<String,Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            List<DailyReport> dataList = filesIOService.readExcelFile(file);

            calculateReport.calculateCustomerSupport(dataList, List.of(
                    "Aung Thein Kyaw", "Thant Htet Ko", "Thant Tun Kyaw", "Shin Khant", "Kyaw Zaw Lin"
                ));

            return ResponseEntity.ok(Map.of("data size", dataList.size()));
            // HttpHeaders headers = new HttpHeaders();
            // headers.add("Content-Disposition", "attachment; filename=report.xlsx");
            // return ResponseEntity
            //         .ok()
            //         .headers(headers)
            //         .contentType(MediaType.APPLICATION_OCTET_STREAM)
            //         .body(new InputStreamResource(in));
        } catch (Exception e) {
            log.error("Failed to read file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



}

