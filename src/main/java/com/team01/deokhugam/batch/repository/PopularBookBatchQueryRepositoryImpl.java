package com.team01.deokhugam.batch.repository;

import static com.team01.deokhugam.book.entity.QBook.book;
import static com.team01.deokhugam.review.entity.QReview.review;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team01.deokhugam.batch.dto.PopularBookScoreRow;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PopularBookBatchQueryRepositoryImpl implements PopularBookBatchQueryRepositoryCustom {
  private final JPAQueryFactory queryFactory;

  @Override
  public List<PopularBookScoreRow> findPopularBooksBetween(
      OffsetDateTime start, OffsetDateTime end) {

    // 평균 평점이 null -> 0.0으로 보정
    NumberExpression<Double> averageRating = review.rating.avg().coalesce(0.0);
    // 점수 계산 식
    NumberExpression<Double> score =
        review.count().doubleValue().multiply(0.4).add(averageRating.multiply(0.6));

    // 특정 기간(start~end) 에 작성된 리뷰 기준 -> 점수
    return queryFactory
        .select(
            Projections.constructor(
                PopularBookScoreRow.class,
                review.book.id,
                // 책별 리뷰 수
                review.count(),
                // 책별 평균 평점
                averageRating,
                // 점수 = 리뷰 수 * 0.4 + 평균 평점 * 0.6
                score))
        // 리뷰를 기준으로 집계한다.
        .from(review)
        // 책 삭제 여부 조건을 함께 보기 위해 조인한다.
        .join(review.book, book)
        .where(
            review.isDeleted.isFalse(),
            book.isDeleted.isFalse(),
            // 집계 시작 시각 이상
            review.createdAt.goe(start),
            // 집계 종료 시각 미만
            review.createdAt.lt(end))
        .groupBy(review.book.id)
        .orderBy(
            // 점수 높은 순
            score.desc(),
            // 평균 평점 높은 순
            averageRating.desc(),
            // 리뷰 수 많은 순
            review.count().desc(),
            // 마지막 tie-breaker
            review.book.id.asc())
        .fetch();
  }
}
