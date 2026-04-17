package com.team01.deokhugam.review.repository;

import com.team01.deokhugam.review.entity.Review;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
  boolean existsById(UUID id); // 선택
}
