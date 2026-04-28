package com.team01.deokhugam.batch.repository;

import com.team01.deokhugam.review.entity.Review;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopularBookBatchQueryRepository
    extends JpaRepository<Review, UUID>, PopularBookBatchQueryRepositoryCustom {}
