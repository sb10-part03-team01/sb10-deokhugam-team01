package com.team01.deokhugam.review.repository;

import com.team01.deokhugam.review.entity.Review;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, UUID>, ReviewRepositoryCustom {

  boolean existsByBookIdAndUserIdAndIsDeletedFalse(UUID bookId, UUID userId);

  Optional<Review> findByIdAndIsDeletedFalse(UUID reviewId);
}
