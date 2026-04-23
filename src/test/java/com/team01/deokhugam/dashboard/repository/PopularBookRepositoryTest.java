package com.team01.deokhugam.dashboard.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.team01.deokhugam.book.entity.Book;
import com.team01.deokhugam.config.QuerydslTestConfig;
import com.team01.deokhugam.dashboard.entity.PopularBook;
import com.team01.deokhugam.global.enums.RankingPeriod;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

@Import(QuerydslTestConfig.class)
@DataJpaTest
@TestPropertySource(
    properties = {"spring.sql.init.mode=never", "spring.jpa.hibernate.ddl-auto=create-drop"})
class PopularBookRepositoryTest {

  @Autowired private PopularBookRepository popularBookRepository;

  @Autowired private EntityManager em;

  private OffsetDateTime time1;
  private OffsetDateTime time2;
  private OffsetDateTime time3;
  private LocalDate latestDate;
  private LocalDate oldDate;

  @BeforeEach
  void setUp() {
    time1 = time(2026, 4, 21, 10, 0);
    time2 = time(2026, 4, 21, 12, 0);
    time3 = time(2026, 4, 21, 14, 0);

    oldDate = LocalDate.of(2026, 4, 20);
    latestDate = LocalDate.of(2026, 4, 21);
  }

  @Test
  @DisplayName("인기 도서 목록 첫 페이지 조회 성공 - 최신 calculatedDate 데이터만 createdAt DESC 기준으로 조회된다.")
  void find_all_by_cursor_first_page_desc_success() {
    Book oldBook = persistBook("옛날 랭킹 책", "저자A", "11");
    Book book1 = persistBook("최신 랭킹 책1", "저자B", "22");
    Book book2 = persistBook("최신 랭킹 책2", "저자C", "33");
    Book book3 = persistBook("최신 랭킹 책3", "저자D", "44");

    persistPopularBook(oldBook, RankingPeriod.DAILY, oldDate, 1, 9.9, 4.9, 100, time3);
    persistPopularBook(book1, RankingPeriod.DAILY, latestDate, 1, 9.5, 4.8, 90, time1);
    persistPopularBook(book2, RankingPeriod.DAILY, latestDate, 2, 9.0, 4.7, 80, time3);
    persistPopularBook(book3, RankingPeriod.DAILY, latestDate, 3, 8.5, 4.6, 70, time2);

    em.flush();
    em.clear();

    List<PopularBook> result =
        popularBookRepository.findAllByCursor(
            RankingPeriod.DAILY, SortDirection.DESC, null, null, 2);

    assertThat(result).hasSize(3);
    assertThat(result)
        .extracting(pb -> pb.getBook().getTitle())
        .containsExactly("최신 랭킹 책2", "최신 랭킹 책3", "최신 랭킹 책1");
    assertThat(result).allMatch(pb -> pb.getCalculatedDate().equals(latestDate));
  }

  @Test
  @DisplayName("인기 도서 목록 다음 페이지 조회 성공 - cursor와 after를 기준으로 정확히 다음 데이터부터 조회한다.")
  void find_all_by_cursor_next_page_desc_success() {
    // given
    Book book1 = persistBook("최신 랭킹 책1", "저자A", "1");
    Book book2 = persistBook("최신 랭킹 책2", "저자B", "2");
    Book book3 = persistBook("최신 랭킹 책3", "저자C", "3");

    PopularBook pb1 =
        persistPopularBook(book1, RankingPeriod.DAILY, latestDate, 1, 9.5, 4.8, 90, time1);
    persistPopularBook(book2, RankingPeriod.DAILY, latestDate, 2, 9.0, 4.7, 80, time3);
    PopularBook pb3 =
        persistPopularBook(book3, RankingPeriod.DAILY, latestDate, 3, 8.5, 4.6, 70, time2);

    em.flush();
    em.clear();

    List<PopularBook> result =
        popularBookRepository.findAllByCursor(
            RankingPeriod.DAILY, SortDirection.DESC, pb3.getId().toString(), pb3.getCreatedAt(), 2);

    // when then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(pb1.getId());
    assertThat(result.get(0).getBook().getTitle()).isEqualTo("최신 랭킹 책1");
  }

