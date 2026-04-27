package com.team01.deokhugam.batch.repository;

import static com.team01.deokhugam.book.entity.QBook.book;
import static com.team01.deokhugam.review.entity.QReview.review;

import com.querydsl.core.types.Projections;
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
    // 특정 기간(start~end) 에 작성된 리뷰 기준 -> 점수
    return queryFactory
        .select(
            Projections.constructor(
                PopularBookScoreRow.class,
                review.book.id,
                // 책별 리뷰 수
                review.count(),
                // 책별 평균 평점 -> null 이면 0.0
                review.rating.avg().coalesce(0.0),
                // 점수 계산 -> 리뷰수 * 0.4 + 평점 평균 * 0.6
                review
                    .count()
                    .doubleValue()
                    .multiply(0.4)
                    .add(review.rating.avg().coalesce(0.0).multiply(0.6))))
        // from Review review
        .from(review)
        // book 삭제 여부 위해 조인
        .join(review.book, book)
        .where(
            review.isDeleted.isFalse(),
            book.isDeleted.isFalse(),
            // goe -> >=
            review.createdAt.goe(start),
            // lt -> <
            review.createdAt.lt(end))
        .groupBy(review.book.id)
        // 정렬 기준 -> 점수 높은 순
        .orderBy(
            review
                .count()
                .doubleValue()
                .multiply(0.4)
                .add(review.rating.avg().coalesce(0.0).multiply(0.6))
                .desc(),
            // 평균 평점 높은 순
            review.rating.avg().coalesce(0.0).desc(),
            // 리뷰수 많은 순
            review.count().desc(),
            // id 오름차순
            review.book.id.asc())

        // 쿼리 실행 후 List<PopularBookScoreRos> 반환
        .fetch();
  }
}
