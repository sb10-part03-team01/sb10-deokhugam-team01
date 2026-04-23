package com.team01.deokhugam.dashboard.repository;

import com.team01.deokhugam.dashboard.entity.PopularBook;
import com.team01.deokhugam.global.enums.RankingPeriod;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class PopularBookRepositoryImpl implements PopularBookRepositoryCustom {

  @PersistenceContext private EntityManager em;

  // 특정 기간의 인기도서 목록 조회
  @Override
  public List<PopularBook> findAllByCursor(
      RankingPeriod period,
      SortDirection direction,
      String cursor,
      OffsetDateTime after,
      int limit) {

    // cursor를 rank로 변환
    Integer parsedCursor = parseCursor(cursor);

    // (cursor, after) 검증
    if ((after == null) != (parsedCursor == null)) {
      Map<String, Object> details = new HashMap<>();
      if (parsedCursor != null) {
        details.put("cursor", cursor);
      }
      if (after != null) {
        details.put("after", after);
      }
      throw new DeokhugamException(ErrorCode.INVALID_CURSOR_PAGINATION, details);
    }

    String comparisonOperator = direction == SortDirection.ASC ? ">" : "<";
    String orderDirection = direction.name();

    // 최신 calculated_date 기준 + rank 정렬
    StringBuilder jpql =
        new StringBuilder(
            """
            select pb
            from PopularBook pb
            join fetch pb.book b
            where pb.periodType = :period
              and pb.calculatedDate = (
                select max(pb2.calculatedDate)
                from PopularBook pb2
                where pb2.periodType = :period
              )
            """);

    boolean hasCursorCondition = parsedCursor != null;

    // 커서 조건: rank 기준
    if (hasCursorCondition) {
      jpql.append(
          """
            and pb.rank %s :cursor
          """
              .formatted(comparisonOperator));
    }

    // 정렬: rank 기준
    jpql.append(
        """

        order by pb.rank %s
        """
            .formatted(orderDirection));

    TypedQuery<PopularBook> query =
        em.createQuery(jpql.toString(), PopularBook.class)
            .setParameter("period", period)
            .setMaxResults(limit + 1);

    if (hasCursorCondition) {
      query.setParameter("cursor", parsedCursor);
    }

    return query.getResultList();
  }

  // 특정 기간(period) 인기 도서 중, 가장 최신 calculated_date 기준 데이터 개수 세는 메서드
  @Override
  public long countByPeriod(RankingPeriod period) {
    return em.createQuery(
            """
            select count(pb)
            from PopularBook pb
            where pb.periodType = :period
              and pb.calculatedDate = (
                select max(pb2.calculatedDate)
                from PopularBook pb2
                where pb2.periodType = :period
              )
            """,
            Long.class)
        .setParameter("period", period)
        .getSingleResult();
  }

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
