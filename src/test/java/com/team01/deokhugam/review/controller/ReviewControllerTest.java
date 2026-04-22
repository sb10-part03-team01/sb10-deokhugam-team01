package com.team01.deokhugam.review.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.exception.review.ReviewUpdateForbidden;
import com.team01.deokhugam.review.dto.CursorPageResponseReviewDto;
import com.team01.deokhugam.review.dto.ReviewCreateRequest;
import com.team01.deokhugam.review.dto.ReviewDto;
import com.team01.deokhugam.review.dto.ReviewUpdateRequest;
import com.team01.deokhugam.review.service.ReviewService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReviewController.class)
@ActiveProfiles("test")
class ReviewControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ReviewService reviewService;

  @Test
  @DisplayName("리뷰 등록 - 성공")
  void createReview_success() throws Exception {
    // given
    UUID reviewId = UUID.randomUUID();
    UUID bookId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    ReviewCreateRequest request = new ReviewCreateRequest(
        bookId,
        userId,
        "테스트 리뷰",
        4.5
    );

    ReviewDto response = new ReviewDto(
        reviewId,
        bookId,
        "테스트 책",
        "thumb.jpg",
        userId,
        "테스트 유저",
        "테스트 리뷰",
        4.5,
        0,
        0,
        false,
        OffsetDateTime.parse("2026-04-22T10:00:00+09:00"),
        null
    );

    given(reviewService.createReview(eq(request))).willReturn(response);

    // when & then
    mockMvc.perform(post("/api/reviews")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(reviewId.toString()))
        .andExpect(jsonPath("$.bookId").value(bookId.toString()))
        .andExpect(jsonPath("$.userId").value(userId.toString()))
        .andExpect(jsonPath("$.content").value("테스트 리뷰"))
        .andExpect(jsonPath("$.rating").value(4.5));
  }

  @Test
  @DisplayName("리뷰 등록 - 요청값 검증 실패")
  void createReview_fail_whenInvalidRequest() throws Exception {
    // given
    String invalidRequest = """
        {
          "bookId": null,
          "userId": null,
          "content": "",
          "rating": 6.0
        }
        """;

    // when & then
    mockMvc.perform(post("/api/reviews")
            .contentType(APPLICATION_JSON)
            .content(invalidRequest))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("리뷰 상세 조회 - 성공")
  void getReview_success() throws Exception {
    // given
    UUID reviewId = UUID.randomUUID();
    UUID bookId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID requestUserId = UUID.randomUUID();

    ReviewDto response = new ReviewDto(
        reviewId,
        bookId,
        "테스트 책",
        "thumb.jpg",
        userId,
        "테스트 유저",
        "상세 리뷰",
        5.0,
        1,
        2,
        false,
        OffsetDateTime.parse("2026-04-22T10:00:00+09:00"),
        null
    );

    given(reviewService.getReview(reviewId, requestUserId)).willReturn(response);

    // when & then
    mockMvc.perform(get("/api/reviews/{reviewId}", reviewId)
            .header("Deokhugam-Request-User-ID", requestUserId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(reviewId.toString()))
        .andExpect(jsonPath("$.content").value("상세 리뷰"))
        .andExpect(jsonPath("$.rating").value(5.0));
  }

  @Test
  @DisplayName("리뷰 목록 조회 - 성공")
  void getReviews_success() throws Exception {
    // given
    UUID requestUserId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();
    UUID bookId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    ReviewDto reviewDto = new ReviewDto(
        reviewId,
        bookId,
        "테스트 책",
        "thumb.jpg",
        userId,
        "테스트 유저",
        "목록 리뷰",
        4.0,
        3,
        1,
        false,
        OffsetDateTime.parse("2026-04-22T10:00:00+09:00"),
        null
    );

    CursorPageResponseReviewDto response = new CursorPageResponseReviewDto(
        List.of(reviewDto),
        null,
        null,
        1,
        1L,
        false
    );

    given(reviewService.searchReviews(
        eq(requestUserId),
        eq(null),
        eq(null),
        eq("테스트"),
        eq("createdAt"),
        eq(SortDirection.DESC),
        eq(null),
        eq(null),
        eq(10)
    )).willReturn(response);

    // when & then
    mockMvc.perform(get("/api/reviews")
            .header("Deokhugam-Request-User-ID", requestUserId)
            .param("keyword", "테스트")
            .param("orderBy", "createdAt")
            .param("direction", "DESC")
            .param("limit", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].id").value(reviewId.toString()))
        .andExpect(jsonPath("$.content[0].content").value("목록 리뷰"))
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.hasNext").value(false));
  }

  @Test
  @DisplayName("리뷰 목록 조회 - 헤더 누락 시 실패")
  void getReviews_fail_whenMissingHeader() throws Exception {
    // when & then
    mockMvc.perform(get("/api/reviews")
            .param("keyword", "테스트"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("리뷰 수정 - 성공")
  void updateReview_success() throws Exception {
    // given
    UUID reviewId = UUID.randomUUID();
    UUID requestUserId = UUID.randomUUID();
    UUID bookId = UUID.randomUUID();

    ReviewUpdateRequest request = new ReviewUpdateRequest("수정된 리뷰", 4.5);

    ReviewDto response = new ReviewDto(
        reviewId,
        bookId,
        "테스트 책",
        "thumb.jpg",
        requestUserId,
        "테스트 유저",
        "수정된 리뷰",
        4.5,
        0,
        0,
        false,
        OffsetDateTime.parse("2026-04-22T10:00:00+09:00"),
        OffsetDateTime.parse("2026-04-22T11:00:00+09:00")
    );

    given(reviewService.updateReview(reviewId, requestUserId, request)).willReturn(response);

    // when & then
    mockMvc.perform(patch("/api/reviews/{reviewId}", reviewId)
            .header("Deokhugam-Request-User-ID", requestUserId)
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(reviewId.toString()))
        .andExpect(jsonPath("$.content").value("수정된 리뷰"))
        .andExpect(jsonPath("$.rating").value(4.5));
  }

  @Test
  @DisplayName("리뷰 수정 - 권한 없는 사용자면 403 반환")
  void updateReview_fail_whenForbidden() throws Exception {
    // given
    UUID reviewId = UUID.randomUUID();
    UUID requestUserId = UUID.randomUUID();

    ReviewUpdateRequest request = new ReviewUpdateRequest("수정 시도", 4.5);

    given(reviewService.updateReview(reviewId, requestUserId, request))
        .willThrow(new ReviewUpdateForbidden(reviewId, requestUserId));

    // when & then
    mockMvc.perform(patch("/api/reviews/{reviewId}", reviewId)
            .header("Deokhugam-Request-User-ID", requestUserId)
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }
}
