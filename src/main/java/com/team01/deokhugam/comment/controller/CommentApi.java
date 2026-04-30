package com.team01.deokhugam.comment.controller;

import com.team01.deokhugam.comment.dto.CommentCreateRequest;
import com.team01.deokhugam.comment.dto.CommentDto;
import com.team01.deokhugam.comment.dto.CommentSearchRequest;
import com.team01.deokhugam.comment.dto.CommentUpdateRequest;
import com.team01.deokhugam.global.constant.AuthHeader;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;

@Tag(name = "댓글 관리", description = "댓글 관련 API")
public interface CommentApi {

  @Operation(summary = "리뷰 댓글 목록 조회", description = "특정 리뷰에 달린 댓글 목록을 시간순으로 조회합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공"),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 (정렬 방향 오류, 페이지네이션 파라미터 오류, 리뷰 ID 누락)",
        content = @Content),
    @ApiResponse(responseCode = "404", description = "리뷰 정보 없음", content = @Content),
    @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
  })
  ResponseEntity<CursorPageResponse<CommentDto>> getComments(
      @ParameterObject @Valid CommentSearchRequest request);

  @Operation(summary = "댓글 등록", description = "새로운 댓글을 등록합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "댓글 등록 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)", content = @Content),
    @ApiResponse(responseCode = "404", description = "리뷰 정보 없음", content = @Content),
    @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
  })
  ResponseEntity<CommentDto> createComment(
      @Parameter(
              name = AuthHeader.REQUEST_USER_ID,
              in = ParameterIn.HEADER,
              description = "요청자 ID",
              required = true)
          UUID userId,
      @Valid CommentCreateRequest request);

  @Operation(summary = "댓글 상세 정보 조회", description = "특정 댓글의 상세 정보를 조회합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "댓글 조회 성공"),
    @ApiResponse(responseCode = "404", description = "댓글 정보 없음", content = @Content),
    @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
  })
  ResponseEntity<CommentDto> getComment(
      @Parameter(description = "댓글 ID", required = true) UUID commentId);

  @Operation(summary = "댓글 수정", description = "본인이 작성한 댓글을 수정합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "댓글 수정 성공"),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 (입력값 검증 실패, 요청자 ID 누락)",
        content = @Content),
    @ApiResponse(responseCode = "403", description = "댓글 수정 권한 없음", content = @Content),
    @ApiResponse(responseCode = "404", description = "댓글 정보 없음", content = @Content),
    @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
  })
  ResponseEntity<CommentDto> updateComment(
      @Parameter(
              name = AuthHeader.REQUEST_USER_ID,
              in = ParameterIn.HEADER,
              description = "요청자 ID",
              required = true)
          UUID userId,
      @Parameter(description = "댓글 ID", required = true) UUID commentId,
      @Valid CommentUpdateRequest request);

  @Operation(summary = "댓글 논리 삭제", description = "본인이 작성한 댓글을 논리적으로 삭제합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "댓글 삭제 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)", content = @Content),
    @ApiResponse(responseCode = "403", description = "댓글 삭제 권한 없음", content = @Content),
    @ApiResponse(responseCode = "404", description = "댓글 정보 없음", content = @Content),
    @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
  })
  ResponseEntity<Void> deleteComment(
      @Parameter(
              name = AuthHeader.REQUEST_USER_ID,
              in = ParameterIn.HEADER,
              description = "요청자 ID",
              required = true)
          UUID userId,
      @Parameter(description = "댓글 ID", required = true) UUID commentId);

  @Operation(summary = "댓글 물리 삭제", description = "논리 삭제된 본인 댓글을 물리적으로 삭제합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "댓글 삭제 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)", content = @Content),
    @ApiResponse(responseCode = "403", description = "댓글 삭제 권한 없음", content = @Content),
    @ApiResponse(
        responseCode = "404",
        description = "댓글 정보 없음 또는 논리 삭제되지 않은 댓글",
        content = @Content),
    @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
  })
  ResponseEntity<Void> hardDeleteComment(
      @Parameter(
              name = AuthHeader.REQUEST_USER_ID,
              in = ParameterIn.HEADER,
              description = "요청자 ID",
              required = true)
          UUID userId,
      @Parameter(description = "댓글 ID", required = true) UUID commentId);
}
