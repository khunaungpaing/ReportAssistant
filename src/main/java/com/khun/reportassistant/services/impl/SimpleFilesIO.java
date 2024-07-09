package com.khun.reportassistant.services.impl;

import com.khun.reportassistant.models.DailyReport;
import com.khun.reportassistant.services.FilesIOService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.util.HSSFColor;
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
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("OSS");
            createMergedRegions(sheet);
            createHeaderRow(sheet,workbook);

            // Write to ByteArrayOutputStream
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                workbook.write(out);
                return new ByteArrayInputStream(out.toByteArray());
            }
        }
    }

    private CellStyle createCellStyle(Workbook workbook, short bg, short fontSize, short fontColor,boolean isBold, HorizontalAlignment textAlign, boolean isWrap) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(isBold);
        font.setColor(fontColor);
        font.setFontHeightInPoints(fontSize);
        style.setFont(font);
        style.setAlignment(textAlign);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(bg);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setWrapText(isWrap);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        return style;
    }
    private void createMergedRegions(Sheet sheet) {
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 9, 11)); //Actual work days of this month
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 12)); // May
        sheet.addMergedRegion(new CellRangeAddress(2, 5, 1, 4));  // Oss
        sheet.addMergedRegion(new CellRangeAddress(6, 6, 1, 4));  // 基盤技術第一部
    }

    private void createHeaderRow(Sheet sheet, Workbook workbook) {

        CellStyle monthHeaderStyle = createCellStyle(
                workbook,
                IndexedColors.GREY_50_PERCENT.getIndex(),
                (short) 28,
                HSSFColor.HSSFColorPredefined.WHITE.getIndex(),
                true,
                HorizontalAlignment.CENTER,
                false
        );
        CellStyle ossHeaderStyle = createCellStyle(
                workbook,
                IndexedColors.YELLOW.getIndex(),
                (short) 20,
                HSSFColor.HSSFColorPredefined.BLUE.getIndex(),
                true,
                HorizontalAlignment.CENTER,
                false
        );
        CellStyle nihongoStyle = createCellStyle(
                workbook,
                IndexedColors.SEA_GREEN.getIndex(),
                (short) 12,
                HSSFColor.HSSFColorPredefined.WHITE.getIndex(),
                false,
                HorizontalAlignment.CENTER,
                true
        );

        CellStyle blueCellStyle = createCellStyle(
                workbook,
                IndexedColors.BLUE.getIndex(),
                (short) 11,
                HSSFColor.HSSFColorPredefined.WHITE.getIndex(),
                false,
                HorizontalAlignment.CENTER,
                true
        );

        CellStyle hourCellStyle = createCellStyle(
                workbook,
                IndexedColors.PALE_BLUE.getIndex(),
                (short) 11,
                HSSFColor.HSSFColorPredefined.BLACK.getIndex(),
                false,
                HorizontalAlignment.CENTER,
                false
        );

        CellStyle headerCellStyle = createCellStyle(
                workbook,
                IndexedColors.BLUE.getIndex(),
                (short) 11,
                HSSFColor.HSSFColorPredefined.WHITE.getIndex(),
                false,
                HorizontalAlignment.CENTER,
                false
        );

        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(20);
        Cell headerCell = headerRow.createCell(9);
        headerCell.setCellValue("Actual work days of this month");

        headerCell = headerRow.createCell(12);
        headerCell.setCellValue(21);

        Row monthRow = sheet.createRow(1);
        Cell monthCell = monthRow.createCell(1);
        monthCell.setCellValue("May");
        monthCell.setCellStyle(monthHeaderStyle);

        Row ossRow = sheet.createRow(2);
        Cell ossCell = ossRow.createCell(1);
        ossCell.setCellValue("OSS");
        ossCell.setCellStyle(ossHeaderStyle);

//        Row subHeaderRow = sheet.createRow(2);
        ossRow.createCell(5).setCellValue("1st week");
        ossRow.createCell(6).setCellValue("2nd week");
        ossRow.createCell(7).setCellValue("3rd week");
        ossRow.createCell(8).setCellValue("4th week");
        ossRow.createCell(9).setCellValue("5th week");
        ossRow.createCell(10).setCellValue("Total");
        ossRow.createCell(11).setCellValue("");
        ossRow.createCell(12).setCellValue("");
        for (int i = 5; i <= 12; i++) {
            ossRow.getCell(i).setCellStyle(blueCellStyle);
            ossRow.setHeight((short) (23 * 23));
        }

        Row subHeaderRow = sheet.createRow(3);
        subHeaderRow.createCell(5).setCellValue("Actual Working Hour");
        subHeaderRow.createCell(6).setCellValue("Actual Working Hour");
        subHeaderRow.createCell(7).setCellValue("Actual Working Hour");
        subHeaderRow.createCell(8).setCellValue("Actual Working Hour");
        subHeaderRow.createCell(9).setCellValue("Actual Working Hour");
        subHeaderRow.createCell(10).setCellValue("");
        subHeaderRow.createCell(11).setCellValue("Actual work days");
        subHeaderRow.createCell(12).setCellValue("ContractMM");
        for (int i = 5; i <= 12; i++) {
            subHeaderRow.getCell(i).setCellStyle(blueCellStyle);
            subHeaderRow.setHeight((short) (45* 23));
        }

        subHeaderRow = sheet.createRow(4);
        subHeaderRow.createCell(5).setCellValue(2);
        subHeaderRow.createCell(6).setCellValue(5);
        subHeaderRow.createCell(7).setCellValue(5);
        subHeaderRow.createCell(8).setCellValue(4);
        subHeaderRow.createCell(9).setCellValue(5);
        subHeaderRow.createCell(10).setCellValue("");
        subHeaderRow.createCell(11).setCellValue(21);
        subHeaderRow.createCell(12).setCellValue(8);
        for (int i = 5; i <= 12; i++) {
            subHeaderRow.getCell(i).setCellStyle(blueCellStyle);
            subHeaderRow.setHeight((short) (20* 23));
        }

        subHeaderRow = sheet.createRow(5);
        subHeaderRow.createCell(5).setCellValue("02~03");
        subHeaderRow.createCell(6).setCellValue("06~03");
        subHeaderRow.createCell(7).setCellValue("02~03");
        subHeaderRow.createCell(8).setCellValue("02~03");
        subHeaderRow.createCell(9).setCellValue("02~03");
        subHeaderRow.createCell(10).setCellValue("Total");
        subHeaderRow.createCell(11).setCellValue("MM");
        subHeaderRow.createCell(12).setCellValue("");
        for (int i = 5; i <= 12; i++) {
            subHeaderRow.getCell(i).setCellStyle(blueCellStyle);
            subHeaderRow.setHeight((short) (20* 23));
        }

        // New row for 基盤技術第一部 header
        subHeaderRow = sheet.createRow(6);
        Cell headerCell2 = subHeaderRow.createCell(1);
        headerCell2.setCellValue("基盤技術第一部");
        headerCell2.setCellStyle(nihongoStyle);
        subHeaderRow.setHeight((short) (20* 23));

        subHeaderRow.createCell(5, CellType.FORMULA).setCellFormula("7.5*F5*$M$5");
        subHeaderRow.createCell(6, CellType.FORMULA).setCellFormula("7.5*G5*$M$5");
        subHeaderRow.createCell(7, CellType.FORMULA).setCellFormula("7.5*H5*$M$5");
        subHeaderRow.createCell(8, CellType.FORMULA).setCellFormula("7.5*I5*$M$5");
        subHeaderRow.createCell(9, CellType.FORMULA).setCellFormula("7.5*J5*$M$5");
        subHeaderRow.createCell(10, CellType.FORMULA).setCellFormula("sum(F7:J7)");
        subHeaderRow.createCell(11, CellType.FORMULA).setCellFormula("(K7/(20*7.5*M5))*M5");
        subHeaderRow.createCell(12).setCellValue("");
        for (int i = 5; i <= 12; i++) {
            subHeaderRow.getCell(i).setCellStyle(hourCellStyle);
            sheet.getWorkbook().getCreationHelper().createFormulaEvaluator().evaluateFormulaCell(subHeaderRow.getCell(i));
        }

        headerRow = sheet.createRow(7);
        headerRow.createCell(1).setCellValue("Project ID");
        headerRow.createCell(2).setCellValue("Project Name");
        headerRow.createCell(3).setCellValue("Tasks");
        headerRow.createCell(4).setCellValue("Plan/Actual");
        headerRow.createCell(5);
        headerRow.createCell(6);
        headerRow.createCell(7);
        headerRow.createCell(8).setCellValue("");
        headerRow.createCell(9).setCellValue("");
        headerRow.createCell(10).setCellValue("");
        headerRow.createCell(11).setCellValue("");
        headerRow.createCell(12).setCellValue("");
        for (int i = 1; i <= 12; i++) {
            headerRow.getCell(i).setCellStyle(headerCellStyle);
            headerRow.setHeight((short) (20* 23));
        }

        //cell style
        CellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setWrapText(true);

        sheet.addMergedRegion(new CellRangeAddress(8, 9, 1, 1));
        sheet.addMergedRegion(new CellRangeAddress(8, 9, 2, 2));
        sheet.addMergedRegion(new CellRangeAddress(8, 9, 3, 3));

        headerRow = sheet.createRow(8);
        headerRow.createCell(1).setCellValue("DAT2024-02-27-001-01");
        headerRow.createCell(2).setCellValue("OSS Development");
        headerRow.createCell(3).setCellValue("A2025500-B0000\n" +
                "Customer Inquires Support (For all released version 2.0~6.1.1)\n" +
                "JAZ 2.1.1 & 6.0\n" +
                "- Investigation for 1119, 1121, 1122, 1126,1136,1137, 1139, 1175, 1174, 1179");
        headerRow.createCell(4).setCellValue("Plan");
        headerRow.createCell(5).setCellValue(52);
        headerRow.createCell(6).setCellValue(52);
        headerRow.createCell(7).setCellValue(52);
        headerRow.createCell(8).setCellValue(52);
        headerRow.createCell(9).setCellValue(52);
        headerRow.createCell(10, CellType.FORMULA).setCellFormula("SUM(F9:J9)");
        headerRow.createCell(11, CellType.FORMULA).setCellFormula("IF(K9=0,0,K9/(7.5*20))");
        headerRow.createCell(12, CellType.FORMULA).setCellFormula("IF(K9=0,0,K9/(7.5*$M$1))");
        for (int i = 1; i <= 12; i++) {
            headerRow.getCell(i).setCellStyle(style);
            headerRow.setHeight((short) (25* 23));
        }

        headerRow = sheet.createRow(9);
        headerRow.createCell(4).setCellValue("Actual");
        headerRow.createCell(5).setCellValue(52);
        headerRow.createCell(6).setCellValue(52);
        headerRow.createCell(7).setCellValue(52);
        headerRow.createCell(8).setCellValue(52);
        headerRow.createCell(9).setCellValue(52);
        headerRow.createCell(10, CellType.FORMULA).setCellFormula("SUM(F9:J9)");
        headerRow.createCell(11, CellType.FORMULA).setCellFormula("IF(K9=0,0,K9/(7.5*20))");
        headerRow.createCell(12, CellType.FORMULA).setCellFormula("IF(K9=0,0,K9/(7.5*$M$1))");
        for (int i = 9; i <= 12; i++) {
            headerRow.getCell(i).setCellStyle(style);
            headerRow.setHeight((short) (25* 23));
        }

        sheet.setColumnWidth(0,5 * 256);
        sheet.setColumnWidth(1,21 * 256);
        sheet.setColumnWidth(2,35 * 256);
        sheet.setColumnWidth(3,40 * 256);
    }

}
