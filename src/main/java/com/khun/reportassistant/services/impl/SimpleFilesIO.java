package com.khun.reportassistant.services.impl;

import com.khun.reportassistant.models.DailyReport;
import com.khun.reportassistant.services.FilesIOService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class SimpleFilesIO implements FilesIOService {

    @Override
    public List<DailyReport> readExcelFile(MultipartFile file) throws IOException {
        List<DailyReport> dailyReports = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            log.info("sheet name: {}", sheet.getSheetName());
            for (Row row : sheet) {
                // Skip the header row (assumed to be the first row)
                if (row.getRowNum() == 0) {
                    continue;
                }
                
                DailyReport dailyReport = new DailyReport();

                if(row.getCell(0) != null) {
                    dailyReport.setDate(row.getCell(0).getLocalDateTimeCellValue().toLocalDate());
                }
                

                if (row.getCell(1) != null) {
                    dailyReport.setProjectId(row.getCell(1).getStringCellValue());
                } else {
                    dailyReport.setProjectId("");
                }
                
                if (row.getCell(2) != null) {
                    dailyReport.setProjectName(row.getCell(2).getStringCellValue());
                } else {
                    dailyReport.setProjectName("");
                }
                
                if (row.getCell(3) != null) {
                    dailyReport.setStaffId(row.getCell(3).getStringCellValue());
                } else {
                    dailyReport.setStaffId("");
                }
                
                if (row.getCell(4) != null) {
                    dailyReport.setStaffName(row.getCell(4).getStringCellValue());
                } else {
                    dailyReport.setStaffName("");
                }
                
                if (row.getCell(6) != null) {
                    dailyReport.setFunctionId(row.getCell(6).getStringCellValue());
                } else {
                    dailyReport.setFunctionId("");
                }
                
                if (row.getCell(7) != null) {
                    dailyReport.setCategory(row.getCell(7).getStringCellValue());
                } else {
                    dailyReport.setCategory("");
                }
                
                if (row.getCell(8) != null) {
                    dailyReport.setActivity(row.getCell(8).getStringCellValue());
                } else {
                    dailyReport.setActivity("");
                }
                
                if (row.getCell(9) != null) {
                    dailyReport.setDescription(row.getCell(9).getStringCellValue());
                } else {
                    dailyReport.setDescription("");
                }
                
                if (row.getCell(14) != null) {
                    dailyReport.setHour((float) row.getCell(14).getNumericCellValue());
                } else {
                    dailyReport.setHour(0.0f);
                }
                
                dailyReports.add(dailyReport);
            }
        }
        return dailyReports;
    }
    public ByteArrayInputStream writeExcelFile(List<DailyReport> dailyReports) throws IOException {
       
        String absolutePath = "/static/reports/mmreport/"+
        (dailyReports.get(0).getProjectName().split("\\s")[0]).toLowerCase()+
                "-"+dailyReports.get(0).getDate().getYear()+"-"+dailyReports.get(0).getDate().getMonthValue()+".xlsx";
        String templatePath = checkFileExist(absolutePath);

        // Read the template Excel file from the resources directory
        try (InputStream templateStream = getClass().getResourceAsStream(templatePath);
             Workbook workbook = new XSSFWorkbook(Objects.requireNonNull(templateStream));
             ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.getSheet("OSS");
            Row row = sheet.getRow(0);


            // Convert ByteArrayOutputStream to ByteArrayInputStream and return it
            return new ByteArrayInputStream(outStream.toByteArray());
        }
    }

    private String checkFileExist(String absolutePath) {
        String templatePath = "/static/reports/mm-template.xlsx";
        if(absolutePath != null && !absolutePath.isEmpty() && Files.exists(Paths.get(absolutePath))) {
            return absolutePath;
        }
        return templatePath;
    }
}
