package com.team01.deokhugam.comment.repository;

import com.team01.deokhugam.comment.dto.CommentSearchCondition;
import com.team01.deokhugam.comment.dto.UserCommentCountRow;
import com.team01.deokhugam.comment.entity.Comment;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface CommentRepositoryCustom {
  // 페이지네이션
  List<Comment> findAllByCursor(CommentSearchCondition condition);

  // totalElements 용
  long countCommentsByReviewId(UUID reviewId);

  // 특정 기간(start 이상, end 미만) 동안 유저별 댓글 수를 집계한다.
  List<UserCommentCountRow> findCommentCountsByUserBetween(
      OffsetDateTime start, OffsetDateTime end);
}
