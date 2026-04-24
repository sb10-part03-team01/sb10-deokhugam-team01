package com.team01.deokhugam.dashboard.popularbook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.team01.deokhugam.book.entity.Book;
import com.team01.deokhugam.dashboard.popularbook.dto.PopularBookDto;
import com.team01.deokhugam.dashboard.popularbook.entity.PopularBook;
import com.team01.deokhugam.dashboard.popularbook.repository.PopularBookRepository;
import com.team01.deokhugam.global.enums.RankingPeriod;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class PopularBookServiceTest {
  @Mock private PopularBookRepository popularBookRepository;

  @InjectMocks private PopularBookService popularBookService;

  @Test
  @DisplayName("인기 도서 조회 성공 - nextCursor / hasNext 값 검사")
  void find_popular_books_success() {
    // given
    OffsetDateTime time1 = time(2026, 4, 21, 10);
    OffsetDateTime time2 = time(2026, 4, 21, 11);
    OffsetDateTime time3 = time(2026, 4, 21, 12);

    PopularBook pb1 = createPopularBook(1, time1);
    PopularBook pb2 = createPopularBook(2, time2);
    PopularBook pb3 = createPopularBook(3, time3);

    // limit 2면 3개 반환
    given(
            popularBookRepository.findAllByCursor(
                RankingPeriod.DAILY, SortDirection.ASC, null, null, 2))
        .willReturn(List.of(pb1, pb2, pb3));

    given(popularBookRepository.countByPeriod(RankingPeriod.DAILY)).willReturn(3L);

    // when
    CursorPageResponse<PopularBookDto> result =
        popularBookService.findPopularBooks(RankingPeriod.DAILY, SortDirection.ASC, null, null, 2);

    // then
    assertThat(result.content()).hasSize(2);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.nextCursor()).isEqualTo("2");
    assertThat(result.nextAfter()).isEqualTo(time2);
    assertThat(result.totalElements()).isEqualTo(3);
  }

  @Test
  @DisplayName("인기 도서 조회 성공 - 다음 페이지가 없으면 nextCursor / nextAfter 는 null 이다")
  void find_popular_books_success_without_next_page() {
    // given
    OffsetDateTime time1 = time(2026, 4, 21, 10);
    OffsetDateTime time2 = time(2026, 4, 21, 11);

    PopularBook pb1 = createPopularBook(1, time1);
    PopularBook pb2 = createPopularBook(2, time2);

    given(
            popularBookRepository.findAllByCursor(
                RankingPeriod.DAILY, SortDirection.ASC, null, null, 2))
        .willReturn(List.of(pb1, pb2));

    given(popularBookRepository.countByPeriod(RankingPeriod.DAILY)).willReturn(2L);

    // when
    CursorPageResponse<PopularBookDto> result =
        popularBookService.findPopularBooks(RankingPeriod.DAILY, SortDirection.ASC, null, null, 2);

    // then
    assertThat(result.content()).hasSize(2);
    assertThat(result.hasNext()).isFalse();
    assertThat(result.nextCursor()).isNull();
    assertThat(result.nextAfter()).isNull();
    assertThat(result.totalElements()).isEqualTo(2);
  }

  private PopularBook createPopularBook(int rank, OffsetDateTime createdAt) {
    Book book =
        Book.builder()
            .title("책" + rank)
            .author("저자" + rank)
            .description("설명")
            .publisher("출판사")
            .publishedDate(LocalDate.of(2026, 4, 1))
            .isbn("isbn-" + rank)
            .build();

    ReflectionTestUtils.setField(book, "id", UUID.randomUUID());

    PopularBook pb =
        new PopularBook(book, RankingPeriod.DAILY, LocalDate.of(2026, 4, 21), rank, 0.0, 0.0, 0);

    ReflectionTestUtils.setField(pb, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(pb, "createdAt", createdAt);

    return pb;
  }

  private OffsetDateTime time(int y, int m, int d, int h) {
    return OffsetDateTime.of(y, m, d, h, 0, 0, 0, ZoneOffset.UTC);
  }
}
