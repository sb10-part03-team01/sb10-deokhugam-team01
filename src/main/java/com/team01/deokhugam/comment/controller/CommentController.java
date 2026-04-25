package com.team01.deokhugam.comment.controller;

import com.team01.deokhugam.comment.dto.CommentCreateRequest;
import com.team01.deokhugam.comment.dto.CommentDto;
import com.team01.deokhugam.comment.dto.CommentUpdateRequest;
import com.team01.deokhugam.comment.service.CommentService;
import com.team01.deokhugam.global.constant.AuthHeader;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.pagination.CursorPageRequest;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/api/comments")
public class CommentController implements CommentApi {

  private final CommentService commentService;

  // 댓글 목록 조회
  @Override
  @GetMapping
  public ResponseEntity<CursorPageResponse<CommentDto>> getComments(
      @RequestParam UUID reviewId,
      @RequestParam(required = false, defaultValue = "DESC") SortDirection direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) OffsetDateTime after,
      @RequestParam(required = false, defaultValue = "50") Integer limit) {

    log.info(
        "[COMMENT] getComments reviewId={}, direction={}, cursor={}, after={}, limit={}",
        reviewId,
        direction,
        cursor,
        after,
        limit);

    CursorPageRequest pageRequest = new CursorPageRequest(cursor, after, limit);
    CursorPageResponse<CommentDto> response =
        commentService.getComments(reviewId, pageRequest, direction);

    return ResponseEntity.ok(response);
  }

  // 댓글 생성
  @Override
  @PostMapping
  public ResponseEntity<CommentDto> createComment(
      @RequestHeader(AuthHeader.REQUEST_USER_ID) UUID userId,
      @Valid @RequestBody CommentCreateRequest request) {
    return ResponseEntity.status(201).body(commentService.createComment(userId, request));
  }

  // 댓글 단건 조회
  @Override
  @GetMapping("/{commentId}")
  public ResponseEntity<CommentDto> getComment(@PathVariable UUID commentId) {
    return ResponseEntity.ok(commentService.getComment(commentId));
  }

  // 댓글 수정
  @Override
  @PatchMapping("/{commentId}")
  public ResponseEntity<CommentDto> updateComment(
      @RequestHeader(AuthHeader.REQUEST_USER_ID) UUID userId,
      @PathVariable UUID commentId,
      @Valid @RequestBody CommentUpdateRequest request) {
    return ResponseEntity.ok(commentService.updateComment(userId, commentId, request));
  }

  // 댓글 논리 삭제
  @Override
  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @RequestHeader(AuthHeader.REQUEST_USER_ID) UUID userId, @PathVariable UUID commentId) {
    commentService.deleteComment(userId, commentId);
    return ResponseEntity.noContent().build();
  }

  // 댓글 물리 삭제
  @Override
  @DeleteMapping("/{commentId}/hard")
  public ResponseEntity<Void> hardDeleteComment(
      @RequestHeader(AuthHeader.REQUEST_USER_ID) UUID userId, @PathVariable UUID commentId) {
    commentService.hardDeleteComment(userId, commentId);
    return ResponseEntity.noContent().build();
  }
}
