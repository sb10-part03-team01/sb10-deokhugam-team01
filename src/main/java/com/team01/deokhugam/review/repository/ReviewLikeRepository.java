package com.team01.deokhugam.review.repository;

import com.team01.deokhugam.review.entity.ReviewLike;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, UUID> {

  Optional<ReviewLike> findByReviewIdAndUserId(UUID reviewId, UUID userId);

  boolean existsByReviewIdAndUserId(UUID reviewId, UUID requestUserId);

  @Query("""
      select rl.review.id
      from ReviewLike rl
      where rl.review.id in :reviewIds
        and rl.user.id = :userId
      """)
  List<UUID> findLikedReviewIdsByReviewIdInAndUserId(Collection<UUID> reviewIds, UUID userId);
}
