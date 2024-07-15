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
    
        List<String> projectNames = extractDistinctProjectNames(contractReport, "(?i)^jaz.*");
        List<ProjectHour> projectHoursList = calculateProjectHours(contractReport, projectNames);
    
        double customerSupportHours = calculateTotalHours(contractReport, "(?i)^inquir.*", "DAT2024-02-27-001-01");
        double projectManagementHours = calculateTotalHours(contractReport, "Project Management", "DAT2024-02-27-001-01", true);
    
        double actualTotalHour = customerSupportHours + projectManagementHours + projectHoursList.stream().mapToDouble(ProjectHour::getHour).sum();
    
        if (planTotalHours > actualTotalHour) {
            customerSupportHours += processFOCSupportingHours(focReport, planTotalHours - actualTotalHour);
        }
        var date = holidayService.getHolidays(2024);
        var value = calculateWeekdays(date);
        log.info("dateList : {}",date);
        log.info("weekday in month: {}", value);

        return createActualMonthlyReportDTO(customerSupportHours, projectManagementHours, projectHoursList);
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
        String directory = "reports/foc/";
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
    public static Map<String, Map<String, Integer>> calculateWeekdays(List<LocalDate> holidays) {
        Map<String, Map<String, Integer>> result = new LinkedHashMap<>();
        String[] months = {
            "january", "february", "march", "april", "may", "june", 
            "july", "august", "september", "october", "november", "december"
        };

        for (int month = 1; month <= 12; month++) {
            YearMonth yearMonth = YearMonth.of(LocalDate.now().getYear(), month);
            LocalDate firstDayOfMonth = yearMonth.atDay(1);
            LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();

            Map<String, Integer> weeklyCounts = new LinkedHashMap<>();
            String monthName = months[month - 1];

            LocalDate currentDay = firstDayOfMonth;
            while (!currentDay.isAfter(lastDayOfMonth)) {
                if (isWeekday(currentDay) && !holidays.contains(currentDay)) {
                    String weekSegment = getWeekSegment(currentDay, firstDayOfMonth, lastDayOfMonth, holidays);
                    weeklyCounts.put(weekSegment, weeklyCounts.getOrDefault(weekSegment, 0) + 1);
                }
                currentDay = currentDay.plusDays(1);
            }

            result.put(monthName, weeklyCounts);
        }

        return result;
    }

    private static boolean isWeekday(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    private static String getWeekSegment(LocalDate date, LocalDate startOfMonth, LocalDate endOfMonth, List<LocalDate> holidays) {
        LocalDate firstDayOfWeek = date.with(DayOfWeek.MONDAY);
        LocalDate lastDayOfWeek = firstDayOfWeek.plusDays(4);

        // Adjust for month boundaries
        if (firstDayOfWeek.isBefore(startOfMonth)) {
            firstDayOfWeek = startOfMonth;
        }
        if (lastDayOfWeek.isAfter(endOfMonth)) {
            lastDayOfWeek = endOfMonth;
        }

        // Adjust for holidays
        while (holidays.contains(lastDayOfWeek) && lastDayOfWeek.isAfter(firstDayOfWeek)) {
            lastDayOfWeek = lastDayOfWeek.minusDays(1);
        }

        return firstDayOfWeek.getDayOfMonth() + "-" + lastDayOfWeek.getDayOfMonth();
    }
}

/*
* TODO: take list of foc and list of daily report
* TODO: filter foc report and contract member report
* TODO: calculate actualMonthlyReport [customer supporting hours, project management hours and project hour list]
* TODO: check the sum of actualMonthlyReport is similar with the planMonthlyReport
*       if not, check which daily report row of foc can we use by category.
*       and use the closest value and note the one you took.
* TODO: calculate plan weekly hour
* TODO: calculate working day and hour excluding myanmar holiday, sat and sun
* TODO: update data to excel file
*
* */
