package com.khun.reportassistant.services.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.khun.reportassistant.services.HolidayService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MyanmarHolidayService extends HolidayService{
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public List<LocalDate> getHolidays(int year) throws IOException {
        return getHolidayFromCSV(year);
    }

   
    private List<LocalDate> getHolidayFromCSV(int year) throws IOException{
        String FILE_PATH = "/static/reports/holiday/mm-holiday-"+year+".csv";
        List<LocalDate> holidays = new ArrayList<>();

        try (var inputStream = getClass().getResourceAsStream(FILE_PATH);
             BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                LocalDate holiday = LocalDate.parse(line, DATE_FORMATTER);
                if (holiday.getYear() == year) {
                    holidays.add(holiday);
                }
            }
        }

        return holidays;
    }

    
}
