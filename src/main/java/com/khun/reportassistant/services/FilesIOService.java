package com.khun.reportassistant.services;

import com.khun.reportassistant.models.DailyReport;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface FilesIOService {
    List<DailyReport> readExcelFile(MultipartFile file) throws IOException;
    ByteArrayInputStream writeExcelFile(List<DailyReport> dailyReports) throws IOException;
}
