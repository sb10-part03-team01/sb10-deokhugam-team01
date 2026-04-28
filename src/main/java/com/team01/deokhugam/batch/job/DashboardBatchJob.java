package com.team01.deokhugam.batch.job;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.batch.service.DashboardBatchService;
import com.team01.deokhugam.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DashboardBatchJob {

  private final DashboardBatchService dashboardBatchService;
  private final NotificationService notificationService;

  @Scheduled(cron = "0 0 0 * * *", zone = "UTC")
  public void run() {
    log.info("[DASHBOARD_BATCH] 시작");

    // 인기 도서 랭킹 계산
    try {
      dashboardBatchService.calculatePopularBookRanking(DashboardPeriod.today());
    } catch (Exception e) {
      log.error("[DASHBOARD_BATCH] 인기 도서 랭킹 계산 실패", e);
    }

    // 인기 리뷰 계산
    try {
      dashboardBatchService.calculatePopularReviewRanking(DashboardPeriod.today());
    } catch (Exception e) {
      log.error("[DASHBOARD_BATCH] 인기 리뷰 랭킹 계산 실패", e);
    }

    // 파워 유저 랭킹 계산
    try {
      dashboardBatchService.calculatePowerUserRanking(DashboardPeriod.today());
    } catch (Exception e) {
      log.error("[DASHBOARD_BATCH] 파워 유저 랭킹 계산 실패", e);
    }
    try {
      notificationService.cleanupReadNotifications();
    } catch (Exception e) {
      log.error("[DASHBOARD_BATCH] 읽음 알림 정리 실패", e);
    }

    log.info("[DASHBOARD_BATCH] 종료");
  }
}
