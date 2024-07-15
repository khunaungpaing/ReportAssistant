package com.khun.reportassistant.services.impl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.khun.reportassistant.models.ActualMonthlyReportDTO;
import com.khun.reportassistant.models.ProjectHour;
import org.springframework.stereotype.Service;

import com.khun.reportassistant.models.DailyReport;
import com.khun.reportassistant.services.CalculateReport;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MonthlyReport implements CalculateReport {

    @Override
    public ActualMonthlyReportDTO calculateCustomerSupport(List<DailyReport> dailyReports, List<String> focMembers, double planTotalHours) {
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

        var projectHoursList = new ArrayList<ProjectHour>();
        for (var name : projectNames) {
            log.info("project name: {}", name);
            var hour = contractReport.stream()
                    .filter(report -> name.equalsIgnoreCase(report.getFunctionId().replaceAll("\\s", "")))
                    .mapToDouble(DailyReport::getHour)
                    .sum();

            projectHoursList.add(new ProjectHour(hour, name));
        }

        // calculate total hour for each member
        var customerSupportHours = contractReport.stream()
                .filter(report -> report != null &&
                        report.getFunctionId() != null &&
                        report.getFunctionId().matches("(?i)^inquir.*")&&
                        "DAT2024-02-27-001-01".equalsIgnoreCase(report.getProjectId()))
                .mapToDouble(DailyReport::getHour)
                .sum();

        var projectManagementHours = contractReport.stream()
                .filter(report -> "Project Management".equalsIgnoreCase(report.getCategory())&&
                        "DAT2024-02-27-001-01".equalsIgnoreCase(report.getProjectId())
                )
                .mapToDouble(DailyReport::getHour)
                .sum();


        var actualTotalHour = customerSupportHours + projectManagementHours + projectHoursList.stream().mapToDouble(ProjectHour::getHour).sum();
       if (planTotalHours > actualTotalHour) {
           var focCustomerSupportingList = focReport.stream()
                   .filter(report -> report != null &&
                           report.getFunctionId() != null &&
                           report.getFunctionId().matches("(?i)^inquir.*") &&
                           "DAT2024-02-27-001-01".equalsIgnoreCase(report.getProjectId()))
                   // sort list by hour ascending
                   .sorted(Comparator.comparing(DailyReport::getHour))
                   .toList();

           var focCustomerSupportingHourList =  findClosestSum(focCustomerSupportingList, planTotalHours-actualTotalHour);
           customerSupportHours += focCustomerSupportingHourList.stream().mapToDouble(DailyReport::getHour).sum();


           String filename = LocalDateTime.now().getMonth().name()+"-foc.csv";

           // Writing focMember values to the file
           try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {

                       // Write the header
                       writer.write("Date,ProjectId,ProjectName,StaffId,StaffName,FunctionId,Activity,Category,Description,Hour");
                       writer.newLine();

                       // Write the data
                       focCustomerSupportingHourList.forEach(report -> {
                           try {
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

                   } catch (IOException e) {
                       log.error(e.getMessage());
                   }
               });
           } catch (IOException e) {
               log.error(e.getMessage());
           }

       }
        var rs = new ActualMonthlyReportDTO();
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
