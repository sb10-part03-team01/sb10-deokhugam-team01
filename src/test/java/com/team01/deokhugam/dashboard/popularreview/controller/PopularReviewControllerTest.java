package com.team01.deokhugam.dashboard.popularreview.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.dashboard.popularreview.dto.CursorPageResponsePopularReviewDto;
import com.team01.deokhugam.dashboard.popularreview.dto.PopularReviewDto;
import com.team01.deokhugam.dashboard.popularreview.service.PopularReviewService;
import com.team01.deokhugam.global.enums.SortDirection;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = PopularReviewController.class,
    properties = {
        "spring.profiles.active=test",
        "SPRING_PROFILES_ACTIVE=test"
    }
)
@AutoConfigureMockMvc(addFilters = false)
class PopularReviewControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private PopularReviewService popularReviewService;

  @Test
  @DisplayName("인기 리뷰 목록 조회 성공 - 기본 파라미터로 조회한다")
  void get_popular_reviews_success_with_default_params() throws Exception {
    OffsetDateTime createdAt = time(10);

    PopularReviewDto dto = createDto(1, createdAt);

    CursorPageResponsePopularReviewDto response = new CursorPageResponsePopularReviewDto(
        List.of(dto),
        null,
        null,
        1,
        1L,
        false
    );

    given(popularReviewService.getPopularReviews(
        eq(DashboardPeriod.DAILY),
        eq(SortDirection.ASC),
        eq(null),
        eq(null),
        eq(50)
    )).willReturn(response);

    mockMvc
        .perform(get("/api/reviews/popular"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].rank").value(1))
        .andExpect(jsonPath("$.content[0].bookTitle").value("책1"))
        .andExpect(jsonPath("$.content[0].userNickname").value("유저1"))
        .andExpect(jsonPath("$.content[0].reviewContent").value("리뷰 내용1"))
        .andExpect(jsonPath("$.content[0].score").value(10.0))
        .andExpect(jsonPath("$.size").value(1))
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.hasNext").value(false))
        .andExpect(jsonPath("$.nextCursor").doesNotExist())
        .andExpect(jsonPath("$.nextAfter").doesNotExist());
  }

  @Test
  @DisplayName("인기 리뷰 목록 조회 성공 - 커서 파라미터로 다음 페이지를 조회한다")
  void get_popular_reviews_success_with_cursor_params() throws Exception {
    OffsetDateTime after = time(10);
    OffsetDateTime nextAfter = time(11);

    PopularReviewDto dto = createDto(2, nextAfter);

    CursorPageResponsePopularReviewDto response = new CursorPageResponsePopularReviewDto(
        List.of(dto),
        "2",
        nextAfter,
        1,
        3L,
        true
    );

    given(popularReviewService.getPopularReviews(
        eq(DashboardPeriod.WEEKLY),
        eq(SortDirection.DESC),
        eq("1"),
        eq(after),
        eq(1)
    )).willReturn(response);

    mockMvc.perform(get("/api/reviews/popular")
            .param("period", "WEEKLY")
            .param("direction", "DESC")
            .param("cursor", "1")
            .param("after", after.toString())
            .param("limit", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].rank").value(2))
        .andExpect(jsonPath("$.nextCursor").value("2"))
        .andExpect(jsonPath("$.nextAfter").value("2026-04-21T11:00:00Z"))
        .andExpect(jsonPath("$.size").value(1))
        .andExpect(jsonPath("$.totalElements").value(3))
        .andExpect(jsonPath("$.hasNext").value(true));
  }

  @Test
  @DisplayName("인기 리뷰 목록 조회 실패 - period 값이 잘못되면 400을 반환한다")
  void get_popular_reviews_fail_when_period_is_invalid() throws Exception {
    mockMvc.perform(get("/api/reviews/popular")
            .param("period", "INVALID"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("인기 리뷰 목록 조회 실패 - direction 값이 잘못되면 400을 반환한다")
  void get_popular_reviews_fail_when_direction_is_invalid() throws Exception {
    mockMvc.perform(get("/api/reviews/popular")
            .param("direction", "INVALID"))
        .andExpect(status().isBadRequest());
  }

  private PopularReviewDto createDto(int rank, OffsetDateTime createdAt) {
    return new PopularReviewDto(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        "책" + rank,
        "thumbnail-" + rank + ".jpg",
        UUID.randomUUID(),
        "유저" + rank,
        "리뷰 내용" + rank,
        4.5,
        DashboardPeriod.DAILY,
        createdAt,
        rank,
        10.0,
        10,
        0
    );
  }

  private OffsetDateTime time(int hour) {
    return OffsetDateTime.of(2026, 4, 21, hour, 0, 0, 0, ZoneOffset.UTC);
  }
}
