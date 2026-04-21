package com.team01.deokhugam.comment.controller;

import com.team01.deokhugam.comment.dto.CommentCreateRequest;
import com.team01.deokhugam.comment.dto.CommentDto;
import com.team01.deokhugam.comment.dto.CommentUpdateRequest;
import com.team01.deokhugam.comment.service.CommentService;
import com.team01.deokhugam.global.constant.AuthHeader;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.pagination.CursorPageRequest;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
@Tag(name = "댓글 관리", description = "댓글 관련 API")
public class CommentController {

  private final CommentService commentService;

  // 댓글 목록 조회
  @GetMapping
  @Operation(summary = "리뷰 댓글 목록 조회", description = "특정 리뷰에 달린 댓글 목록을 시간순으로 조회합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공"),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 (정렬 방향 오류, 페이지네이션 파라미터 오류, 리뷰 ID 누락, 요청자 ID 누락)",
        content = @Content),
    @ApiResponse(responseCode = "404", description = "리뷰 정보 없음", content = @Content),
    @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
  })
  public ResponseEntity<CursorPageResponse<CommentDto>> getComments(
      @Parameter(description = "리뷰 ID", required = true) @RequestParam UUID reviewId,
      @Parameter(description = "정렬 방향", example = "DESC")
          @RequestParam(required = false, defaultValue = "DESC")
          SortDirection direction,
      @Parameter(description = "커서 페이지네이션 커서") @RequestParam(required = false) String cursor,
      @Parameter(description = "보조 커서(createdAt)") @RequestParam(required = false)
          OffsetDateTime after,
      @Parameter(description = "페이지 크기", example = "50")
          @RequestParam(required = false, defaultValue = "50")
          Integer limit) {

    CursorPageRequest pageRequest = new CursorPageRequest(cursor, after, limit);

    return ResponseEntity.ok(commentService.getComments(reviewId, pageRequest, direction));
  }

  // 댓글 등록
  @PostMapping
  @Operation(summary = "댓글 등록", description = "새로운 댓글을 등록합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "댓글 등록 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)", content = @Content),
    @ApiResponse(responseCode = "404", description = "리뷰 정보 없음", content = @Content),
    @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
  })
  public ResponseEntity<CommentDto> createComment(
      @Parameter(description = "요청자 ID", required = true) @RequestHeader(AuthHeader.REQUEST_USER_ID)
          UUID userId,
      @Valid @RequestBody CommentCreateRequest request) {
    return ResponseEntity.status(201).body(commentService.createComment(userId, request));
  }

  // 댓글 단건 조회
  @GetMapping("/{commentId}")
  @Operation(summary = "댓글 상세 정보 조회", description = "특정 댓글의 상세 정보를 조회합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "댓글 조회 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)", content = @Content),
    @ApiResponse(responseCode = "404", description = "댓글 정보 없음", content = @Content),
    @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
  })
  public ResponseEntity<CommentDto> getComment(
      @Parameter(description = "댓글 ID", required = true) @PathVariable UUID commentId) {
    return ResponseEntity.ok(commentService.getComment(commentId));
  }

  // 댓글 수정
  @PatchMapping("/{commentId}")
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
  public ResponseEntity<CommentDto> updateComment(
      @Parameter(description = "요청자 ID", required = true) @RequestHeader(AuthHeader.REQUEST_USER_ID)
          UUID userId,
      @Parameter(description = "댓글 ID", required = true) @PathVariable UUID commentId,
      @Valid @RequestBody CommentUpdateRequest request) {
    return ResponseEntity.ok(commentService.updateComment(userId, commentId, request));
  }

  // 댓글 논리 삭제
  @DeleteMapping("/{commentId}")
  @Operation(summary = "댓글 논리 삭제", description = "본인이 작성한 댓글을 논리적으로 삭제합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "댓글 삭제 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)", content = @Content),
    @ApiResponse(responseCode = "403", description = "댓글 삭제 권한 없음", content = @Content),
    @ApiResponse(responseCode = "404", description = "댓글 정보 없음", content = @Content),
    @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
  })
  public ResponseEntity<Void> deleteComment(
      @Parameter(description = "요청자 ID", required = true) @RequestHeader(AuthHeader.REQUEST_USER_ID)
          UUID userId,
      @Parameter(description = "댓글 ID", required = true) @PathVariable UUID commentId) {
    commentService.deleteComment(userId, commentId);
    return ResponseEntity.noContent().build();
  }

  // 댓글 물리 삭제
  @DeleteMapping("/{commentId}/hard")
  @Operation(summary = "댓글 물리 삭제", description = "본인이 작성한 댓글을 물리적으로 삭제합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "댓글 삭제 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)", content = @Content),
    @ApiResponse(responseCode = "403", description = "댓글 삭제 권한 없음", content = @Content),
    @ApiResponse(responseCode = "404", description = "댓글 정보 없음", content = @Content),
    @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
  })
  public ResponseEntity<Void> hardDeleteComment(
      @Parameter(description = "요청자 ID", required = true) @RequestHeader(AuthHeader.REQUEST_USER_ID)
          UUID userId,
      @Parameter(description = "댓글 ID", required = true) @PathVariable UUID commentId) {
    commentService.hardDeleteComment(userId, commentId);
    return ResponseEntity.noContent().build();
  }
}
