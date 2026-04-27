package com.team01.deokhugam.batch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.batch.dto.PopularBookScoreRow;
import com.team01.deokhugam.book.entity.Book;
import com.team01.deokhugam.book.repository.BookRepository;
import com.team01.deokhugam.dashboard.popularbook.entity.PopularBook;
import com.team01.deokhugam.dashboard.popularbook.repository.PopularBookRepository;
import com.team01.deokhugam.dashboard.poweruser.repository.PowerUserRepository;
import com.team01.deokhugam.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DashboardBatchTransactionServiceTest {

  @Mock private PowerUserRepository powerUserRepository;
  @Mock private UserRepository userRepository;
  @Mock private PopularBookRepository popularBookRepository;
  @Mock private BookRepository bookRepository;

  @InjectMocks private DashboardBatchTransactionService dashboardBatchTransactionService;

  @Test
  @DisplayName("인기 도서 집계 결과를 rank 순서대로 PopularBook에 저장한다")
  void delete_and_save_popular_books_success() {
    // given
    UUID bookId1 = UUID.randomUUID();
    UUID bookId2 = UUID.randomUUID();

    Book book1 =
        new Book("책1", "저자1", "설명1", "출판사1", LocalDate.of(2026, 4, 1), "1111111111111", "t1");
    Book book2 =
        new Book("책2", "저자2", "설명2", "출판사2", LocalDate.of(2026, 4, 1), "2222222222222", "t2");

    ReflectionTestUtils.setField(book1, "id", bookId1);
    ReflectionTestUtils.setField(book2, "id", bookId2);

    List<PopularBookScoreRow> rows =
        List.of(
            new PopularBookScoreRow(bookId1, 5L, 4.8, 4.88),
            new PopularBookScoreRow(bookId2, 3L, 4.2, 3.72));

    LocalDate calculatedDate = LocalDate.of(2026, 4, 26);

    given(bookRepository.findAllById(List.of(bookId1, bookId2))).willReturn(List.of(book1, book2));

    // when
    dashboardBatchTransactionService.deleteAndSavePopularBooks(
        DashboardPeriod.DAILY, rows, calculatedDate);

    // then
    verify(popularBookRepository)
        .deleteByPeriodTypeAndCalculatedDate(DashboardPeriod.DAILY, calculatedDate);

    ArgumentCaptor<List<PopularBook>> captor = ArgumentCaptor.forClass(List.class);
    verify(popularBookRepository).saveAll(captor.capture());

    List<PopularBook> saved = captor.getValue();

    assertThat(saved).hasSize(2);

    PopularBook first = saved.get(0);
    PopularBook second = saved.get(1);

    assertThat(first.getBook().getId()).isEqualTo(bookId1);
    assertThat(first.getPeriodType()).isEqualTo(DashboardPeriod.DAILY);
    assertThat(first.getCalculatedDate()).isEqualTo(calculatedDate);
    assertThat(first.getRank()).isEqualTo(1);
    assertThat(first.getScore()).isEqualTo(4.88);
    assertThat(first.getRating()).isEqualTo(4.8);
    assertThat(first.getReviewCount()).isEqualTo(5);

    assertThat(second.getBook().getId()).isEqualTo(bookId2);
    assertThat(second.getRank()).isEqualTo(2);
    assertThat(second.getScore()).isEqualTo(3.72);
    assertThat(second.getRating()).isEqualTo(4.2);
    assertThat(second.getReviewCount()).isEqualTo(3);
  }
}
