package com.jstock.jstock.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {
  private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

  public static Long getCurrentTimeMilis() {
    return ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toInstant().toEpochMilli();
  }

  public static Long fromDateToMilis(String date) {
    LocalDate localDate = LocalDate.parse(date, formatter);
    ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.of("Asia/Kolkata"));
    return zonedDateTime.toInstant().toEpochMilli();
  }

}
