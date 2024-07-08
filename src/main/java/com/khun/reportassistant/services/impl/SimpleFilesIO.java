package com.khun.reportassistant.services.impl;

import com.khun.reportassistant.models.DailyReport;
import com.khun.reportassistant.services.FilesIOService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
            for (Row row : sheet) {
                // Skip the header row (assumed to be the first row)
                if (row.getRowNum() == 0) {
                    continue;
                }

                DailyReport dailyReport = new DailyReport();

                dailyReport.setDate(row.getCell(0).getLocalDateTimeCellValue().toLocalDate());
                dailyReport.setProjectId(row.getCell(1).getStringCellValue());
                dailyReport.setProjectName(row.getCell(2).getStringCellValue());
                dailyReport.setStaffId(row.getCell(3).getStringCellValue());
                dailyReport.setName(row.getCell(4).getStringCellValue());
                dailyReport.setFunctionId(row.getCell(6).getStringCellValue());
                dailyReport.setActivity(row.getCell(7).getStringCellValue());
                dailyReport.setCategory(row.getCell(8).getStringCellValue());
                dailyReport.setDescription(row.getCell(9).getStringCellValue());
                dailyReport.setHour((float) row.getCell(14).getNumericCellValue());

                dailyReports.add(dailyReport);
            }
        }
        return dailyReports;
    }

    @Override
    public ByteArrayInputStream writeExcelFile(List<DailyReport> dailyReports) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("OSS");

        // Create styles for headers and cells

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle cellStyle = createCellStyle(workbook);
        CellStyle planStyle = createPlanStyle(workbook);
        CellStyle actualStyle = createActualStyle(workbook);

        // Set up the merged regions for complex headers
        createMergedRegions(sheet);

        // Create the first row and set column titles
        createHeaderRow(sheet, headerStyle);

        // Create data rows
        createDataRows(sheet, cellStyle, planStyle, actualStyle);

        // Adjust column widths
        adjustColumnWidths(sheet);

        // Write to ByteArrayOutputStream
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static CellStyle createCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static CellStyle createPlanStyle(Workbook workbook) {
        CellStyle style = createCellStyle(workbook);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static CellStyle createActualStyle(Workbook workbook) {
        CellStyle style = createCellStyle(workbook);
        style.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static void createMergedRegions(Sheet sheet) {
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0)); // Project ID
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1)); // Project Name
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 2, 2)); // Tasks
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 3, 4)); // Plan/Actual
    }

    private static void createHeaderRow(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(20);

        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue("Project ID");
        headerCell.setCellStyle(headerStyle);

        headerCell = headerRow.createCell(1);
        headerCell.setCellValue("Project Name");
        headerCell.setCellStyle(headerStyle);

        headerCell = headerRow.createCell(2);
        headerCell.setCellValue("Tasks");
        headerCell.setCellStyle(headerStyle);

        headerCell = headerRow.createCell(3);
        headerCell.setCellValue("Plan");
        headerCell.setCellStyle(headerStyle);

        headerCell = headerRow.createCell(4);
        headerCell.setCellValue("Actual");
        headerCell.setCellStyle(headerStyle);

        Row subHeaderRow = sheet.createRow(1);
        subHeaderRow.setHeightInPoints(20);

        Cell subHeaderCell = subHeaderRow.createCell(3);
        subHeaderCell.setCellValue("Plan");
        subHeaderCell.setCellStyle(headerStyle);

        subHeaderCell = subHeaderRow.createCell(4);
        subHeaderCell.setCellValue("Actual");
        subHeaderCell.setCellStyle(headerStyle);
    }

    private static void createDataRows(Sheet sheet, CellStyle cellStyle, CellStyle planStyle, CellStyle actualStyle) {
        Object[][] data = {
                {"DAT2024-02-27-001-01", "OSS Development", "A2025500-B0000", 52.50, 55.25},
                {"DAT2024-02-27-001-01", "OSS Development", "A2025500-C0000", 52.50, 50.70},
                {"DAT2024-02-27-001-01", "OSS Development", "A2025500-E0000", 15.00, 17.55},
                {"DAT2024-02-27-003-01", "OSS Project Knowledge Improvement and Training", "Offshore certificate level up training. (Web Client Level 2 training)", 1.50, 0.00},
                {"DAT2024-08-16-002", "IJP Training Program", "Japanese Language Training", 323.25, 323.25}
        };

        int rowNum = 2;
        for (Object[] rowData : data) {
            Row row = sheet.createRow(rowNum++);
            int colNum = 0;
            for (Object field : rowData) {
                Cell cell = row.createCell(colNum++);
                if (field instanceof String) {
                    cell.setCellValue((String) field);
                    cell.setCellStyle(cellStyle);
                } else if (field instanceof Double) {
                    cell.setCellValue((Double) field);
                    if (colNum == 4) {
                        cell.setCellStyle(planStyle);
                    } else if (colNum == 5) {
                        cell.setCellStyle(actualStyle);
                    }
                }
            }
        }
    }

    private static void adjustColumnWidths(Sheet sheet) {
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
