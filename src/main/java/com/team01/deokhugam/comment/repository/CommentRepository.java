package com.team01.deokhugam.comment.repository;

import com.team01.deokhugam.comment.entity.Comment;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, UUID>, CommentRepositoryCustom {
  // 단건 조회
  Optional<Comment> findByIdAndIsDeletedFalse(UUID id);
}
