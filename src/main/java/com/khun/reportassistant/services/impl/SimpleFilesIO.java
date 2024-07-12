package com.khun.reportassistant.services.impl;

import com.khun.reportassistant.models.DailyReport;
import com.khun.reportassistant.services.FilesIOService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

                
                dailyReport.setDate(row.getCell(0).getLocalDateTimeCellValue().toLocalDate());
                if(row.getCell(1) != null) {
                    dailyReport.setProjectId(row.getCell(1).getStringCellValue());
                }
                dailyReport.setProjectName(row.getCell(2).getStringCellValue());
                dailyReport.setStaffId(row.getCell(3).getStringCellValue());
                dailyReport.setStaffName(row.getCell(4).getStringCellValue());
                dailyReport.setFunctionId(row.getCell(6).getStringCellValue());
                dailyReport.setCategory(row.getCell(7).getStringCellValue());
                dailyReport.setActivity(row.getCell(8).getStringCellValue());
                dailyReport.setDescription(row.getCell(9).getStringCellValue());
                dailyReport.setHour((float) row.getCell(14).getNumericCellValue());

                dailyReports.add(dailyReport);
            }
        }
        return dailyReports;
    }
    public ByteArrayInputStream writeExcelFile(List<DailyReport> dailyReports) throws IOException {
        String templatePath = "/static/report/mm-template.xlsx";

        // Read the template Excel file from the resources directory
        try (InputStream templateStream = getClass().getResourceAsStream(templatePath);
             Workbook workbook = new XSSFWorkbook(templateStream);
             ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {

            if (templateStream == null) {
                throw new IOException("Template file not found: " + templatePath);
            }

            Sheet sheet = workbook.getSheet("OSS");
            Row row = sheet.getRow(0);

            // Convert ByteArrayOutputStream to ByteArrayInputStream and return it
            return new ByteArrayInputStream(outStream.toByteArray());
        }
    }
}
