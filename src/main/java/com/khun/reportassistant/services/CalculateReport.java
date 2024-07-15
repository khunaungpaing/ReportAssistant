package com.khun.reportassistant.services;

import java.util.List;

import com.khun.reportassistant.models.ActualMonthlyReportDTO;
import com.khun.reportassistant.models.DailyReport;

public interface CalculateReport {
    ActualMonthlyReportDTO calculateCustomerSupport(List<DailyReport> dailyReports, List<String> focMembers, double planTotalHours) ;
}
