package com.khun.reportassistant.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ActualMonthlyReportDTO {
    private List<ProjectHour> projectHourList;
    private double projectManagementHour;
    private double customerSupportingHour;
    private int weekIndex;
    private String week;
    private int workingDay;
    private String month;
    private Map<String, Integer> workingDaysPerMonth;
}
