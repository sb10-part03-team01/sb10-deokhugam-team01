package com.team01.deokhugam.batch.service;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.batch.dto.PopularBookScoreRow;
import com.team01.deokhugam.batch.dto.UserActivityCountRow;
import com.team01.deokhugam.batch.dto.UserReviewScoreSumRow;
import com.team01.deokhugam.batch.repository.PopularBookBatchQueryRepository;
import com.team01.deokhugam.batch.repository.PowerUserBatchQueryRepository;
import com.team01.deokhugam.comment.dto.UserCommentCountRow;
import com.team01.deokhugam.comment.repository.CommentRepository;
import com.team01.deokhugam.dashboard.popularreview.service.PopularReviewService;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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
  private final PowerUserBatchQueryRepository powerUserBatchQueryRepository;


  public void calculatePowerUserRanking(LocalDate baseDate) {

    OffsetDateTime calculatedAt = OffsetDateTime.now(ZoneOffset.UTC);

    for (DashboardPeriod period : DashboardPeriod.values()) {
      // 1. 기간 시작/종료 시간
      OffsetDateTime start = period.getStartDateTime(baseDate).atOffset(ZoneOffset.UTC);
      OffsetDateTime end = period.getEndDateTime(baseDate).atOffset(ZoneOffset.UTC);
      // 2. 유저별 데이터 조회
      var reviewScoreSums = powerUserBatchQueryRepository.findReviewScoreSumsByUser(period,
          baseDate);
      var likeCounts = powerUserBatchQueryRepository.findLikeCountsByUserBetween(start, end);
      var commentCounts = commentRepository.findCommentCountsByUserBetween(start, end);
      // 3. 점수 계산
      Map<UUID, Double> reviewScoreSumMap = reviewScoreSums.stream()
          .collect(Collectors.toMap(
              UserReviewScoreSumRow::userId,
              UserReviewScoreSumRow::scoreSum
          ));

      Map<UUID, Long> likeCountMap = likeCounts.stream()
          .collect(Collectors.toMap(
              UserActivityCountRow::userId,
              UserActivityCountRow::count
          ));

      Map<UUID, Long> commentCountMap = commentCounts.stream()
          .collect(Collectors.toMap(
              UserCommentCountRow::userId,
              UserCommentCountRow::commentCount
          ));

      Set<UUID> userIds = new HashSet<>();
      userIds.addAll(reviewScoreSumMap.keySet());
      userIds.addAll(likeCountMap.keySet());
      userIds.addAll(commentCountMap.keySet());

      Map<UUID, Double> activityScoreMap = new HashMap<>();

      for (UUID userId : userIds) {
        double reviewScoreSum = reviewScoreSumMap.getOrDefault(userId, 0.0);
        long likeCount = likeCountMap.getOrDefault(userId, 0L);
        long commentCount = commentCountMap.getOrDefault(userId, 0L);

        double activityScore = reviewScoreSum * 0.5
            + likeCount * 0.2
            + commentCount * 0.3;

        if (activityScore > 0) {
          activityScoreMap.put(userId, activityScore);
        }
      }
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
