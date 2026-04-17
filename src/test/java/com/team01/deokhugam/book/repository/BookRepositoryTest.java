package com.team01.deokhugam.book.repository;


import static org.assertj.core.api.Assertions.assertThat;

import com.team01.deokhugam.book.Book;
import com.team01.deokhugam.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@Import({QueryDslConfig.class})
class BookRepositoryTest {

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private EntityManager em;

  private OffsetDateTime time1, time2, time3;

  @BeforeEach
  void setUp(){
    // 직접 넣은 생성 시간 때문에 jpaAuditing은 하지않음
    time1 = OffsetDateTime.parse("2026-04-17T10:00:00Z"); // 제일 옛날
    time2 = OffsetDateTime.parse("2026-04-17T12:00:00Z"); // 중간
    time3 = OffsetDateTime.parse("2026-04-17T14:00:00Z"); // 제일 최신

    // 1. 빌더를 이용해 객체 생성
    Book book1 = Book.builder()
        .title("해리포터1")
        .author("J.K. 롤링")
        .description("해리포터의 위대한 첫 번째 이야기입니다.")
        .publisher("문학수첩")
        .publishedDate(LocalDate.of(1997, 6, 26))
        .isbn("9788983920677")
        .build();

    Book book2 = Book.builder()
        .title("반지의제왕")
        .author("J.R.R. 톨킨")
        .description("절대반지를 파괴하기 위한 여정.")
        .publisher("황금가지")
        .publishedDate(LocalDate.of(1954, 7, 29))
        .isbn("9788982730000")
        .build();

    Book book3 = Book.builder()
        .title("나니아연대기")
        .author("C.S. 루이스")
        .description("옷장 너머의 마법 세계.")
        .publisher("시공주니어")
        .publishedDate(LocalDate.of(1950, 10, 16))
        .isbn("9788952744886")
        .build();

    // ReflectionTestUtils를 이용해 book 필드(rating, createdAt)에 강제로 값 주입
    // 평점이 4.5로 똑같은 책 2권 세팅
    ReflectionTestUtils.setField(book1, "rating", 4.5);
    ReflectionTestUtils.setField(book1, "createdAt", time2);
    ReflectionTestUtils.setField(book1, "updatedAt", time2);

    ReflectionTestUtils.setField(book2, "rating", 4.5);
    ReflectionTestUtils.setField(book2, "createdAt", time3); // book2가 더 최신
    ReflectionTestUtils.setField(book2, "updatedAt", time3);

    // 평점이 낮은 책 1권 세팅
    ReflectionTestUtils.setField(book3, "rating", 4.0);
    ReflectionTestUtils.setField(book3, "createdAt", time1);
    ReflectionTestUtils.setField(book3, "updatedAt", time1);

    // 영속성 컨텍스트에 저장
    em.persist(book1);
    em.persist(book2);
    em.persist(book3);

    em.flush();
    em.clear();
  }

  @Test
  @DisplayName("평점이 같으면 보조커서 기준(createdAt DESC)으로 정렬되어야 한다.")
  void testFirstPage() {
    // given
    String orderBy = "rating";
    String direction = "DESC";
    int limit = 2;

    // when
    List<Book> result = bookRepository.findBooks(
        null, orderBy, direction, null, null, limit
    );

    // then
    assertThat(result).hasSize(3); // limit(2) + 1개

    // 1등: 평점이 4.5로 같지만, time3으로 더 최신인 '반지의제왕'이 먼저 와야 함!
    assertThat(result.get(0).getTitle()).isEqualTo("반지의제왕");

    // 2등: 그 다음 최신인 '해리포터1'
    assertThat(result.get(1).getTitle()).isEqualTo("해리포터1");

    // 3등: 평점이 4.0인 '나니아연대기'
    assertThat(result.get(2).getTitle()).isEqualTo("나니아연대기");
  }

  @Test
  @DisplayName("두 번째 페이지로 커서(평점)와 보조커서(시간)를 주면 정확히 그 다음부터 가져온다.")
  void testSecondPage() {
    // given: 프론트가 1페이지의 마지막 데이터(해리포터1)를 기준으로 커서를 던짐
    String orderBy = "rating";
    String direction = "DESC";

    // 1페이지의 마지막 책이었던 '해리포터1'의 정보를 커서로 세팅
    String cursor = "4.5";
    OffsetDateTime after = time2;

    int limit = 2; // 2개 요청

    // when
    List<Book> result = bookRepository.findBooks(
        null, orderBy, direction, cursor, after, limit
    );

    // then
    // '반지의제왕'과 '해리포터1'은 이미 앞서 봤으므로 스킵되어야함
    // 남은 것은 평점 4.0인 '나니아연대기'
    assertThat(result).hasSize(1);

    // 다음 책도 없어야함
    Book nextBook = result.get(0);

    // 정확히 그 다음 순서인 '나니아연대기'를 가져왔는지 검증
    assertThat(nextBook.getTitle()).isEqualTo("나니아연대기");
    assertThat(nextBook.getRating()).isEqualTo(4.0);

    // 넣었던 데이터가 맞는지 추가 검증!
    assertThat(nextBook.getAuthor()).isEqualTo("C.S. 루이스");
    assertThat(nextBook.getPublisher()).isEqualTo("시공주니어");
  }

}