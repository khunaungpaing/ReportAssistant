package com.khun.reportassistant.controller;

import com.khun.reportassistant.exception.CannotReadFileException;
import com.khun.reportassistant.models.DailyReport;
import com.khun.reportassistant.services.CalculateReport;
import com.khun.reportassistant.services.FilesIOService;
import com.khun.reportassistant.services.MemberService;

import jakarta.servlet.http.HttpServletResponse;
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

@RestController
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FileController {
    private final FilesIOService filesIOService;
    private final CalculateReport calculateReport;
    private final MemberService focMemberService;

    @PostMapping("/upload")
    public ResponseEntity<InputStreamResource> uploadFile(@RequestParam("file") MultipartFile file, HttpServletResponse response) {
    try {
        // Retrieve necessary data
        var necessaryData = focMemberService.getMemberList();

        // Process the uploaded file
        List<DailyReport> dataList = filesIOService.readExcelFile(file).stream()
                .filter(r -> !necessaryData.getRemoveMember().contains(r.getStaffName()))
                .toList();

        // Calculate the report
        var actualMonthlyReportDTO = calculateReport.calculateCustomerSupport(dataList, necessaryData.getFocMember(), (5 * 7.5 * 8));

        // Generate the filename
        var filename = (dataList.get(0).getProjectName().split("\\s")[0]).toLowerCase() +
                "-" + dataList.get(0).getDate().getYear() + "-" + dataList.get(0).getDate().getMonthValue() + ".xlsx";

        // Write the report to a file
        ByteArrayInputStream in = filesIOService.writeExcelFile(actualMonthlyReportDTO, filename);
          HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=report.xlsx");
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(in));        

    } catch (CannotReadFileException e) {
        log.error("Wrong file structure. Failed to read file: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    } catch (IOException e) {
        log.error("Failed to read file: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}


}

