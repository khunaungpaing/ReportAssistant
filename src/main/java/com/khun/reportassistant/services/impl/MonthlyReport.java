package com.khun.reportassistant.services.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.khun.reportassistant.models.ActualMonthlyReportDTO;
import com.khun.reportassistant.models.ProjectHour;
import org.springframework.stereotype.Service;

import com.khun.reportassistant.models.DailyReport;
import com.khun.reportassistant.services.CalculateReport;
import com.khun.reportassistant.services.HolidayService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MonthlyReport implements CalculateReport {

    private final HolidayService holidayService;

    @Override
    public ActualMonthlyReportDTO calculateCustomerSupport(List<DailyReport> dailyReports, List<String> focMembers, double planTotalHours) {
        List<DailyReport> focReport = filterReportsByStaffNames(dailyReports, focMembers);
        List<DailyReport> contractReport = filterReportsExcludingStaffNames(dailyReports, focMembers);
        List<LocalDate> dates = dailyReports.stream().map(DailyReport::getDate).distinct().toList();
        List<String> projectNames = extractDistinctProjectNames(contractReport, "(?i)^jaz.*");
        List<ProjectHour> projectHoursList = calculateProjectHours(contractReport, projectNames);
    
        double customerSupportHours = calculateTotalHours(contractReport, "(?i)^inquir.*", "DAT2024-02-27-001-01");
        double projectManagementHours = calculateTotalHours(contractReport, "Project Management", "DAT2024-02-27-001-01", true);
    
        double actualTotalHour = customerSupportHours + projectManagementHours + projectHoursList.stream().mapToDouble(ProjectHour::getHour).sum();
    
        if (planTotalHours > actualTotalHour) {
            customerSupportHours += processFOCSupportingHours(focReport, planTotalHours - actualTotalHour);
        }
        List<LocalDate> holidayDate = null;
        try {
            holidayDate = holidayService.getHolidays(2024);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var week= dates.get(0).getDayOfMonth()+"~"+dates.get(dates.size()-1).getDayOfMonth();
        var workingDays = holidayService.calculateWeekdays(holidayDate)
                .get(dates.get(0).getMonth().name().toLowerCase())
                .get(week);
        log.info("workingDaysPerYear : {}",workingDays);

        return ActualMonthlyReportDTO.builder()
                .customerSupportingHour(customerSupportHours)
                .projectManagementHour(projectManagementHours)
                .projectHourList(projectHoursList)
                .week(week)
                .workingDay(workingDays)
                .build();
    }
    
    private List<DailyReport> filterReportsByStaffNames(List<DailyReport> reports, List<String> staffNames) {
        return reports.stream()
                .filter(report -> staffNames.contains(report.getStaffName()))
                .toList();
    }
    
    private List<DailyReport> filterReportsExcludingStaffNames(List<DailyReport> reports, List<String> staffNames) {
        return reports.stream()
                .filter(report -> !staffNames.contains(report.getStaffName()))
                .toList();
    }
    
    private List<String> extractDistinctProjectNames(List<DailyReport> reports, String regex) {
        return reports.stream()
                .filter(report -> report.getFunctionId() != null && report.getFunctionId().matches(regex))
                .map(DailyReport::getFunctionId)
                .map(functionId -> functionId.replaceAll("\\s", ""))
                .distinct()
                .toList();
    }
    
    private List<ProjectHour> calculateProjectHours(List<DailyReport> reports, List<String> projectNames) {
        List<ProjectHour> projectHoursList = new ArrayList<>();
        for (String name : projectNames) {
            double hour = reports.stream()
                    .filter(report -> name.equalsIgnoreCase(report.getFunctionId().replaceAll("\\s", "")))
                    .mapToDouble(DailyReport::getHour)
                    .sum();
            projectHoursList.add(new ProjectHour(hour, name));
        }
        return projectHoursList;
    }
    
    private double calculateTotalHours(List<DailyReport> reports, String functionIdRegex, String projectId) {
        return reports.stream()
                .filter(report -> report != null &&
                        report.getFunctionId() != null &&
                        report.getFunctionId().matches(functionIdRegex) &&
                        projectId.equalsIgnoreCase(report.getProjectId()))
                .mapToDouble(DailyReport::getHour)
                .sum();
    }
    
    private double calculateTotalHours(List<DailyReport> reports, String category, String projectId, boolean byCategory) {
        return reports.stream()
                .filter(report -> category.equalsIgnoreCase(report.getCategory()) &&
                        projectId.equalsIgnoreCase(report.getProjectId()))
                .mapToDouble(DailyReport::getHour)
                .sum();
    }
    
    private double processFOCSupportingHours(List<DailyReport> focReport, double targetAdditionalHours) {
        List<DailyReport> focCustomerSupportingList = focReport.stream()
                .filter(report -> report != null &&
                        report.getFunctionId() != null &&
                        report.getFunctionId().matches("(?i)^inquir.*") &&
                        "DAT2024-02-27-001-01".equalsIgnoreCase(report.getProjectId()))
                .sorted(Comparator.comparing(DailyReport::getHour))
                .toList();
    
        List<DailyReport> focCustomerSupportingHourList = findClosestSum(focCustomerSupportingList, targetAdditionalHours);
        String filename = "foc-kpi-report-"+ LocalDate.now().toString() +".csv";
        writeFOCReportsToFile(focCustomerSupportingHourList, filename);
    
        return focCustomerSupportingHourList.stream().mapToDouble(DailyReport::getHour).sum();
    }
    
    private void writeFOCReportsToFile(List<DailyReport> reports, String filename) {
        String directory = "src/main/resources/static/reports/foc/";
        String fullPath = directory + filename;
        
        // Ensure the directory exists
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fullPath))) {
            writer.write("Date,ProjectId,ProjectName,StaffId,StaffName,FunctionId,Activity,Category,Description,Hour");
            writer.newLine();
    
            for (DailyReport report : reports) {
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                        report.getDate(),
                        report.getProjectId(),
                        report.getProjectName(),
                        report.getStaffId(),
                        report.getStaffName(),
                        report.getFunctionId(),
                        report.getActivity(),
                        report.getCategory(),
                        report.getDescription(),
                        report.getHour()));
                writer.newLine();
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    
    private ActualMonthlyReportDTO createActualMonthlyReportDTO(double customerSupportHours, double projectManagementHours, List<ProjectHour> projectHoursList) {
        ActualMonthlyReportDTO rs = new ActualMonthlyReportDTO();
        rs.setCustomerSupportingHour(customerSupportHours);
        rs.setProjectManagementHour(projectManagementHours);
        rs.setProjectHourList(projectHoursList);
        return rs;
    }
    
    public static List<DailyReport> findClosestSum(List<DailyReport> input, double target) {
        List<DailyReport> closestSubset = new ArrayList<>();
        findClosestSumHelper(input, target, 0, new ArrayList<>(), closestSubset);
        return closestSubset;
    }
    
    private static void findClosestSumHelper(List<DailyReport> input, double target, int index, List<DailyReport> currentSubset, List<DailyReport> closestSubset) {
        double currentSum = currentSubset.stream().mapToDouble(DailyReport::getHour).sum();
        double closestSum = closestSubset.stream().mapToDouble(DailyReport::getHour).sum();
    
        if (Math.abs(currentSum - target) < Math.abs(closestSum - target)) {
            closestSubset.clear();
            closestSubset.addAll(currentSubset);
        }
    
        for (int i = index; i < input.size(); i++) {
            currentSubset.add(input.get(i));
            findClosestSumHelper(input, target, i + 1, currentSubset, closestSubset);
            currentSubset.remove(currentSubset.size() - 1);
        }
    }    

    // calculate weekday in every month excluding public holiday
    // input -> holiday list of the year
    // output -> list of weekday in every month like [july1: {"1-5":5},july2: {"8-12":5},...] output: Map<String, Map<String, Integer>>

}

