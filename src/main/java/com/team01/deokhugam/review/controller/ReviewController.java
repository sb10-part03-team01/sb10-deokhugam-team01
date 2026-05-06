package com.team01.deokhugam.review.controller;

import com.team01.deokhugam.global.constant.AuthHeader;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.review.dto.CursorPageResponseReviewDto;
import com.team01.deokhugam.review.dto.ReviewCreateRequest;
import com.team01.deokhugam.review.dto.ReviewDto;
import com.team01.deokhugam.review.dto.ReviewLikeDto;
import com.team01.deokhugam.review.dto.ReviewUpdateRequest;
import com.team01.deokhugam.review.service.ReviewService;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController implements ReviewApi {

  private final ReviewService reviewService;

  @Override
  @GetMapping
  public ResponseEntity<CursorPageResponseReviewDto> getReviews(
      @RequestHeader(AuthHeader.REQUEST_USER_ID) UUID requestUserId,
      @RequestParam(required = false) UUID userId,
      @RequestParam(required = false) UUID bookId,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "createdAt") String orderBy,
      @RequestParam(defaultValue = "DESC") SortDirection direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) OffsetDateTime after,
      @RequestParam(required = false) Integer limit
  ) {
    CursorPageResponseReviewDto response = reviewService.searchReviews(
        requestUserId,
        userId,
        bookId,
        keyword,
        orderBy,
        direction,
        cursor,
        after,
        limit
    );

    log.info(
        "리뷰 목록 조회 성공: requestUserId={}, userId={}, bookId={}, keyword={}",
        requestUserId,
        userId,
        bookId,
        keyword
    );

    return ResponseEntity.ok(response);
  }

  @Override
  @PostMapping
  public ResponseEntity<ReviewDto> createReview(
      @Valid @RequestBody ReviewCreateRequest request
  ) {
    ReviewDto response = reviewService.createReview(request);

    log.info(
        "리뷰 등록 성공, reviewId={}, bookId={}, userId={}",
        response.id(),
        response.bookId(),
        response.userId()
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Override
  @PostMapping("/{reviewId}/like")
  public ResponseEntity<ReviewLikeDto> likeReview(
      @PathVariable UUID reviewId,
      @RequestHeader(AuthHeader.REQUEST_USER_ID) UUID requestUserId
  ) {
    ReviewLikeDto response = reviewService.toggleLike(reviewId, requestUserId);

    log.info(
        "리뷰 좋아요 토글 성공: reviewId={}, requestUserId={}, liked={}",
        reviewId,
        requestUserId,
        response.liked()
    );

    return ResponseEntity.ok(response);
  }

  @Override
  @GetMapping("/{reviewId}")
  public ResponseEntity<ReviewDto> getReview(
      @PathVariable UUID reviewId,
      @RequestHeader(AuthHeader.REQUEST_USER_ID) UUID requestUserId
  ) {
    ReviewDto response = reviewService.getReview(reviewId, requestUserId);

    log.info("리뷰 조회 성공, reviewId={}, userId={}", reviewId, requestUserId);

    return ResponseEntity.ok(response);
  }

  @Override
  @DeleteMapping("/{reviewId}")
  public ResponseEntity<Void> deleteReview(
      @PathVariable UUID reviewId,
      @RequestHeader(AuthHeader.REQUEST_USER_ID) UUID requestUserId
  ) {
    reviewService.deleteReview(reviewId, requestUserId);

    log.info("리뷰 논리삭제 성공 requestUserId={}, reviewId={}", requestUserId, reviewId);

    return ResponseEntity.noContent().build();
  }

  @Override
  @PatchMapping("/{reviewId}")
  public ResponseEntity<ReviewDto> updateReview(
      @PathVariable UUID reviewId,
      @RequestHeader(AuthHeader.REQUEST_USER_ID) UUID requestUserId,
      @Valid @RequestBody ReviewUpdateRequest request
  ) {
    ReviewDto response = reviewService.updateReview(reviewId, requestUserId, request);

    log.info(
        "리뷰 수정 성공 requestUserId={}, reviewId={}, userId={}",
        requestUserId,
        reviewId,
        response.userId()
    );

    return ResponseEntity.ok(response);
  }

  @Override
  @DeleteMapping("/{reviewId}/hard")
  public ResponseEntity<Void> hardDeleteReview(
      @PathVariable UUID reviewId,
      @RequestHeader(AuthHeader.REQUEST_USER_ID) UUID requestUserId
  ) {
    reviewService.hardDeleteReview(reviewId, requestUserId);

    log.info("리뷰 물리삭제 성공 requestUserId={}, reviewId={}", requestUserId, reviewId);

    return ResponseEntity.noContent().build();
  }
}
