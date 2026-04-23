package com.team01.deokhugam.batch.common;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

public enum DashboardPeriod {
  DAILY, WEEKLY, MONTHLY, ALL_TIME;

  private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

  public LocalDateTime getStartDateTime() {
    return getStartDateTime(Clock.system(SERVICE_ZONE));
  }

  public LocalDateTime getStartDateTime(Clock clock) {
    LocalDateTime now = LocalDateTime.now(clock);
    return switch (this) {
      case DAILY -> now.minusDays(1);
      case WEEKLY -> now.minusWeeks(1);
      case MONTHLY -> now.minusMonths(1);
      case ALL_TIME -> LocalDateTime.of(2000, 1, 1, 0, 0);
    };
  }
}
