package com.team01.deokhugam.batch.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.batch.dto.PopularBookScoreRow;
import com.team01.deokhugam.batch.repository.PopularBookBatchQueryRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DashboardBatchServiceTest {

  @Mock private DashboardBatchTransactionService dashboardBatchTransactionService;
  @Mock private PopularBookBatchQueryRepository popularBookBatchQueryRepository;

  @InjectMocks private DashboardBatchService dashboardBatchService;

  @Test
  @DisplayName("인기 도서 배치 실행 시 기간별 집계 결과가 있으면 저장한다")
  void calculate_popular_book_ranking_success() {
    // given
    LocalDate baseDate = LocalDate.of(2026, 4, 27);

    List<PopularBookScoreRow> rows =
        List.of(
            new PopularBookScoreRow(UUID.randomUUID(), 3L, 4.5, 3.9),
            new PopularBookScoreRow(UUID.randomUUID(), 2L, 4.0, 3.2));

    given(
            popularBookBatchQueryRepository.findPopularBooksBetween(
                any(OffsetDateTime.class), any(OffsetDateTime.class)))
        .willReturn(rows);

    // when
    dashboardBatchService.calculatePopularBookRanking(baseDate);

    // then
    verify(popularBookBatchQueryRepository)
        .findPopularBooksBetween(
            eq(DashboardPeriod.DAILY.getStartDateTime(baseDate).atOffset(java.time.ZoneOffset.UTC)),
            eq(DashboardPeriod.DAILY.getEndDateTime(baseDate).atOffset(java.time.ZoneOffset.UTC)));
    verify(popularBookBatchQueryRepository)
        .findPopularBooksBetween(
            eq(
                DashboardPeriod.WEEKLY
                    .getStartDateTime(baseDate)
                    .atOffset(java.time.ZoneOffset.UTC)),
            eq(DashboardPeriod.WEEKLY.getEndDateTime(baseDate).atOffset(java.time.ZoneOffset.UTC)));
    verify(popularBookBatchQueryRepository)
        .findPopularBooksBetween(
            eq(
                DashboardPeriod.MONTHLY
                    .getStartDateTime(baseDate)
                    .atOffset(java.time.ZoneOffset.UTC)),
            eq(
                DashboardPeriod.MONTHLY
                    .getEndDateTime(baseDate)
                    .atOffset(java.time.ZoneOffset.UTC)));
    verify(popularBookBatchQueryRepository)
        .findPopularBooksBetween(
            eq(
                DashboardPeriod.ALL_TIME
                    .getStartDateTime(baseDate)
                    .atOffset(java.time.ZoneOffset.UTC)),
            eq(
                DashboardPeriod.ALL_TIME
                    .getEndDateTime(baseDate)
                    .atOffset(java.time.ZoneOffset.UTC)));

    verify(dashboardBatchTransactionService)
        .deleteAndSavePopularBooks(eq(DashboardPeriod.DAILY), eq(rows), any(LocalDate.class));
    verify(dashboardBatchTransactionService)
        .deleteAndSavePopularBooks(eq(DashboardPeriod.WEEKLY), eq(rows), any(LocalDate.class));
    verify(dashboardBatchTransactionService)
        .deleteAndSavePopularBooks(eq(DashboardPeriod.MONTHLY), eq(rows), any(LocalDate.class));
    verify(dashboardBatchTransactionService)
        .deleteAndSavePopularBooks(eq(DashboardPeriod.ALL_TIME), eq(rows), any(LocalDate.class));
  }

  @Test
  @DisplayName("인기 도서 배치 실행 시 집계 결과가 없으면 저장하지 않는다")
  void calculate_popular_book_ranking_skip_when_empty() {
    // given
    LocalDate baseDate = LocalDate.of(2026, 4, 27);

    given(
            popularBookBatchQueryRepository.findPopularBooksBetween(
                any(OffsetDateTime.class), any(OffsetDateTime.class)))
        .willReturn(List.of());

    // when
    dashboardBatchService.calculatePopularBookRanking(baseDate);

    // then
    verify(dashboardBatchTransactionService, never())
        .deleteAndSavePopularBooks(any(DashboardPeriod.class), any(), any(LocalDate.class));
  }
}
