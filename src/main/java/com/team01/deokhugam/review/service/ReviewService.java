package com.team01.deokhugam.review.service;

import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.review.dto.CursorPageResponseReviewDto;
import com.team01.deokhugam.review.dto.ReviewCreateRequest;
import com.team01.deokhugam.review.dto.ReviewDto;
import com.team01.deokhugam.review.dto.ReviewLikeDto;
import com.team01.deokhugam.review.dto.ReviewUpdateRequest;
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

  ReviewDto updateReview(UUID reviewId, UUID requestUserId, ReviewUpdateRequest request);

  // 논리 삭제
  void deleteReview(UUID reviewId, UUID requestUserId);

  // 물리 삭제
  void hardDeleteReview(UUID reviewId, UUID requestUserId);

  // 리뷰 토글
  ReviewLikeDto toggleLike(UUID reviewId, UUID requestUserId);
}