  @Test
  @DisplayName("인기 도서 목록 ASC 조회 성공 - 오래된 createdAt부터 순서대로 조회된다.")
  void find_all_by_cursor_first_page_asc_success() {
    Book book1 = persistBook("랭킹 책1", "저자A", "1");
    Book book2 = persistBook("랭킹 책2", "저자B", "2");
    Book book3 = persistBook("랭킹 책3", "저자C", "3");

    persistPopularBook(book1, RankingPeriod.DAILY, latestDate, 1, 9.5, 4.8, 90, time1);
    persistPopularBook(book2, RankingPeriod.DAILY, latestDate, 2, 9.0, 4.7, 80, time3);
    persistPopularBook(book3, RankingPeriod.DAILY, latestDate, 3, 8.5, 4.6, 70, time2);

    em.flush();
    em.clear();

    List<PopularBook> result =
        popularBookRepository.findAllByCursor(
            RankingPeriod.DAILY, SortDirection.ASC, null, null, 2);

    assertThat(result).hasSize(3);
    assertThat(result)
        .extracting(pb -> pb.getBook().getTitle())
        .containsExactly("랭킹 책1", "랭킹 책3", "랭킹 책2");
  }

  @Test
  @DisplayName("인기 도서 목록 다음 페이지 조회 성공 - createdAt이 같으면 id를 보조 커서로 사용한다.")
  void find_all_by_cursor_next_page_uses_id_as_tie_breaker() {

    // given
    Book book1 = persistBook("동일시각 책1", "저자A", "11");
    Book book2 = persistBook("동일시각 책2", "저자B", "22");
    Book book3 = persistBook("이전시각 책", "저자C", "33");

    PopularBook sameTimeBook1 =
        persistPopularBook(book1, RankingPeriod.DAILY, latestDate, 1, 9.5, 4.8, 90, time3);
    PopularBook sameTimeBook2 =
        persistPopularBook(book2, RankingPeriod.DAILY, latestDate, 2, 9.0, 4.7, 80, time3);
    PopularBook olderBook =
        persistPopularBook(book3, RankingPeriod.DAILY, latestDate, 3, 8.5, 4.6, 70, time1);

    em.flush();

    List<PopularBook> expectedOrderAtSameTime =
        sameTimeBook1.getId().compareTo(sameTimeBook2.getId()) > 0
            ? List.of(sameTimeBook1, sameTimeBook2)
            : List.of(sameTimeBook2, sameTimeBook1);

    PopularBook firstBook = expectedOrderAtSameTime.get(0);
    PopularBook secondBook = expectedOrderAtSameTime.get(1);

    em.clear();

    // when
    List<PopularBook> firstPage =
        popularBookRepository.findAllByCursor(
            RankingPeriod.DAILY, SortDirection.DESC, null, null, 1);

    // then 첫번째 페이지 조회
    assertThat(firstPage)
        .extracting(PopularBook::getId)
        .containsExactly(firstBook.getId(), secondBook.getId());

    // when
    List<PopularBook> secondPage =
        popularBookRepository.findAllByCursor(
            RankingPeriod.DAILY,
            SortDirection.DESC,
            firstBook.getId().toString(),
            firstBook.getCreatedAt(),
            2);
    // then 두번째 페이지 조회
    assertThat(secondPage)
        .extracting(PopularBook::getId)
        .containsExactly(secondBook.getId(), olderBook.getId());
  }

