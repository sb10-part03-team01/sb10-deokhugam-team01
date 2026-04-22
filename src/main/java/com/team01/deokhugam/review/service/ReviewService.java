package com.team01.deokhugam.review.service;

import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.review.dto.CursorPageResponseReviewDto;
import com.team01.deokhugam.review.dto.ReviewCreateRequest;
import com.team01.deokhugam.review.dto.ReviewDto;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface ReviewService {

  ReviewDto createReview(ReviewCreateRequest request);

  ReviewDto getReview(UUID reviewId, UUID requestUserId);

  CursorPageResponseReviewDto searchReviews(
      UUID requestUserId,
      UUID userId,
      UUID bookId,
      String keyword,
      String orderBy,
      SortDirection direction,
      String cursor,
      OffsetDateTime after,
      Integer limit
  );
}
