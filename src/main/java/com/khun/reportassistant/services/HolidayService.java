package com.khun.reportassistant.services;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface HolidayService {
    boolean isHoliday(String date);
    List<LocalDate> getHolidays(int year) throws IOException;
}