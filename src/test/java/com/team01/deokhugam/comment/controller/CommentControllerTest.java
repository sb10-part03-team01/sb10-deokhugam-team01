package com.team01.deokhugam.comment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team01.deokhugam.comment.dto.CommentCreateRequest;
import com.team01.deokhugam.comment.dto.CommentDto;
import com.team01.deokhugam.comment.service.CommentService;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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

  private static final String USER_ID_HEADER = "Deokhugam-Request-User-ID";

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
}
