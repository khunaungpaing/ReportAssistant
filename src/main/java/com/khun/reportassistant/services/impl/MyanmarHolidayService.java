package com.khun.reportassistant.services.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.khun.reportassistant.services.HolidayService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MyanmarHolidayService implements HolidayService{
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public boolean isHoliday(String date) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isHoliday'");
    }

    @Override
    public List<LocalDate> getHolidays(int year) {
        return getHolidayFromCSV(year);
    }

   
    public List<LocalDate> getHolidayFromCSV(int year) {
        final String FILE_PATH = "reports/holiday/mm-holiday-"+year+".csv";
        List<LocalDate> holidays = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
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
        } catch (IOException e) {
            log.error("Error reading the CSV file: " + e.getMessage());
        }

        return holidays;
    }

    
}
