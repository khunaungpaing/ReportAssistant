package com.khun.reportassistant.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.khun.reportassistant.models.DailyReport;
import com.khun.reportassistant.services.CalculateReport;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MonthlyReport implements CalculateReport {

    @Override
    public double calculateCustomerSupport(List<DailyReport> dailyReports, List<String> focMembers) {
        // filter contract member and foc member from daily report
        var focReport = dailyReports.stream()
                .filter(report -> focMembers.contains(report.getStaffName()))
                .toList();

        var contractReport = dailyReports.stream()
                .filter(report -> !focMembers.contains(report.getStaffName()))
                .toList();

        var projectNames = contractReport.stream()
                .filter(report ->
                // use regex to match the function id value that start with "jaz" ignore case
                report.getFunctionId() != null && report.getFunctionId().matches("(?i)^jaz.*"))
                .map(DailyReport::getFunctionId)
                .map(functionId -> functionId.replaceAll("\\s", ""))
                .distinct()
                .toList();

        
        for (var project : projectNames) {
            log.info("project name: {}", project);
            var p = contractReport.stream()
                    .filter(report -> project.equalsIgnoreCase(report.getFunctionId().replaceAll("\\s", "")))
                    .mapToDouble(DailyReport::getHour)
                    .sum();
        }

        log.info("project count: {}", projectNames);

        // calculate total hour for each member
        var customerSupportHours = contractReport.stream()
                .filter(report -> report != null &&
                        report.getFunctionId() != null &&
                        report.getFunctionId().matches("(?i)^inquir.*"))
                .mapToDouble(DailyReport::getHour)
                .sum();

        var projectManagementHours = contractReport.stream()
                .filter(report -> "Project Management".equalsIgnoreCase(report.getCategory()))
                .mapToDouble(DailyReport::getHour)
                .sum();


        log.info("Total Customer Support Hours: {}", customerSupportHours);
        return customerSupportHours;
    }

}
