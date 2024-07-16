package com.khun.reportassistant.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ActualMonthlyReportDTO {
    private List<ProjectHour> projectHourList;
    private double projectManagementHour;
    private double customerSupportingHour;
    private String week;
    private int workingDay;
}
