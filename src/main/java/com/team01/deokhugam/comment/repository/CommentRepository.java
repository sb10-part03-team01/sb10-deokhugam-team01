package com.team01.deokhugam.comment.repository;

import com.team01.deokhugam.comment.entity.Comment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
  // soft delete 반영 및 리뷰 댓글 목록 조회
  List<Comment> findAllByReview_IdAndIsDeletedFalseOrderByCreatedAtDesc(UUID reviewId);

  // 단건 조회
  Optional<Comment> findByIdAndIsDeletedFalse(UUID id);
}
