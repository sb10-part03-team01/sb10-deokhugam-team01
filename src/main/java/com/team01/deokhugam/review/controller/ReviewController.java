package com.team01.deokhugam.review.controller;

import com.team01.deokhugam.global.constant.AuthHeader;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.review.dto.CursorPageResponseReviewDto;
import com.team01.deokhugam.review.dto.ReviewCreateRequest;
import com.team01.deokhugam.review.dto.ReviewDto;
import com.team01.deokhugam.review.dto.ReviewLikeDto;
import com.team01.deokhugam.review.dto.ReviewUpdateRequest;
import com.team01.deokhugam.review.service.ReviewService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
public class ReviewController {

  private final ReviewService reviewService;

  /*
  TODO ApiResponse 불일치는 나중에 코드 작성 마지막에
   errorCode에 커스템 객체를 만들어 대응할 예정
   */
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "리뷰 등록 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청(입력값 검증 실패)"),
      @ApiResponse(responseCode = "404", description = "도서 정보 없음"),
      @ApiResponse(responseCode = "409", description = "이미 작성된 리뷰 존재"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PostMapping
  public ResponseEntity<ReviewDto> createReview(
      @Valid @RequestBody ReviewCreateRequest request
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(request));
  }

  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "리뷰 상세 정보 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청(요청자 ID 누락)"),
      @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping("/{reviewId}")
  public ResponseEntity<ReviewDto> getReview(
      @PathVariable UUID reviewId,
      @RequestHeader(AuthHeader.REQUEST_USER_ID) UUID requestUserId
  ) {
    return ResponseEntity.ok(reviewService.getReview(reviewId, requestUserId));
  }

  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "리뷰 목록 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청(정렬 기준 오류, 페이지네이션 파라미터 오류, 요청자 ID누락)"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
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
    // 리턴 타입 의논
    return ResponseEntity.ok(reviewService.searchReviews(
            requestUserId,
            userId,
            bookId,
            keyword,
            orderBy,
            direction,
            cursor,
            after,
            limit
        )
    );
  }

  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "리뷰 수정 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)"),
      @ApiResponse(responseCode = "403", description = "리뷰 수정 권한 없음"),
      @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PatchMapping("/{reviewId}")
  public ResponseEntity<ReviewDto> updateReview(
      @PathVariable UUID reviewId,
      @RequestHeader(AuthHeader.REQUEST_USER_ID) UUID requestUserId,
      @Valid @RequestBody ReviewUpdateRequest request
  ) {
    ReviewDto response = reviewService.updateReview(reviewId, requestUserId, request);
    return ResponseEntity.ok(response);
  }

  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "리뷰 논리 삭제 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청(요청자 ID누락)"),
      @ApiResponse(responseCode = "403", description = "리뷰 삭제 권한 없음"),
      @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @DeleteMapping("/{reviewId}")
  public ResponseEntity<Void> deleteReview(
      @PathVariable UUID reviewId,
      @RequestHeader(AuthHeader.REQUEST_USER_ID) UUID requestUserId
  ) {
    reviewService.deleteReview(reviewId, requestUserId);
    return ResponseEntity.noContent().build();
  }

  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "리뷰 물리 삭제 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청(요청자 ID누락, 논리 삭제되지 않은 리뷰)"),
      @ApiResponse(responseCode = "403", description = "리뷰 삭제 권한 없음"),
      @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @DeleteMapping("/{reviewId}/hard")
  public ResponseEntity<Void> hardDeleteReview(
      @PathVariable UUID reviewId,
      @RequestHeader(AuthHeader.REQUEST_USER_ID) UUID requestUserId
  ) {
    reviewService.hardDeleteReview(reviewId, requestUserId);
    return ResponseEntity.noContent().build();
  }

  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "리뷰 좋아요 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청(요청자 ID 누락)"),
      @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PostMapping("/{reviewId}/like")
  public ResponseEntity<ReviewLikeDto> likeReview(
      @PathVariable UUID reviewId,
      @RequestHeader(AuthHeader.REQUEST_USER_ID) UUID requestUserId
  ) {
    return ResponseEntity.ok(reviewService.toggleLike(reviewId, requestUserId));
  }
}
