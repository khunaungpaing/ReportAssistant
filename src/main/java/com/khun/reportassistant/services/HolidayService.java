package com.khun.reportassistant.services;

import java.time.LocalDate;
import java.util.List;

public interface HolidayService {
    boolean isHoliday(String date);
    List<LocalDate> getHolidays(int year);
}