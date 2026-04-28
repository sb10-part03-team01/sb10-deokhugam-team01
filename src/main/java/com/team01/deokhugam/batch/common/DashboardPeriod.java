package com.team01.deokhugam.batch.common;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public enum DashboardPeriod {
  DAILY,
  WEEKLY,
  MONTHLY,
  ALL_TIME;

  // 배치 집계 기준 시간대 UTC 통일
  private static final ZoneId SERVICE_ZONE = ZoneId.of("UTC");

  public LocalDateTime getStartDateTime(LocalDate baseDate) {
    return switch (this) {
      case DAILY -> baseDate.minusDays(1).atStartOfDay();
      case WEEKLY -> baseDate.minusWeeks(1).atStartOfDay();
      case MONTHLY -> baseDate.minusMonths(1).atStartOfDay();
      case ALL_TIME -> LocalDateTime.of(2000, 1, 1, 0, 0);
    };
  }

  public LocalDateTime getEndDateTime(LocalDate baseDate) {
    return baseDate.atStartOfDay();
  }

  // 기준일도 UTC 기준 오늘 날짜
  public static LocalDate today() {
    return LocalDate.now(SERVICE_ZONE);
  }
}
