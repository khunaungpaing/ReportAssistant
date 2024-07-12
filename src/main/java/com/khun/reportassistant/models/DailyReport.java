package com.khun.reportassistant.models;


import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyReport {
    private LocalDate date;
    private String ProjectId;
    private String projectName;
    private String staffId;
    private String staffName;
    private String functionId;
    private String activity;
    private String category;
    private String description;
    private float hour;
}
