package com.khun.reportassistant.services.impl;

import com.khun.reportassistant.exception.CannotReadFileException;
import com.khun.reportassistant.models.ActualMonthlyReportDTO;
import com.khun.reportassistant.models.DailyReport;
import com.khun.reportassistant.services.FilesIOService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class ExcelFilesIO implements FilesIOService {

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
            if (dailyReports.isEmpty()) {
                throw new CannotReadFileException("There are no daily reports in the file");
            }
        }catch (NullPointerException|IllegalStateException e) {
            throw new CannotReadFileException(e.getMessage());
        }catch (IOException e) {
            throw new IOException(e.getMessage());
        }
        return dailyReports;
    }

   public ByteArrayInputStream writeExcelFile(ActualMonthlyReportDTO actualMonthlyReportDTO, String filename) throws IOException {
        String folderPath = "src/main/resources/static/reports/mmreport/";
        String absolutePath = folderPath + filename;
        String templatePath = checkFileExist(absolutePath);

        // Read the template Excel file from the resources directory
        try (InputStream templateStream = new ClassPathResource(templatePath).getInputStream();
             XSSFWorkbook workbook = new XSSFWorkbook(Objects.requireNonNull(templateStream));
             ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.getSheet("OSS");
            Row row = sheet.getRow(1);

            // Write the actual monthly report data to the Excel file
            row.getCell(1).setCellValue(actualMonthlyReportDTO.getMonth());

            row = sheet.getRow(4);
            int startCol = 5;
            for (var value : actualMonthlyReportDTO.getWorkingDaysPerMonth().values()) {
                row.getCell(startCol++).setCellValue(value);
            }
            

            row = sheet.getRow(5);
            startCol = 5;
            for (var value : actualMonthlyReportDTO.getWorkingDaysPerMonth().keySet()) {
                row.getCell(startCol++).setCellValue(value);
            }

            //for customer supporting hour
            row = sheet.getRow(10);
            row.getCell(actualMonthlyReportDTO.getWeekIndex() + 4).setCellValue(actualMonthlyReportDTO.getCustomerSupportingHour());

            // Evaluate the formula
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            evaluator.evaluateAll();

            // Write the modified workbook to a ByteArrayOutputStream
            workbook.write(outStream);
            outStream.flush();

            // Ensure the folder path exists, if not, create it
            Path path = Paths.get(folderPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }

            // Save the output stream to a file for verification
            try (FileOutputStream fos = new FileOutputStream(absolutePath)) {
                fos.write(outStream.toByteArray());
                fos.flush();
            }

            return new ByteArrayInputStream(outStream.toByteArray());
        } catch (IOException e) {
            throw new IOException("Error while processing the Excel file", e);
        } catch (NullPointerException | IllegalStateException e) {
            throw new IOException("Error while writing data to the Excel file", e);
        }
    }

    private String checkFileExist(String absolutePath) {
        String templatePath = "static/reports/mm-template.xlsx";

        if (absolutePath != null && !absolutePath.isEmpty()) {
            try {
                Path path = Paths.get(absolutePath).normalize();
                if (Files.exists(path)) {
                    return absolutePath;
                } else {
                    return templatePath;
                }
            } catch (Exception e) {
                System.err.println("An error occurred while checking the file existence: " + e.getMessage());
            }
        }

        return templatePath;
    }
}
