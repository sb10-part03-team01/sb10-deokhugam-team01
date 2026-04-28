package com.team01.deokhugam.dashboard.popularbook.repository;

import static com.team01.deokhugam.dashboard.popularbook.entity.QPopularBook.popularBook;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.dashboard.popularbook.entity.PopularBook;
import com.team01.deokhugam.dashboard.popularbook.entity.QPopularBook;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class PopularBookRepositoryImpl implements PopularBookRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  // 특정 기간의 인기 도서 목록을 최신 스냅샷 기준으로 커서 페이지네이션 조회한다.
  @Override
  public List<PopularBook> findAllByCursor(
      DashboardPeriod period,
      SortDirection direction,
      String cursor,
      OffsetDateTime after,
      int limit) {

    // 인기 도서 커서는 rank를 의미한다.
    Integer parsedCursor = parseCursor(cursor);

    // 현재 인기 도서 페이지네이션은 rank만 커서로 사용하므로,
    // cursor와 after는 함께 오거나 함께 비어 있어야 한다.
    if ((after == null) != (parsedCursor == null)) {
      Map<String, Object> details = new HashMap<>();
      if (cursor != null) {
        details.put("cursor", cursor);
      }
      if (after != null) {
        details.put("after", after);
      }
      throw new DeokhugamException(ErrorCode.INVALID_CURSOR_PAGINATION, details);
    }

    QPopularBook pbSub = new QPopularBook("pbSub");

    return queryFactory
        .selectFrom(popularBook)
        // 응답에서 book 정보를 바로 사용하므로 fetch join 한다.
        .join(popularBook.book)
        .fetchJoin()
        .where(
            popularBook.periodType.eq(period),
            // 해당 기간의 최신 calculatedDate 스냅샷만 조회한다.
            popularBook.calculatedDate.eq(
                JPAExpressions.select(pbSub.calculatedDate.max())
                    .from(pbSub)
                    .where(pbSub.periodType.eq(period))),
            rankCursorCondition(parsedCursor, direction))
        // rank 기준으로 정렬한다.
        .orderBy(rankOrder(direction))
        .limit(limit + 1L)
        .fetch();
  }

  // 특정 기간의 최신 인기 도서 스냅샷 개수를 반환한다.
  @Override
  public long countByPeriod(DashboardPeriod period) {
    QPopularBook pbSub = new QPopularBook("pbSub");

    Long count =
        queryFactory
            .select(popularBook.count())
            .from(popularBook)
            .where(
                popularBook.periodType.eq(period),
                popularBook.calculatedDate.eq(
                    JPAExpressions.select(pbSub.calculatedDate.max())
                        .from(pbSub)
                        .where(pbSub.periodType.eq(period))))
            .fetchOne();

    return count != null ? count : 0L;
  }

  // rank 커서 조건을 생성한다.
  private BooleanExpression rankCursorCondition(Integer cursor, SortDirection direction) {
    if (cursor == null) {
      return null;
    }

    return direction == SortDirection.ASC
        ? popularBook.rank.gt(cursor)
        : popularBook.rank.lt(cursor);
  }

  // rank 정렬 방향을 생성한다.
  private OrderSpecifier<Integer> rankOrder(SortDirection direction) {
    Order order = direction == SortDirection.ASC ? Order.ASC : Order.DESC;
    return new OrderSpecifier<>(order, popularBook.rank);
  }

  // 문자열 cursor를 rank 정수로 변환한다.
  private Integer parseCursor(String cursor) {
    if (!StringUtils.hasText(cursor)) {
      return null;
    }

    try {
      return Integer.parseInt(cursor);
    } catch (NumberFormatException e) {
      Map<String, Object> details = new HashMap<>();
      details.put("cursor", cursor);
      details.put("rule", "cursor는 rank를 의미하는 정수 형식이어야 합니다.");
      throw new DeokhugamException(ErrorCode.INVALID_CURSOR_FORMAT, details);
    }
  }
}
