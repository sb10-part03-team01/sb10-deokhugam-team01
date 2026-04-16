package com.team01.deokhugam.comment.repository;

import com.team01.deokhugam.comment.dto.CommentSearchCondition;
import com.team01.deokhugam.comment.entity.Comment;
import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class CommentRepositoryImpl implements CommentRepositoryCustom {
  // JPQL
  @PersistenceContext private EntityManager em;

  @Override
  public List<Comment> findAllByCursor(CommentSearchCondition condition) {
    // 요청받은 limit 보정
    int limit = condition.normalizedLimit();
    OffsetDateTime after = condition.after();

    // 문자열 cursor UUID 변환
    UUID cursor = parseCursor(condition.cursor());

    // after나 cursor 둘이 상태다르면 바로 예외
    if ((after == null) != (cursor == null)) {
      Map<String, Object> details = new HashMap<>();
      if (condition.cursor() != null) {
        details.put("cursor", condition.cursor());
      }
      if (condition.after() != null) {
        details.put("after", condition.after());
      }
      throw new DeokhugamException(ErrorCode.INVALID_CURSOR_PAGINATION, details);
    }

    // ASC면 >  -> 다음 페이지는 더 큰 값 조회
    String comparisonOperator = condition.direction().isAscending() ? ">" : "<";

    // order by 에 들어갈 문자열
    String orderDirection = condition.direction().name(); // ASC or DESC

    // JQPL 삭제되지 않고 특정 리뷰의 댓글만 조회.
    // user fetch join
    StringBuilder jpql =
        new StringBuilder(
            """
        select c
        from Comment c
        join fetch c.user u
        where c.review.id = :reviewId
          and c.isDeleted = false
        """);

    // 커서 페이지네이션 after, cursor 둘다 있을떄만
    boolean hasCursorCondition = after != null;

    // createdAt 같다면 현재 커서위치만 id보다 큰 값
    if (hasCursorCondition) {
      jpql.append(
          """
          and (
            c.createdAt %s :after
            or (c.createdAt = :after and c.id %s :cursor)
          )
          """
              .formatted(comparisonOperator, comparisonOperator));
    }

    // createdAt + id로 정렬
    jpql.append(
        """

        order by c.createdAt %s, c.id %s
        """
            .formatted(orderDirection, orderDirection));

    // 쿼리 조합 -> limit + 1로 다음페이지 서비스에서 확인
    TypedQuery<Comment> query =
        em.createQuery(jpql.toString(), Comment.class)
            .setParameter("reviewId", condition.reviewId())
            .setMaxResults(limit + 1);

    if (hasCursorCondition) {
      query.setParameter("after", after);
      query.setParameter("cursor", cursor);
    }

    return query.getResultList();
  }

  // 삭제 x인 댓글만 카운트
  @Override
  public long countCommentsByReviewId(UUID reviewId) {
    return em.createQuery(
            """
            select count(c)
            from Comment c
            where c.review.id = :reviewId
              and c.isDeleted = false
            """,
            Long.class)
        .setParameter("reviewId", reviewId)
        .getSingleResult();
  }

  // 값이 있으면 UUID 변환
  private UUID parseCursor(String cursor) {
    if (!StringUtils.hasText(cursor)) {
      return null;
    }
    try {
      return UUID.fromString(cursor);
    } catch (IllegalArgumentException e) {
      throw new DeokhugamException(ErrorCode.INVALID_CURSOR_FORMAT, Map.of("cursor", cursor));
    }
  }
}
