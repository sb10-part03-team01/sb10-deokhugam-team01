package com.team01.deokhugam.comment.repository;

import com.team01.deokhugam.comment.dto.CommentSearchCondition;
import com.team01.deokhugam.comment.entity.Comment;
import java.util.List;
import java.util.UUID;

public interface CommentRepositoryCustom {
  // 페이지네이션
  List<Comment> findAllByCursor(CommentSearchCondition condition);

  // totalElements 용
  long countCommentsByReviewId(UUID reviewId);
}
