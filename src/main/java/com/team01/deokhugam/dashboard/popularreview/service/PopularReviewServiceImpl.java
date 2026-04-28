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
    // 기간 내 집계 대상 리뷰 조회
    List<PopularReviewScoreRow> rows =
        popularReviewRepository.findPopularReviewScoreRows(start, end);
    // 점수 계산, 점수 오름차순 후 역정렬 -> 높은 순서대로 정리 가능
    List<PopularReviewScoreRow> rankedRows = rows.stream()
        .filter(row -> row.score() > 0)
        .sorted(Comparator.comparingDouble(PopularReviewScoreRow::score).reversed())
        .toList();
    // 정렬된 결과에서 id 추출
    List<UUID> reviewIds = rankedRows.stream()
        .map(PopularReviewScoreRow::reviewId)
        .toList();
    // 추출한 id로 엔티티 조회 후 id를 기준으로 Map변환
    Map<UUID, Review> reviewMap = reviewRepository.findAllById(reviewIds).stream()
        .collect(Collectors.toMap(Review::getId, review -> review));
    // 기존 인기 리뷰 삭제
    popularReviewRepository.deleteByPeriodAndCalculatedDate(period, calculatedDate);

    List<PopularReview> popularReviews = new ArrayList<>();

    for (int i = 0; i < rankedRows.size(); i++) {
      PopularReviewScoreRow row = rankedRows.get(i);
      Review review = reviewMap.get(row.reviewId());

      if (review == null) {
        continue;
      }

      // 집계 결과대로 랭크 부여
      popularReviews.add(new PopularReview(
          review,
          period,
          calculatedDate,
          i + 1, // 랭크 1부터 ~
          row.score(),
          Math.toIntExact(row.likeCount()),
          Math.toIntExact(row.commentCount())
      ));
    }
    popularReviewRepository.saveAll(popularReviews);
  }

}