  @Test
  @DisplayName("인기 도서 목록 조회 성공 - period가 다르면 해당 기간 데이터만 조회된다.")
  void find_all_by_cursor_period_filter_success() {

    // given
    Book dailyBook = persistBook("일간 인기책", "저자A", "1111111111111");
    Book weeklyBook = persistBook("주간 인기책", "저자B", "2222222222222");

    persistPopularBook(dailyBook, RankingPeriod.DAILY, latestDate, 1, 9.5, 4.8, 90, time2);
    persistPopularBook(weeklyBook, RankingPeriod.WEEKLY, latestDate, 1, 8.5, 4.3, 50, time3);

    em.flush();
    em.clear();

    // when DAILY 기준 만 조회
    List<PopularBook> result =
        popularBookRepository.findAllByCursor(
            RankingPeriod.DAILY, SortDirection.DESC, null, null, 10);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getPeriodType()).isEqualTo(RankingPeriod.DAILY);
    assertThat(result.get(0).getBook().getTitle()).isEqualTo("일간 인기책");
  }

  @Test
  @DisplayName("findAllByCursor 실패 - cursor만 있고 after가 없으면 INVALID_CURSOR_PAGINATION 예외가 발생한다.")
  void find_all_by_cursor_fail_when_cursor_exists_without_after() {
    assertThatThrownBy(
            () ->
                popularBookRepository.findAllByCursor(
                    RankingPeriod.DAILY,
                    SortDirection.DESC,
                    UUID.randomUUID().toString(),
                    null,
                    10))
        .isInstanceOf(DeokhugamException.class)
        .satisfies(
            exception -> {
              DeokhugamException e = (DeokhugamException) exception;
              assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_CURSOR_PAGINATION);
            });
  }

  @Test
  @DisplayName("findAllByCursor 실패 - cursor가 UUID 형식이 아니면 INVALID_CURSOR_FORMAT 예외가 발생한다.")
  void find_all_by_cursor_fail_when_cursor_format_is_invalid() {
    assertThatThrownBy(
            () ->
                popularBookRepository.findAllByCursor(
                    RankingPeriod.DAILY, SortDirection.DESC, "not-a-uuid", time1, 10))
        .isInstanceOf(DeokhugamException.class)
        .satisfies(
            exception -> {
              DeokhugamException e = (DeokhugamException) exception;
              assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_CURSOR_FORMAT);
            });
  }

  @Test
  @DisplayName("countByPeriod 성공 - 최신 calculatedDate 기준 해당 기간 데이터 개수만 반환한다.")
  void count_by_period_success() {

    // given
    Book oldBook = persistBook("옛날 책", "저자A", "1");
    Book latestBook1 = persistBook("최신 책1", "저자B", "2");
    Book latestBook2 = persistBook("최신 책2", "저자C", "3");
    Book weeklyBook = persistBook("주간 책", "저자D", "4");

    persistPopularBook(oldBook, RankingPeriod.DAILY, oldDate, 1, 9.9, 4.9, 100, time1);
    persistPopularBook(latestBook1, RankingPeriod.DAILY, latestDate, 1, 9.5, 4.8, 90, time2);
    persistPopularBook(latestBook2, RankingPeriod.DAILY, latestDate, 2, 9.0, 4.7, 80, time3);
    persistPopularBook(weeklyBook, RankingPeriod.WEEKLY, latestDate, 1, 8.0, 4.2, 40, time3);

    em.flush();
    em.clear();

    // when
    long count = popularBookRepository.countByPeriod(RankingPeriod.DAILY);

    // then
    assertThat(count).isEqualTo(2L);
  }

  private Book persistBook(String title, String author, String isbn) {
    Book book =
        Book.builder()
            .title(title)
            .author(author)
            .description("설명")
            .publisher("출판사")
            .publishedDate(LocalDate.of(2026, 4, 1))
            .isbn(isbn)
            .build();

    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    ReflectionTestUtils.setField(book, "createdAt", now);
    ReflectionTestUtils.setField(book, "updatedAt", now);

    em.persist(book);
    return book;
  }

  private PopularBook persistPopularBook(
      Book book,
      RankingPeriod periodType,
      LocalDate calculatedDate,
      int rank,
      double score,
      double rating,
      int reviewCount,
      OffsetDateTime createdAt) {

    PopularBook popularBook =
        new PopularBook(book, periodType, calculatedDate, rank, score, rating, reviewCount);

    ReflectionTestUtils.setField(popularBook, "createdAt", createdAt);
    em.persist(popularBook);
    return popularBook;
  }

  private OffsetDateTime time(int y, int m, int d, int h, int min) {
    return OffsetDateTime.of(y, m, d, h, min, 0, 0, ZoneOffset.UTC);
  }
}
