package com.khun.reportassistant.services;

import java.util.List;

import com.khun.reportassistant.models.DailyReport;

public interface CalculateReport {
    double calculateCustomerSupport(List<DailyReport> dailyReports, List<String> focMembers);
}
