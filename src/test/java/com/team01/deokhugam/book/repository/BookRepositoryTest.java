package com.team01.deokhugam.book.repository;


import static org.assertj.core.api.Assertions.assertThat;

import com.team01.deokhugam.book.entity.Book;
import com.team01.deokhugam.global.config.QueryDslConfig;
import com.team01.deokhugam.global.enums.SortDirection;
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
        .isbn("9788983730000")
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
    ReflectionTestUtils.setField(book1, "reviewCount", 100);

    // ...
    ReflectionTestUtils.setField(book2, "rating", 4.5);
    ReflectionTestUtils.setField(book2, "reviewCount", 50);

    // ...
    ReflectionTestUtils.setField(book3, "rating", 4.0);
    ReflectionTestUtils.setField(book3, "reviewCount", 200);


    // 영속성 컨텍스트에 저장
    em.persist(book1);
    em.persist(book2);
    em.persist(book3);

    em.flush();

    em.createQuery("UPDATE Book b SET b.createdAt = :time, b.updatedAt = :time WHERE b.id = :id")
        .setParameter("time", time2).setParameter("id", book1.getId()).executeUpdate();

    em.createQuery("UPDATE Book b SET b.createdAt = :time, b.updatedAt = :time WHERE b.id = :id")
        .setParameter("time", time3).setParameter("id", book2.getId()).executeUpdate();

    em.createQuery("UPDATE Book b SET b.createdAt = :time, b.updatedAt = :time WHERE b.id = :id")
        .setParameter("time", time1).setParameter("id", book3.getId()).executeUpdate();
    em.clear();
  }

  @Test
  @DisplayName("평점이 같으면 보조커서 기준(createdAt DESC)으로 정렬되어야 한다.")
  void testFirstPage() {
    // given
    String orderBy = "rating";
    SortDirection direction = SortDirection.DESC;
    int limit = 2;

    // when
    List<Book> result = bookRepository.findBooks(
        null, orderBy, direction, null, null, limit
    );

    // then
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
    SortDirection direction = SortDirection.DESC;

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


  // =========================================================================
  // 1. 키워드 검색 & countBooks 분기 (if문 커버리지)
  // =========================================================================

  @Test
  @DisplayName("키워드 검색 분기 - 공백일 때와 값이 있을 때 모든 조건을 커버한다.")
  void testKeywordSearch_And_CountBooks() {
    // 1. 키워드가 빈 문자열일 때 (if(!StringUtils.hasText(keyword)) 분기 통과)
    List<Book> emptyKeyword = bookRepository.findBooks("", "title", SortDirection.ASC, null, null, 10);
    long emptyCount = bookRepository.countBooks("");
    assertThat(emptyKeyword).hasSize(3);
    assertThat(emptyCount).isEqualTo(3L);

    // 2. 제목 검색 커버리지
    List<Book> byTitle = bookRepository.findBooks("반지", "title", SortDirection.ASC, null, null, 10);
    assertThat(byTitle).hasSize(1);
    assertThat(byTitle.get(0).getTitle()).isEqualTo("반지의제왕");

    // 3. 작가 검색 커버리지
    List<Book> byAuthor = bookRepository.findBooks("루이스", "title", SortDirection.ASC, null, null, 10);
    assertThat(byAuthor.get(0).getAuthor()).isEqualTo("C.S. 루이스");

    // 4. ISBN 검색 커버리지
    List<Book> byIsbn = bookRepository.findBooks("9788983", "title", SortDirection.ASC, null, null, 10);
    assertThat(byIsbn).hasSize(2); // 해리포터, 반지의제왕 둘 다 9788983으로 시작
  }

  // =========================================================================
  // 2. dynamicOrder 분기 (if문 & switch문 커버리지)
  // =========================================================================

  @Test
  @DisplayName("정렬 기준 분기 - 값이 없으면 기본값(createdAt)으로, switch문의 모든 case를 통과한다.")
  void testDynamicOrder_Switch_Coverage() {
    // 1. 정렬 기준이 없을 때 (if(!StringUtils.hasText(orderBy)) 분기) -> createdAt 기준 DESC로 작동
    List<Book> emptyOrder = bookRepository.findBooks(null, "", SortDirection.DESC, null, null, 10);
    assertThat(emptyOrder.get(0).getTitle()).isEqualTo("반지의제왕"); // time3 (가장 최신)

    // 2. title 기준 (ASC)
    List<Book> titleAsc = bookRepository.findBooks(null, "title", SortDirection.ASC, null, null, 10);
    // 가나다순: 나니아연대기 -> 반지의제왕 -> 해리포터1
    assertThat(titleAsc.get(0).getTitle()).isEqualTo("나니아연대기");

    // 3. reviewCount 기준 (ASC)
    List<Book> reviewAsc = bookRepository.findBooks(null, "reviewCount", SortDirection.ASC, null, null, 10);
    // 50(반지) -> 100(해리) -> 200(나니아)
    assertThat(reviewAsc.get(0).getTitle()).isEqualTo("반지의제왕");
    assertThat(reviewAsc.get(2).getTitle()).isEqualTo("나니아연대기");

    // 4. publishedDate 기준 (ASC)
    List<Book> dateAsc = bookRepository.findBooks(null, "publishedDate", SortDirection.ASC, null, null, 10);
    // 1950(나니아) -> 1954(반지) -> 1997(해리)
    assertThat(dateAsc.get(0).getTitle()).isEqualTo("나니아연대기");

    // 5. default 기준 (이상한 문자열) -> title DESC로 fallback 작동
    List<Book> strangeOrder = bookRepository.findBooks(null, "strange_order", SortDirection.DESC, null, null, 10);
    assertThat(strangeOrder.get(0).getTitle()).isEqualTo("해리포터1"); // '해'가 가나다 역순 1등
  }

  // =========================================================================
  // 3. cursorCondition 분기 (switch문 & isAsc 커버리지)
  // =========================================================================

  @Test
  @DisplayName("커서 조건 분기 - 각 정렬 기준별 커서 로직과 오름차순/내림차순(isAsc)을 모두 통과한다.")
  void testCursorCondition_Switch_And_AscDesc_Coverage() {
    // 1. title + ASC 커서 (나니아 -> 반지 -> 해리)
    // '나니아연대기'를 커서로 던지면 그 다음인 '반지의제왕'이 나와야 함
    List<Book> titleCursorAsc = bookRepository.findBooks(null, "title", SortDirection.ASC, "나니아연대기", time1, 10);
    assertThat(titleCursorAsc.get(0).getTitle()).isEqualTo("반지의제왕");

    // 2. publishedDate + DESC 커서 (해리 -> 반지 -> 나니아)
    // '1997-06-26'(해리포터)를 커서로 던지면 그 과거인 '반지의제왕'이 나와야 함
    List<Book> dateCursorDesc = bookRepository.findBooks(null, "publishedDate", SortDirection.DESC, "1997-06-26", time2, 10);
    assertThat(dateCursorDesc.get(0).getTitle()).isEqualTo("반지의제왕");

    // 3. reviewCount + ASC 커서 (반지[50] -> 해리[100] -> 나니아[200])
    // '50'(반지)를 커서로 던지면 그 다음인 '해리포터1'이 나와야 함
    List<Book> reviewCursorAsc = bookRepository.findBooks(null, "reviewCount", SortDirection.ASC, "50", time3, 10);
    assertThat(reviewCursorAsc.get(0).getTitle()).isEqualTo("해리포터1");

    // 4. default + ASC 커서 (이상한 정렬 기준 -> title ASC로 빠짐)
    List<Book> strangeCursorAsc = bookRepository.findBooks(null, "strange_order", SortDirection.ASC, "나니아연대기", time1, 10);
    assertThat(strangeCursorAsc.get(0).getTitle()).isEqualTo("반지의제왕");
  }
}
