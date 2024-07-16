package com.khun.reportassistant.controller;

import com.khun.reportassistant.exception.CannotReadFileException;
import com.khun.reportassistant.models.DailyReport;
import com.khun.reportassistant.services.CalculateReport;
import com.khun.reportassistant.services.FilesIOService;
import com.khun.reportassistant.services.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private final MemberService focMemberService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String,Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            var necessaryData = focMemberService.getMemberList();
            List<DailyReport> dataList = filesIOService.readExcelFile(file).stream()
                    .filter(r-> !necessaryData.getRemoveMember().contains(r.getStaffName()))
                    .toList();

            var actualMonthlyReportDTO = calculateReport.calculateCustomerSupport(dataList, necessaryData.getFocMember(), (5*7.5*8));

            var filename = (dataList.get(0).getProjectName().split("\\s")[0]).toLowerCase()+
                    "-"+dataList.get(0).getDate().getYear()+"-"+dataList.get(0).getDate().getMonthValue()+".xlsx";

            var out = filesIOService.writeExcelFile(actualMonthlyReportDTO,filename);
            return ResponseEntity.ok(Map.of("data size", dataList.size()));

        } catch (CannotReadFileException e) {
            log.error("Wrong file structure. Failed to read file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (IOException e) {
            log.error("Failed to read file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



}

