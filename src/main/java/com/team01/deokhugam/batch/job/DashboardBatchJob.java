package com.team01.deokhugam.batch.job;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.batch.service.DashboardBatchService;
import com.team01.deokhugam.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DashboardBatchJob {

  private final DashboardBatchService dashboardBatchService;
  private final NotificationService notificationService;

  @Scheduled(cron = "0 0 0 * * *")
  public void run() {
    dashboardBatchService.calculatePowerUserRanking(DashboardPeriod.today());
    notificationService.cleanupReadNotifications();
  }

}
