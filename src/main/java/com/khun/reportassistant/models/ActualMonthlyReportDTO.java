package com.khun.reportassistant.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ActualMonthlyReportDTO {
    private List<ProjectHour> projectHourList;
    private double projectManagementHour;
    private double customerSupportingHour;
}
