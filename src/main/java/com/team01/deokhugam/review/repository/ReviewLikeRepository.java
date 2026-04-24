package com.team01.deokhugam.review.repository;

import com.team01.deokhugam.review.entity.ReviewLike;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, UUID> {

  Optional<ReviewLike> findByReviewIdAndUserId(UUID reviewId, UUID userId);

  boolean existsByReviewIdAndUserId(UUID reviewId, UUID requestUserId);
}
