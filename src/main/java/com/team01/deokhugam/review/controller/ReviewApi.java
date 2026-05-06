package com.team01.deokhugam.review.controller;

import com.team01.deokhugam.global.constant.AuthHeader;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.review.dto.CursorPageResponseReviewDto;
import com.team01.deokhugam.review.dto.ReviewCreateRequest;
import com.team01.deokhugam.review.dto.ReviewDto;
import com.team01.deokhugam.review.dto.ReviewLikeDto;
import com.team01.deokhugam.review.dto.ReviewUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "리뷰 관리", description = "리뷰 관련 API")
public interface ReviewApi {

  @Operation(summary = "리뷰 목록 조회", description = "조건에 맞는 리뷰 목록을 커서 페이지네이션 방식으로 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "리뷰 목록 조회 성공",
          content = @Content(schema = @Schema(implementation = CursorPageResponseReviewDto.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<CursorPageResponseReviewDto> getReviews(
      @Parameter(
          name = AuthHeader.REQUEST_USER_ID,
          description = "요청자 ID",
          required = true,
          in = ParameterIn.HEADER
      )
      UUID requestUserId,

      @Parameter(description = "작성자 ID") UUID userId,
      @Parameter(description = "도서 ID") UUID bookId,
      @Parameter(description = "검색어 - 작성자 닉네임, 리뷰 내용, 도서 제목") String keyword,
      @Parameter(description = "정렬 기준: createdAt, rating", example = "createdAt") String orderBy,
      @Parameter(description = "정렬 방향: ASC, DESC", example = "DESC") SortDirection direction,
      @Parameter(description = "다음 페이지 조회를 위한 커서") String cursor,
      @Parameter(description = "보조 커서 - 이전 페이지 마지막 항목의 생성 일시") OffsetDateTime after,
      @Parameter(description = "페이지 크기", example = "50") Integer limit
  );

  @Operation(summary = "리뷰 등록", description = "도서에 대한 리뷰를 등록합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "리뷰 등록 성공",
          content = @Content(schema = @Schema(implementation = ReviewDto.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "404", description = "도서 또는 사용자 정보 없음"),
      @ApiResponse(responseCode = "409", description = "이미 작성된 리뷰 존재"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<ReviewDto> createReview(
      @Valid
      @RequestBody
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "리뷰 등록 요청 정보",
          required = true,
          content = @Content(schema = @Schema(implementation = ReviewCreateRequest.class))
      )
      ReviewCreateRequest request
  );

  @Operation(summary = "리뷰 좋아요", description = "요청자가 특정 리뷰에 좋아요를 추가하거나 취소합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "리뷰 좋아요 토글 성공",
          content = @Content(schema = @Schema(implementation = ReviewLikeDto.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<ReviewLikeDto> likeReview(
      @Parameter(description = "리뷰 ID", required = true)
      UUID reviewId,

      @Parameter(
          name = AuthHeader.REQUEST_USER_ID,
          description = "요청자 ID",
          required = true,
          in = ParameterIn.HEADER
      )
      UUID requestUserId
  );

  @Operation(summary = "리뷰 상세 정보 조회", description = "리뷰 ID로 리뷰 상세 정보를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "리뷰 상세 정보 조회 성공",
          content = @Content(schema = @Schema(implementation = ReviewDto.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<ReviewDto> getReview(
      @Parameter(description = "리뷰 ID", required = true)
      UUID reviewId,

      @Parameter(
          name = AuthHeader.REQUEST_USER_ID,
          description = "요청자 ID",
          required = true,
          in = ParameterIn.HEADER
      )
      UUID requestUserId
  );

  @Operation(summary = "리뷰 논리 삭제", description = "리뷰를 논리 삭제 처리합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "리뷰 논리 삭제 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "403", description = "리뷰 삭제 권한 없음"),
      @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<Void> deleteReview(
      @Parameter(description = "리뷰 ID", required = true)
      UUID reviewId,

      @Parameter(
          name = AuthHeader.REQUEST_USER_ID,
          description = "요청자 ID",
          required = true,
          in = ParameterIn.HEADER
      )
      UUID requestUserId
  );

  @Operation(summary = "리뷰 수정", description = "본인이 작성한 리뷰의 내용과 평점을 수정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "리뷰 수정 성공",
          content = @Content(schema = @Schema(implementation = ReviewDto.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "403", description = "리뷰 수정 권한 없음"),
      @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<ReviewDto> updateReview(
      @Parameter(description = "리뷰 ID", required = true)
      UUID reviewId,

      @Parameter(
          name = AuthHeader.REQUEST_USER_ID,
          description = "요청자 ID",
          required = true,
          in = ParameterIn.HEADER
      )
      UUID requestUserId,

      @Valid
      @RequestBody
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "리뷰 수정 요청 정보",
          required = true,
          content = @Content(schema = @Schema(implementation = ReviewUpdateRequest.class))
      )
      ReviewUpdateRequest request
  );

  @Operation(summary = "리뷰 물리 삭제", description = "논리 삭제된 리뷰를 물리 삭제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "리뷰 물리 삭제 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "403", description = "리뷰 삭제 권한 없음"),
      @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<Void> hardDeleteReview(
      @Parameter(description = "리뷰 ID", required = true)
      UUID reviewId,

      @Parameter(
          name = AuthHeader.REQUEST_USER_ID,
          description = "요청자 ID",
          required = true,
          in = ParameterIn.HEADER
      )
      UUID requestUserId
  );
}
