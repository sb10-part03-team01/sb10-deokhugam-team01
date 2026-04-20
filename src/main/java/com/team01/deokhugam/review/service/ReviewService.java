package com.team01.deokhugam.review.service;

import com.team01.deokhugam.review.dto.ReviewCreateRequest;
import com.team01.deokhugam.review.dto.ReviewDto;
import java.util.UUID;

public interface ReviewService {

  ReviewDto createReview(ReviewCreateRequest request);

  ReviewDto getReview(UUID reviewId, UUID requestUserId);

}
