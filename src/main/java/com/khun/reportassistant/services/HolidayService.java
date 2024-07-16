package com.khun.reportassistant.services;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class HolidayService {

    public abstract List<LocalDate> getHolidays(int year) throws IOException;

    public Map<String, Map<String, Integer>> calculateWeekdays(List<LocalDate> holidays) {
        Map<String, Map<String, Integer>> result = new LinkedHashMap<>();
        String[] months = {
                "january", "february", "march", "april", "may", "june",
                "july", "august", "september", "october", "november", "december"
        };

        for (int month = 1; month <= 12; month++) {
            YearMonth yearMonth = YearMonth.of(LocalDate.now().getYear(), month);
            LocalDate firstDayOfMonth = yearMonth.atDay(1);
            LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();

            Map<String, Integer> weeklyCounts = new LinkedHashMap<>();
            String monthName = months[month - 1];

            LocalDate currentDay = firstDayOfMonth;
            while (!currentDay.isAfter(lastDayOfMonth)) {
                if (isWeekday(currentDay) && !holidays.contains(currentDay)) {
                    String weekSegment = getWeekSegment(currentDay, firstDayOfMonth, lastDayOfMonth, holidays);
                    weeklyCounts.put(weekSegment, weeklyCounts.getOrDefault(weekSegment, 0) + 1);
                }
                currentDay = currentDay.plusDays(1);
            }

            result.put(monthName, weeklyCounts);
        }

        return result;
    }

    private static boolean isWeekday(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    private static String getWeekSegment(LocalDate date, LocalDate startOfMonth, LocalDate endOfMonth, List<LocalDate> holidays) {
        LocalDate firstDayOfWeek = date.with(DayOfWeek.MONDAY);
        LocalDate lastDayOfWeek = firstDayOfWeek.plusDays(4);

        // Adjust for month boundaries
        if (firstDayOfWeek.isBefore(startOfMonth)) {
            firstDayOfWeek = startOfMonth;
        }
        if (lastDayOfWeek.isAfter(endOfMonth)) {
            lastDayOfWeek = endOfMonth;
        }

        // Adjust for holidays
        while (holidays.contains(lastDayOfWeek) && lastDayOfWeek.isAfter(firstDayOfWeek)) {
            lastDayOfWeek = lastDayOfWeek.minusDays(1);
        }

        return firstDayOfWeek.getDayOfMonth() + "~" + lastDayOfWeek.getDayOfMonth();
    }

}