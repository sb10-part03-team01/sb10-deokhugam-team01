package com.team01.deokhugam.batch.repository;

import static com.team01.deokhugam.dashboard.popularreview.entity.QPopularReview.popularReview;
import static com.team01.deokhugam.review.entity.QReview.review;
import static com.team01.deokhugam.review.entity.QReviewLike.reviewLike;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.batch.dto.UserActivityCountRow;
import com.team01.deokhugam.batch.dto.UserReviewScoreSumRow;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PowerUserBatchQueryRepositoryImpl implements PowerUserBatchQueryRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<UserReviewScoreSumRow> findReviewScoreSumsByUser(
      DashboardPeriod period,
      LocalDate calculatedDate
  ) {
    return queryFactory
        .select(Projections.constructor(
            UserReviewScoreSumRow.class,
            popularReview.review.user.id,
            popularReview.score.sum().coalesce(0.0)
        ))
        .from(popularReview)
        .where(
            popularReview.period.eq(period),
            popularReview.calculatedDate.eq(calculatedDate),
            popularReview.review.isDeleted.isFalse(),
            popularReview.review.user.isDeleted.isFalse()
        )
        .groupBy(popularReview.review.user.id)
        .fetch();
  }

  @Override
  public List<UserActivityCountRow> findLikeCountsByUserBetween(
      OffsetDateTime start,
      OffsetDateTime end
  ) {
    return queryFactory
        .select(Projections.constructor(
            UserActivityCountRow.class,
            reviewLike.review.user.id,
            reviewLike.count()
        ))
        .from(reviewLike)
        .join(reviewLike.review, review)
        .where(
            reviewLike.createdAt.goe(start),
            reviewLike.createdAt.lt(end),
            review.isDeleted.isFalse(),
            review.user.isDeleted.isFalse()
        )
        .groupBy(reviewLike.review.user.id)
        .fetch();
  }
}
