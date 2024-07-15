package com.khun.reportassistant.models;

import lombok.Data;

import java.util.List;

@Data
public class ActualMonthlyReportDTO {
    private List<ProjectHour> projectHourList;
    private double projectManagementHour;
    private double customerSupportingHour;
}
