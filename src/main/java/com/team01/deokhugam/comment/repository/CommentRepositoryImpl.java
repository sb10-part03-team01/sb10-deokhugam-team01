package com.team01.deokhugam.comment.repository;

import static com.team01.deokhugam.comment.entity.QComment.comment;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team01.deokhugam.comment.dto.CommentSearchCondition;
import com.team01.deokhugam.comment.entity.Comment;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {
  // QueryDSL
  private final JPAQueryFactory queryFactory;

  @Override
  public List<Comment> findAllByCursor(CommentSearchCondition condition) {
    // 요청받은 limit, after, cursor 보정 및 변환
    int limit = condition.normalizedLimit();
    OffsetDateTime after = condition.after();
    UUID cursor = parseCursor(condition.cursor());

    // after나 cursor 둘이 같이 있어야하고 상태 다르면 바로 예외
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

    return queryFactory
        // select c from Comment c
        .selectFrom(comment)
        // 댓글 목록 응답에서 user 정보 필요 -> fetch join
        .join(comment.user)
        .fetchJoin()
        // Where 조건 -> 1. 특정 리뷰에 달린 2. 논리 삭제된 댓글이 아닌 3. 다음 페이지 요청이면 커서조건 적용
        .where(
            comment.review.id.eq(condition.reviewId()),
            comment.isDeleted.isFalse(),
            cursorCondition(after, cursor, condition.direction()))
        // createdAt 이후 id로 정렬
        .orderBy(orderSpecifiers(condition.direction()))
        .limit(limit + 1L)
        // 쿼리 실행 후 리스트 반환
        .fetch();
  }

  // 삭제되지 않은 댓글만 카운트
  @Override
  public long countCommentsByReviewId(UUID reviewId) {
    Long count =
        queryFactory
            // select count(comment)
            .select(comment.count())
            .from(comment)
            .where(comment.review.id.eq(reviewId), comment.isDeleted.isFalse())
            .fetchOne();

    return count != null ? count : 0L;
  }

  // 커서 페이지네이션 조건 생성 (after / parsedCursor / direction)
  // createdAt이 같은 경우 id를 보조 커서로 사용
  private BooleanExpression cursorCondition(
      OffsetDateTime after, UUID parsedCursor, SortDirection direction) {
    if (after == null || parsedCursor == null) {
      return null;
    }

    boolean isAsc = direction == SortDirection.ASC;

    return isAsc
        // created_at > after OR (created_at = after AND id > cursor)
        ? comment
            .createdAt
            .gt(after)
            .or(comment.createdAt.eq(after).and(comment.id.gt(parsedCursor)))
        : comment
            .createdAt
            .lt(after)
            .or(comment.createdAt.eq(after).and(comment.id.lt(parsedCursor)));
  }

  private OrderSpecifier<?>[] orderSpecifiers(SortDirection direction) {
    Order order = direction == SortDirection.ASC ? Order.ASC : Order.DESC;

    return new OrderSpecifier<?>[] {
      // ORDER BY created_at ASC, id ASC
      new OrderSpecifier<>(order, comment.createdAt), new OrderSpecifier<>(order, comment.id)
    };
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
