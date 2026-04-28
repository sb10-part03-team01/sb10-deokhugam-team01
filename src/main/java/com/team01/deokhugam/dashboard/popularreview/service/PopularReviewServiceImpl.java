package com.team01.deokhugam.dashboard.popularreview.service;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.dashboard.popularreview.dto.PopularReviewScoreRow;
import com.team01.deokhugam.dashboard.popularreview.entity.PopularReview;
import com.team01.deokhugam.dashboard.popularreview.repository.PopularReviewRepository;
import com.team01.deokhugam.review.entity.Review;
import com.team01.deokhugam.review.repository.ReviewRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PopularReviewServiceImpl implements PopularReviewService {

  private final PopularReviewRepository popularReviewRepository;
  private final ReviewRepository reviewRepository;

  @Override
  public void calculatePopularReviews(
      DashboardPeriod period,
      LocalDate calculatedDate,
      OffsetDateTime start,
      OffsetDateTime end
  ) {
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
  }
}
