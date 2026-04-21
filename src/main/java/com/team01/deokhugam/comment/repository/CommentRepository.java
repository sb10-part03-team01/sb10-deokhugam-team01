package com.team01.deokhugam.comment.repository;

import com.team01.deokhugam.comment.entity.Comment;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CommentRepository extends JpaRepository<Comment, UUID>, CommentRepositoryCustom {

  // 상세 조회,수정, 삭제 (fetch join) isDeleted = false 찾는 용
  @Query(
      """
    select c
    from Comment c
    join fetch c.user
    join fetch c.review
    where c.id = :id
      and c.isDeleted = false
    """)
  Optional<Comment> findDetailById(UUID id);

  //
  Optional<Comment> findByIdAndIsDeletedTrue(UUID id);
}
