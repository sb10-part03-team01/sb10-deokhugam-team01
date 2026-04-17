package com.team01.deokhugam.review.repository;

import com.team01.deokhugam.review.entity.Review;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

  // 같은 사용자가 같은 책에 리뷰를 이미 썼는지 확인
  boolean existsByBook_IdAndUser_IdAndIsDeletedFalse(UUID bookId, UUID userId);

  // 삭제되지 않은 리뷰 1건 조회
  Optional<Review> findByIdAndIsDeletedFalse(UUID reviewId);
}