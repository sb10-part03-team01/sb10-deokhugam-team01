package com.team01.deokhugam.dashboard.popularreview.service;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.dashboard.popularreview.dto.CursorPageResponsePopularReviewDto;
import com.team01.deokhugam.dashboard.popularreview.dto.PopularReviewDto;
import com.team01.deokhugam.dashboard.popularreview.dto.PopularReviewScoreRow;
import com.team01.deokhugam.dashboard.popularreview.dto.PopularReviewSearchCondition;
import com.team01.deokhugam.dashboard.popularreview.entity.PopularReview;
import com.team01.deokhugam.dashboard.popularreview.mapper.PopularReviewMapper;
import com.team01.deokhugam.dashboard.popularreview.repository.PopularReviewRepository;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.review.entity.Review;
import com.team01.deokhugam.review.repository.ReviewRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PopularReviewServiceImpl implements PopularReviewService {

  private final PopularReviewRepository popularReviewRepository;
  private final ReviewRepository reviewRepository;
  private final PopularReviewMapper popularReviewMapper;

  @Override
  public void calculatePopularReviews(
      DashboardPeriod period,
      LocalDate calculatedDate,
      OffsetDateTime start,
      OffsetDateTime end
  ) {
    log.info("인기 리뷰 집계 시작: period={}, calculatedDate={}, start={}, end={}",
        period, calculatedDate, start, end);
    // 기간 내 좋아요가 발생한 리뷰별 집계 결과를 조회
    List<PopularReviewScoreRow> likeRows =
        popularReviewRepository.findPopularReviewLikeScoreRows(start, end);

    // 기간 내 댓글이 작성된 리뷰별 집계 결과를 조회
    List<PopularReviewScoreRow> commentRows =
        popularReviewRepository.findPopularReviewCommentScoreRows(start, end);

    // 좋아요 집계와 댓글 집계를 reviewId 기준으로 합치기 위한 Map
    Map<UUID, PopularReviewScoreRow> scoreRowMap = new HashMap<>();

    for (PopularReviewScoreRow row : likeRows) {
      scoreRowMap.put(row.reviewId(), row);
    }

    for (PopularReviewScoreRow row : commentRows) {
      scoreRowMap.merge(
          row.reviewId(),
          row,
          (likeRow, commentRow) -> new PopularReviewScoreRow(
              likeRow.reviewId(),
              likeRow.likeCount(),
              commentRow.commentCount()
          )
      );
    }

    // 점수가 있는 리뷰만 인기 점수 내림차순으로 정렬
    // 동점이면 댓글 수, 좋아요 수, reviewId 순서로 정렬해 rank가 흔들리지 않게 고정
    List<PopularReviewScoreRow> rankedRows = scoreRowMap.values().stream()
        .filter(row -> row.score() > 0)
        .sorted(
            Comparator.comparingDouble(PopularReviewScoreRow::score).reversed()
                .thenComparing(PopularReviewScoreRow::commentCount, Comparator.reverseOrder())
                .thenComparing(PopularReviewScoreRow::likeCount, Comparator.reverseOrder())
                .thenComparing(PopularReviewScoreRow::reviewId)
        )
        .toList();

    if (rankedRows.isEmpty()) {
      log.info("인기 리뷰 집계 대상 없음: period={}, calculatedDate={}", period, calculatedDate);
    }

    // PopularReview 생성에 필요한 Review 엔티티를 한 번에 조회
    List<UUID> reviewIds = rankedRows.stream()
        .map(PopularReviewScoreRow::reviewId)
        .toList();

    Map<UUID, Review> reviewMap = reviewRepository.findAllById(reviewIds).stream()
        .collect(Collectors.toMap(Review::getId, review -> review));

    // 같은 기간/집계일의 기존 인기 리뷰 결과를 삭제
    popularReviewRepository.deleteByPeriodAndCalculatedDate(period, calculatedDate);

    List<PopularReview> popularReviews = new ArrayList<>();

    for (PopularReviewScoreRow row : rankedRows) {
      Review review = reviewMap.get(row.reviewId());

      if (review == null) {
        log.warn("인기 리뷰 집계 제외 - 리뷰 엔티티 없음: reviewId={}, period={}, calculatedDate={}",
            row.reviewId(), period, calculatedDate);
        continue;
      }

      // 저장되는 순서 기준으로 rank 부여
      popularReviews.add(new PopularReview(
          review,
          period,
          calculatedDate,
          popularReviews.size() + 1,
          row.score(),
          Math.toIntExact(row.likeCount()),
          Math.toIntExact(row.commentCount())
      ));
    }

    popularReviewRepository.saveAll(popularReviews);

    log.info("인기 리뷰 집계 완료: period={}, calculatedDate={}, savedCount={}",
        period, calculatedDate, popularReviews.size());
  }

  @Override
  @Transactional(readOnly = true)
  public CursorPageResponsePopularReviewDto getPopularReviews(
      DashboardPeriod period,
      SortDirection direction,
      String cursor,
      OffsetDateTime after,
      Integer limit
  ) {
    DashboardPeriod resolvedPeriod = period != null ? period : DashboardPeriod.DAILY;
    SortDirection resolvedDirection = direction != null ? direction : SortDirection.ASC;

    LocalDate calculatedDate = OffsetDateTime.now(ZoneOffset.UTC).toLocalDate();

    PopularReviewSearchCondition condition = new PopularReviewSearchCondition(
        resolvedPeriod,
        resolvedDirection,
        cursor,
        after,
        limit,
        calculatedDate
    );

    int resolvedLimit = condition.limit();

    List<PopularReview> popularReviews = popularReviewRepository.findAllByCondition(condition);
    long totalElements = popularReviewRepository.countByCondition(condition);

    boolean hasNext = popularReviews.size() > resolvedLimit;

    List<PopularReview> pageContent = hasNext
        ? popularReviews.subList(0, resolvedLimit)
        : popularReviews;

    List<PopularReviewDto> content = popularReviewMapper.toDtoList(pageContent);

    String nextCursor = null;
    OffsetDateTime nextAfter = null;

    if (hasNext && !pageContent.isEmpty()) {
      PopularReview last = pageContent.get(pageContent.size() - 1);
      nextCursor = String.valueOf(last.getRank());
      nextAfter = last.getCreatedAt();
    }

    return new CursorPageResponsePopularReviewDto(
        content,
        nextCursor,
        nextAfter,
        content.size(),
        totalElements,
        hasNext
    );
  }
}
