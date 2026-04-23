package com.team01.deokhugam.batch.common;

import java.time.LocalDateTime;

public enum DashboardPeriod {
  DAILY, WEEKLY, MONTHLY, ALL_TIME;

  public LocalDateTime getStartDateTime() {
    LocalDateTime now = LocalDateTime.now();
    return switch (this) {
      case DAILY -> now.minusDays(1);
      case WEEKLY -> now.minusWeeks(1);
      case MONTHLY -> now.minusMonths(1);
      case ALL_TIME -> LocalDateTime.of(2000, 1, 1, 0, 0);
    };
  }
}
