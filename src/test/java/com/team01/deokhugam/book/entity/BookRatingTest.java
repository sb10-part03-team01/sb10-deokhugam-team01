package com.team01.deokhugam.book.entity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.offset;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class BookRatingTest {

  private Book buildBook() {
    return Book.builder()
        .title("테스트 책")
        .author("저자")
        .description("설명")
        .publisher("출판사")
        .publishedDate(LocalDate.of(2024, 1, 1))
        .isbn("1234567890123")
        .build();
  }

  @Test
  void plusRating_건수와_평균이_올바르게_업데이트된다() {
    Book book = buildBook();
    book.plusRating(4.0);  // 첫 번째 리뷰
    book.plusRating(2.0);  // 두 번째 리뷰

    assertThat(book.getReviewCount()).isEqualTo(2);
    assertThat(book.getRating()).isEqualTo(3.0, offset(0.001)); // (4+2)/2 = 3.0
  }

  @Test
  void minusRating_리뷰가_1개일때_삭제하면_0점_0건으로_초기화된다() {
    Book book = buildBook();
    book.plusRating(5.0);
    book.minusRating(5.0);

    assertThat(book.getReviewCount()).isEqualTo(0);
    assertThat(book.getRating()).isEqualTo(0.0, offset(0.001));
  }

  @Test
  void modifyRating_건수는_유지되고_평균만_바뀐다() {
    Book book = buildBook();
    book.plusRating(4.0);
    book.plusRating(4.0);
    // 현재 평균 4.0, 건수 2
    book.modifyRating(4.0, 2.0); // 한 리뷰를 4점→2점으로 수정

    assertThat(book.getReviewCount()).isEqualTo(2); // 건수 유지
    assertThat(book.getRating()).isEqualTo(3.0, offset(0.001)); // (4+2)/2 = 3.0
  }
}
