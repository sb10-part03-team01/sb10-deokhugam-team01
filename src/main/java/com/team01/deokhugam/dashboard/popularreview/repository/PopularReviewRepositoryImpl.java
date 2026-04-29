package com.team01.deokhugam.dashboard.popularreview.repository;

import static com.team01.deokhugam.dashboard.popularreview.entity.QPopularReview.popularReview;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team01.deokhugam.dashboard.popularreview.dto.PopularReviewSearchCondition;
import com.team01.deokhugam.dashboard.popularreview.entity.PopularReview;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PopularReviewRepositoryImpl implements PopularReviewRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<PopularReview> findAllByCondition(PopularReviewSearchCondition condition) {
    return queryFactory
        .selectFrom(popularReview)
        .join(popularReview.review).fetchJoin()
        .join(popularReview.review.book).fetchJoin()
        .join(popularReview.review.user).fetchJoin()
        .where(
            popularReview.period.eq(condition.period()),
            popularReview.calculatedDate.eq(condition.calculatedDate()),
            cursorCondition(condition)
        )
        .orderBy(orderSpecifiers(condition.direction()))
        .limit(condition.limit() + 1L)
        .fetch();
  }

  @Override
  public long countByCondition(PopularReviewSearchCondition condition) {
    Long count = queryFactory
        .select(popularReview.count())
        .from(popularReview)
        .where(
            popularReview.period.eq(condition.period()),
            popularReview.calculatedDate.eq(condition.calculatedDate())
        )
        .fetchOne();

    return count != null ? count : 0L;
  }

  private BooleanExpression cursorCondition(PopularReviewSearchCondition condition) {
    if (condition.cursor() == null || condition.cursor().isBlank()) {
      return null;
    }

    int cursorRank;
    try {
      cursorRank = Integer.parseInt(condition.cursor());
    } catch (NumberFormatException e) {
      throw new DeokhugamException(
          ErrorCode.INVALID_CURSOR_FORMAT,
          Map.of("cursor", condition.cursor())
      );
    }

    if (condition.after() == null) {
      return condition.direction() == SortDirection.DESC
          ? popularReview.rank.lt(cursorRank)
          : popularReview.rank.gt(cursorRank);
    }

    if (condition.direction() == SortDirection.DESC) {
      return popularReview.rank.lt(cursorRank)
          .or(
              popularReview.rank.eq(cursorRank)
                  .and(popularReview.createdAt.lt(condition.after()))
          );
    }

    return popularReview.rank.gt(cursorRank)
        .or(
            popularReview.rank.eq(cursorRank)
                .and(popularReview.createdAt.gt(condition.after()))
        );
  }

  private OrderSpecifier<?>[] orderSpecifiers(SortDirection direction) {
    if (direction == SortDirection.DESC) {
      return new OrderSpecifier<?>[]{
          popularReview.rank.desc(),
          popularReview.createdAt.desc()
      };
    }

    return new OrderSpecifier<?>[]{
        popularReview.rank.asc(),
        popularReview.createdAt.asc()
    };
  }
}
