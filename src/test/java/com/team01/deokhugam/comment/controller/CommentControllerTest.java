package com.team01.deokhugam.comment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team01.deokhugam.comment.dto.CommentCreateRequest;
import com.team01.deokhugam.comment.dto.CommentDto;
import com.team01.deokhugam.comment.dto.CommentUpdateRequest;
import com.team01.deokhugam.comment.service.CommentService;
import com.team01.deokhugam.global.constant.AuthHeader;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import com.team01.deokhugam.global.pagination.CursorPageRequest;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CommentController.class)
public class CommentControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private CommentService commentService;

  private static final String USER_ID_HEADER = AuthHeader.REQUEST_USER_ID;

  private CommentDto createCommentDto(UUID commentId, UUID reviewId, UUID userId, String content) {
    return new CommentDto(
        commentId,
        reviewId,
        userId,
        "jongin",
        content,
        OffsetDateTime.of(2026, 4, 20, 12, 0, 0, 0, ZoneOffset.UTC),
        OffsetDateTime.of(2026, 4, 20, 12, 0, 0, 0, ZoneOffset.UTC));
  }

  // ========= Comment 등록 테스트 =========

  @Test
  @DisplayName("댓글 등록 성공")
  void create_comment_success() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();
    UUID commentId = UUID.randomUUID();

    CommentCreateRequest request = new CommentCreateRequest(reviewId, "댓글");
    CommentDto response = createCommentDto(commentId, reviewId, userId, "댓글");

    given(commentService.createComment(eq(userId), any(CommentCreateRequest.class)))
        .willReturn(response);

    // when // then
    mockMvc
        .perform(
            post("/api/comments")
                .header(USER_ID_HEADER, userId.toString())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(commentId.toString()))
        .andExpect(jsonPath("$.reviewId").value(reviewId.toString()))
        .andExpect(jsonPath("$.userId").value(userId.toString()))
        .andExpect(jsonPath("$.userNickname").value("jongin"))
        .andExpect(jsonPath("$.content").value("댓글"));
  }

  @Test
  @DisplayName("댓글 등록 실패(400) - 사용자 ID 헤더 누락")
  void create_comment_fail_without_user_id_header() throws Exception {
    // given
    UUID reviewId = UUID.randomUUID();
    CommentCreateRequest request = new CommentCreateRequest(reviewId, "댓글");

    // when , then
    mockMvc
        .perform(
            post("/api/comments")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
    // 호출 횟수 검증
    verify(commentService, never()).createComment(any(), any());
  }

  @Test
  @DisplayName("댓글 등록 실패(400) - content가 비어있음")
  void create_comment_fail_when_content_is_blank() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();
    CommentCreateRequest request = new CommentCreateRequest(reviewId, "");

    // when , then
    mockMvc
        .perform(
            post("/api/comments")
                .header(USER_ID_HEADER, userId.toString())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
    verify(commentService, never()).createComment(any(), any());
  }

  @Test
  @DisplayName("댓글 등록 실패(404) - 존재하지 않는 리뷰면")
  void create_comment_fail_when_review_not_found() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();
    CommentCreateRequest request = new CommentCreateRequest(reviewId, "댓글");

    given(commentService.createComment(eq(userId), any(CommentCreateRequest.class)))
        .willThrow(
            new DeokhugamException(
                ErrorCode.REVIEW_NOT_FOUND, Map.of("reviewId", reviewId.toString())));

    // when , then
    mockMvc
        .perform(
            post("/api/comments")
                .header(USER_ID_HEADER, userId.toString())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  // ========= Comment 수정 테스트 =========
  @Test
  @DisplayName("댓글 수정 성공")
  void update_comment_success() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();
    UUID commentId = UUID.randomUUID();

    CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글");
    CommentDto response = createCommentDto(commentId, reviewId, userId, "수정된 댓글");

    given(commentService.updateComment(eq(userId), eq(commentId), any(CommentUpdateRequest.class)))
        .willReturn(response);

    // when // then
    mockMvc
        .perform(
            patch("/api/comments/{commentId}", commentId)
                .header(USER_ID_HEADER, userId.toString())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(commentId.toString()))
        .andExpect(jsonPath("$.reviewId").value(reviewId.toString()))
        .andExpect(jsonPath("$.userId").value(userId.toString()))
        .andExpect(jsonPath("$.userNickname").value("jongin"))
        .andExpect(jsonPath("$.content").value("수정된 댓글"));

    verify(commentService)
        .updateComment(eq(userId), eq(commentId), any(CommentUpdateRequest.class));
  }

  @Test
  @DisplayName("댓글 수정 실패(400) - 요청자 ID 헤더 누락")
  void update_comment_fail_without_user_id_header() throws Exception {
    // given
    UUID commentId = UUID.randomUUID();
    CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글");

    // when // then
    mockMvc
        .perform(
            patch("/api/comments/{commentId}", commentId)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verify(commentService, never()).updateComment(any(), any(), any());
  }

  @Test
  @DisplayName("댓글 수정 실패(403) - 본인 댓글이 아니면")
  void update_comment_fail_when_forbidden() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    UUID commentId = UUID.randomUUID();
    CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글");

    given(commentService.updateComment(eq(userId), eq(commentId), any(CommentUpdateRequest.class)))
        .willThrow(
            new DeokhugamException(
                ErrorCode.FORBIDDEN_COMMENT_ACCESS,
                Map.of(
                    "commentId", commentId.toString(),
                    "userId", userId.toString())));

    // when // then
    mockMvc
        .perform(
            patch("/api/comments/{commentId}", commentId)
                .header(USER_ID_HEADER, userId.toString())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  // ========= Comment 삭제 테스트 =========
  @Test
  @DisplayName("댓글 논리 삭제 성공")
  void delete_comment_success() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    UUID commentId = UUID.randomUUID();

    // when , then
    mockMvc
        .perform(
            delete("/api/comments/{commentId}", commentId)
                .header(USER_ID_HEADER, userId.toString()))
        .andExpect(status().isNoContent());

    verify(commentService).deleteComment(eq(userId), eq(commentId));
  }

  @Test
  @DisplayName("댓글 논리 삭제 실패(400) - 요청자 ID 헤더 누락")
  void delete_comment_fail_without_user_id_header() throws Exception {
    // given
    UUID commentId = UUID.randomUUID();

    // when , then 헤더 누락
    mockMvc
        .perform(delete("/api/comments/{commentId}", commentId))
        .andExpect(status().isBadRequest());

    verify(commentService, never()).deleteComment(any(), any());
  }

  @Test
  @DisplayName("댓글 논리 삭제 실패(403) - 본인 댓글이 아니면")
  void delete_comment_fail_when_forbidden() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    UUID commentId = UUID.randomUUID();

    willThrow(
            new DeokhugamException(
                ErrorCode.FORBIDDEN_COMMENT_ACCESS,
                Map.of(
                    "commentId", commentId.toString(),
                    "userId", userId.toString())))
        .given(commentService)
        .deleteComment(eq(userId), eq(commentId));

    // when // then
    mockMvc
        .perform(
            delete("/api/comments/{commentId}", commentId)
                .header(USER_ID_HEADER, userId.toString()))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("댓글 논리 삭제 실패(404) - 댓글이 존재하지 않으면")
  void delete_comment_fail_when_comment_not_found() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    UUID commentId = UUID.randomUUID();

    willThrow(
            new DeokhugamException(
                ErrorCode.COMMENT_NOT_FOUND, Map.of("commentId", commentId.toString())))
        .given(commentService)
        .deleteComment(eq(userId), eq(commentId));

    // when // then
    mockMvc
        .perform(
            delete("/api/comments/{commentId}", commentId)
                .header(USER_ID_HEADER, userId.toString()))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("댓글 물리 삭제 성공")
  void hard_delete_comment_success() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    UUID commentId = UUID.randomUUID();

    // when // then
    mockMvc
        .perform(
            delete("/api/comments/{commentId}/hard", commentId)
                .header(USER_ID_HEADER, userId.toString()))
        .andExpect(status().isNoContent());

    verify(commentService).hardDeleteComment(eq(userId), eq(commentId));
  }

  @Test
  @DisplayName("댓글 물리 삭제 실패(400) - 요청자 ID 헤더 누락")
  void hard_delete_comment_fail_without_user_id_header() throws Exception {
    // given
    UUID commentId = UUID.randomUUID();

    // when // then
    mockMvc
        .perform(delete("/api/comments/{commentId}/hard", commentId))
        .andExpect(status().isBadRequest());

    verify(commentService, never()).hardDeleteComment(any(), any());
  }

  @Test
  @DisplayName("댓글 물리 삭제 실패(403) - 본인 댓글이 아니면")
  void hard_delete_comment_fail_when_forbidden() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    UUID commentId = UUID.randomUUID();

    willThrow(
            new DeokhugamException(
                ErrorCode.FORBIDDEN_COMMENT_ACCESS,
                Map.of(
                    "commentId", commentId.toString(),
                    "userId", userId.toString())))
        .given(commentService)
        .hardDeleteComment(eq(userId), eq(commentId));

    // when // then
    mockMvc
        .perform(
            delete("/api/comments/{commentId}/hard", commentId)
                .header(USER_ID_HEADER, userId.toString()))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("댓글 물리 삭제 실패(404) - 댓글이 존재하지 않거나 논리 삭제된 댓글이 아니면")
  void hard_delete_comment_fail_when_comment_not_found() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    UUID commentId = UUID.randomUUID();

    willThrow(
            new DeokhugamException(
                ErrorCode.COMMENT_NOT_FOUND, Map.of("commentId", commentId.toString())))
        .given(commentService)
        .hardDeleteComment(eq(userId), eq(commentId));

    // when // then
    mockMvc
        .perform(
            delete("/api/comments/{commentId}/hard", commentId)
                .header(USER_ID_HEADER, userId.toString()))
        .andExpect(status().isNotFound());
  }

  // ========= Comment 조회 테스트 =========}
  @Test
  @DisplayName("댓글 목록 조회 성공")
  void get_comments_sucess() throws Exception {
    // given
    UUID userId1 = UUID.randomUUID();
    UUID userId2 = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();
    UUID commentId1 = UUID.randomUUID();
    UUID commentId2 = UUID.randomUUID();

    CommentDto comment1 = createCommentDto(commentId1, reviewId, userId1, "첫 번째 댓글");
    CommentDto comment2 = createCommentDto(commentId2, reviewId, userId2, "두 번째 댓글");

    CursorPageResponse<CommentDto> response =
        new CursorPageResponse<>(
            List.of(comment1, comment2), commentId2.toString(), comment2.createdAt(), 2, 2, false);

    given(
            commentService.getComments(
                eq(reviewId), any(CursorPageRequest.class), eq(SortDirection.DESC)))
        .willReturn(response);

    // when , then
    mockMvc
        .perform(
            get("/api/comments")
                .param("reviewId", reviewId.toString())
                .param("direction", "DESC")
                .param("limit", "50"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(commentId1.toString()))
        .andExpect(jsonPath("$.content[0].reviewId").value(reviewId.toString()))
        .andExpect(jsonPath("$.content[0].content").value("첫 번째 댓글"))
        .andExpect(jsonPath("$.content[1].id").value(commentId2.toString()))
        .andExpect(jsonPath("$.content[1].content").value("두 번째 댓글"))
        .andExpect(jsonPath("$.size").value(2))
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.hasNext").value(false));

    verify(commentService)
        .getComments(eq(reviewId), any(CursorPageRequest.class), eq(SortDirection.DESC));
  }

  @Test
  @DisplayName("댓글 목록 조회 실패(400) - reviewId가 없으면")
  void get_comments_fail_without_review_id() throws Exception {
    // when, then
    mockMvc.perform(get("/api/comments")).andExpect(status().isBadRequest());

    verify(commentService, never()).getComments(any(), any(), any());
  }
}
