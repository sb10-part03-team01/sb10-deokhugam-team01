package com.team01.deokhugam.dashboard.popularbook.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team01.deokhugam.dashboard.popularbook.dto.PopularBookDto;
import com.team01.deokhugam.dashboard.popularbook.service.PopularBookService;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PopularBookController.class)
class PopularBookControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private PopularBookService popularBookService;

  @Test
  @DisplayName("인기 도서 조회 성공 - 요청 파라미터가 없으면 기본값으로 조회한다")
  void get_popular_books_with_default_params() throws Exception {
    // given
    CursorPageResponse<PopularBookDto> response =
        new CursorPageResponse<>(List.of(), null, null, 0, 0L, false);

    given(
            popularBookService.findPopularBooks(
                RankingPeriod.DAILY, SortDirection.ASC, null, null, null))
        .willReturn(response);

    // when & then
    mockMvc
        .perform(get("/api/books/popular"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.size").value(0))
        .andExpect(jsonPath("$.totalElements").value(0))
        .andExpect(jsonPath("$.hasNext").value(false));

    verify(popularBookService)
        .findPopularBooks(RankingPeriod.DAILY, SortDirection.ASC, null, null, null);
  }

  @Test
  @DisplayName("인기 도서 조회 성공 - period, direction, cursor, after, limit를 전달하면 그대로 서비스에 위임한다")
  void get_popular_books_with_all_params() throws Exception {
    // given
    OffsetDateTime after = OffsetDateTime.parse("2026-04-24T10:00:00Z");

    CursorPageResponse<PopularBookDto> response =
        new CursorPageResponse<>(List.of(), "3", after, 0, 3L, true);

    given(
            popularBookService.findPopularBooks(
                RankingPeriod.WEEKLY, SortDirection.DESC, "2", after, 10))
        .willReturn(response);

    // when & then
    mockMvc
        .perform(
            get("/api/books/popular")
                .param("period", "WEEKLY")
                .param("direction", "DESC")
                .param("cursor", "2")
                .param("after", "2026-04-24T10:00:00Z")
                .param("limit", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nextCursor").value("3"))
        .andExpect(jsonPath("$.totalElements").value(3))
        .andExpect(jsonPath("$.hasNext").value(true));

    verify(popularBookService)
        .findPopularBooks(RankingPeriod.WEEKLY, SortDirection.DESC, "2", after, 10);
  }
}
