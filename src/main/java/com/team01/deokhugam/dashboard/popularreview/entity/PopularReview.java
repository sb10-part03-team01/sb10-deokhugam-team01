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
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "popular_reviews",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_popular_reviews_review_period",
            columnNames = {"review_id", "period"}
        ),
        @UniqueConstraint(
            name = "uk_popular_reviews_period_rank",
            columnNames = {"period", "ranking"}
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
  @Column(name = "period", nullable = false, length = 20)
  private DashboardPeriod period;

  @Column(name = "ranking", nullable = false)
  private int rank;

  @Column(name = "score", nullable = false)
  private double score;

  @Column(name = "like_count", nullable = false)
  private int likeCount;

  @Column(name = "comment_count", nullable = false)
  private int commentCount;

  public PopularReview(
      Review review,
      DashboardPeriod period,
      int rank,
      double score,
      int likeCount,
      int commentCount
  ) {
    validate(review, period, rank, score, likeCount, commentCount);

    this.review = review;
    this.period = period;
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
    validate(this.review, this.period, rank, score, likeCount, commentCount);

    this.rank = rank;
    this.score = score;
    this.likeCount = likeCount;
    this.commentCount = commentCount;
  }

  private void validate(
      Review review,
      DashboardPeriod period,
      int rank,
      double score,
      int likeCount,
      int commentCount
  ) {
    if (review == null) {
      throw new DeokhugamException(
          ErrorCode.POPULAR_REVIEW_REVIEW_REQUIRED,
          Map.of()
      );
    }
    if (period == null) {
      throw new DeokhugamException(
          ErrorCode.POPULAR_REVIEW_PERIOD_REQUIRED,
          Map.of()
      );
    }
    if (rank < 1) {
      throw new DeokhugamException(
          ErrorCode.POPULAR_REVIEW_INVALID_RANK,
          Map.of("rank", rank)
      );
    }
    if (!Double.isFinite(score)) {
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
