package com.team01.deokhugam.dashboard.popularreview.entity;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.global.entity.BaseEntity;
import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import com.team01.deokhugam.review.entity.Review;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "popular_reviews",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_popular_reviews_period_rank",
            columnNames = {"period_type", "calculated_date", "ranking"}
        ),
        @UniqueConstraint(
            name = "uk_popular_reviews_period_review",
            columnNames = {"period_type", "calculated_date", "review_id"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularReview extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "review_id", nullable = false)
  private Review review;

  @Enumerated(EnumType.STRING)
  @Column(name = "period_type", nullable = false, length = 20)
  private DashboardPeriod period;

  @Column(name = "calculated_date", nullable = false)
  private LocalDate calculatedDate;

  @Column(name = "ranking", nullable = false)
  private int rank;

  @Column(name = "score", nullable = false)
  private double score;

  @Column(name = "liked_count", nullable = false)
  private int likeCount;

  @Column(name = "comment_count", nullable = false)
  private int commentCount;

  public PopularReview(
      Review review,
      DashboardPeriod period,
      LocalDate calculatedDate,
      int rank,
      double score,
      int likeCount,
      int commentCount
  ) {
    validate(review, period, calculatedDate, rank, score, likeCount, commentCount);

    this.review = review;
    this.period = period;
    this.calculatedDate = calculatedDate;
    this.rank = rank;
    this.score = score;
    this.likeCount = likeCount;
    this.commentCount = commentCount;
  }

  public void updateAggregateResult(
      int rank,
      double score,
      int likeCount,
      int commentCount
  ) {
    validate(this.review, this.period, this.calculatedDate, rank, score, likeCount, commentCount);

    this.rank = rank;
    this.score = score;
    this.likeCount = likeCount;
    this.commentCount = commentCount;
  }

  private void validate(
      Review review,
      DashboardPeriod period,
      LocalDate calculatedDate,
      int rank,
      double score,
      int likeCount,
      int commentCount
  ) {
    if (review == null) {
      throw new DeokhugamException(ErrorCode.POPULAR_REVIEW_REVIEW_REQUIRED);
    }
    if (period == null) {
      throw new DeokhugamException(ErrorCode.POPULAR_REVIEW_PERIOD_REQUIRED);
    }
    if (calculatedDate == null) {
      throw new DeokhugamException(ErrorCode.POPULAR_REVIEW_CALCULATED_DATE_REQUIRED);
    }
    if (rank < 1) {
      throw new DeokhugamException(
          ErrorCode.POPULAR_REVIEW_INVALID_RANK,
          Map.of("rank", rank)
      );
    }
    if (!Double.isFinite(score) || score < 0) {
      throw new DeokhugamException(
          ErrorCode.POPULAR_REVIEW_INVALID_SCORE,
          Map.of("score", String.valueOf(score))
      );
    }
    if (likeCount < 0) {
      throw new DeokhugamException(
          ErrorCode.POPULAR_REVIEW_INVALID_LIKE_COUNT,
          Map.of("likeCount", likeCount)
      );
    }
    if (commentCount < 0) {
      throw new DeokhugamException(
          ErrorCode.POPULAR_REVIEW_INVALID_COMMENT_COUNT,
          Map.of("commentCount", commentCount)
      );
    }
  }
}
