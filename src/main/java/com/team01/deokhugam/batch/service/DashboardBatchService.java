package com.team01.deokhugam.batch.service;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.batch.dto.PopularBookScoreRow;
import com.team01.deokhugam.batch.repository.PopularBookBatchQueryRepository;
import com.team01.deokhugam.comment.repository.CommentRepository;
import com.team01.deokhugam.dashboard.popularreview.service.PopularReviewService;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardBatchService {

  private final DashboardBatchTransactionService dashboardBatchTransactionService;
  private final PopularBookBatchQueryRepository popularBookBatchQueryRepository;
  private final CommentRepository commentRepository;
  private final PopularReviewService popularReviewService;

  public void calculatePowerUserRanking(LocalDate baseDate) {

    OffsetDateTime calculatedAt = OffsetDateTime.now(ZoneOffset.UTC);

    for (DashboardPeriod period : DashboardPeriod.values()) {
      // 1. 기간 시작/종료 시간
      OffsetDateTime start = period.getStartDateTime(baseDate).atOffset(ZoneOffset.UTC);
      OffsetDateTime end = period.getEndDateTime(baseDate).atOffset(ZoneOffset.UTC);
      // 2. 유저별 데이터 조회
      // TODO: 유저별 리뷰 인기점수 합 조회 (start, end 사용)

      // TODO: 유저별 좋아요 수 조회 (start, end 사용)

      // TODO: 유저별 댓글 수 조회 (start, end 사용)
      var commentCounts = commentRepository.findCommentCountsByUserBetween(start, end);
      // 3. 점수 계산
      Map<UUID, Double> activityScoreMap = new HashMap<>();
      // activityScoreMap.put(userId, (reviewScoreSum * 0.5) + (likeCount * 0.2) + (commentCounts *
      // 0.3));
      // 4. rank 부여
      List<Map.Entry<UUID, Double>> rank =
          activityScoreMap.entrySet().stream()
              .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
              .toList();
      if (rank.isEmpty()) {
        continue;
      }
      // 5. 삭제 및 저장
      try {
        dashboardBatchTransactionService.deleteAndSave(period, rank, calculatedAt);
      } catch (Exception e) {
        log.error("랭킹 계산 실패: {}", period, e);
      }
    }
  }

  // 인기 도서 계산 메서드
  public void calculatePopularBookRanking(LocalDate baseDate) {
    LocalDate calculatedDate = baseDate;

    for (DashboardPeriod period : DashboardPeriod.values()) {
      OffsetDateTime start = period.getStartDateTime(baseDate).atOffset(ZoneOffset.UTC);
      OffsetDateTime end = period.getEndDateTime(baseDate).atOffset(ZoneOffset.UTC);

      // 기간 내 리뷰를 책별로 집계해서 점수를 계산한다.
      List<PopularBookScoreRow> rows =
          popularBookBatchQueryRepository.findPopularBooksBetween(start, end);

      if (rows.isEmpty()) {
        log.info("[DASHBOARD_BATCH] 인기 도서 데이터 없음. period={}", period);
        continue;
      }

      try {
        dashboardBatchTransactionService.deleteAndSavePopularBooks(period, rows, calculatedDate);
      } catch (Exception e) {
        log.error("[DASHBOARD_BATCH] 인기 도서 저장 실패. period={}", period, e);
      }
    }
  }

  // 인기 리뷰 계산 메서드
  public void calculatePopularReviewRanking(LocalDate baseDate) {
    LocalDate calculatedDate = baseDate;

    for (DashboardPeriod period : DashboardPeriod.values()) {
      OffsetDateTime start = period.getStartDateTime(baseDate).atOffset(ZoneOffset.UTC);
      OffsetDateTime end = period.getEndDateTime(baseDate).atOffset(ZoneOffset.UTC);

      try {
        popularReviewService.calculatePopularReviews(period, calculatedDate, start, end);
      } catch (Exception e) {
        log.error("[DASHBOARD_BATCH] 인기 리뷰 저장 실패. period={}", period, e);
      }
    }
  }
}
