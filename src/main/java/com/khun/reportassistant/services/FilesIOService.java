package com.khun.reportassistant.services;

import com.khun.reportassistant.models.ActualMonthlyReportDTO;
import com.khun.reportassistant.models.DailyReport;


import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public interface FilesIOService {
    List<DailyReport> readExcelFile(MultipartFile file) throws IOException;
    ByteArrayInputStream writeExcelFile(ActualMonthlyReportDTO actualMonthlyReportDTO, String filename) throws IOException;
}
