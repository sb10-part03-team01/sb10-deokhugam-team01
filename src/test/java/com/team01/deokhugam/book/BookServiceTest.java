package com.team01.deokhugam.book;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.team01.deokhugam.book.dto.BookCreateRequest;
import com.team01.deokhugam.book.dto.BookDto;
import com.team01.deokhugam.book.repository.BookRepository;
import com.team01.deokhugam.global.exception.book.BookNotFoundException;
import com.team01.deokhugam.global.exception.book.DuplicatedIsbnException;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

  @Mock
  private BookRepository bookRepository;

  @Mock
  private BookMapper bookMapper;

  @InjectMocks
  private BookService bookService;

  private BookCreateRequest request;
  private Book book;
  private BookDto bookDto;

  @BeforeEach
  void setUp() {
    // 테스트에 사용할 공통 데이터 세팅
    request = new BookCreateRequest(
        "테스트 도서",
        "테스트 저자",
        "테스트 설명",
        "테스트 출판사",
        LocalDate.of(2026, 4, 16),
        "1234567890"
    );

    book = Book.builder()
        .title(request.getTitle())
        .author(request.getAuthor())
        .description(request.getDescription())
        .publisher(request.getPublisher())
        .publishedDate(request.getPublishedDate())
        .isbn(request.getIsbn())
        .build();

    bookDto = BookDto.builder()
        .id(UUID.randomUUID())
        .title(request.getTitle())
        .author(request.getAuthor())
        .description(request.getDescription())
        .publisher(request.getPublisher())
        .publishedDate(request.getPublishedDate())
        .isbn(request.getIsbn())
        .thumbnailUrl(null)
        .reviewCount(book.getReviewCount())
        .rating(book.getRating())
        .createdAt(book.getCreatedAt())
        .updatedAt(book.getUpdatedAt())
        .build();
  }

  @Test
  @DisplayName("썸네일 없이 도서 등록 성공 - 정상적인 요청일 때")
  void createBook_without_thumbnail_Success() {
    // given
    Book savedBook = book;
    given(bookRepository.existsByIsbn(anyString())).willReturn(false); // ISBN 중복 아님
    given(bookRepository.saveAndFlush(any(Book.class))).willReturn(savedBook); // 저장하면 book 반환
    given(bookMapper.toDto(savedBook)).willReturn(bookDto); // 매퍼 호출 시 bookDto 반환

    // when
    BookDto result = bookService.createBook(request, null);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("테스트 도서");
    assertThat(result.getAuthor()).isEqualTo("테스트 저자");
    assertThat(result.getDescription()).isEqualTo("테스트 설명");
    assertThat(result.getPublisher()).isEqualTo("테스트 출판사");
    assertThat(result.getPublishedDate()).isEqualTo(LocalDate.of(2026, 4, 16));
    assertThat(result.getIsbn()).isEqualTo("1234567890");

    // 메서드들이 한번씩 호출되었는지 검사
    verify(bookRepository).existsByIsbn("1234567890");
    verify(bookRepository).saveAndFlush(any(Book.class));
    verify(bookMapper).toDto(savedBook);
  }

  @Test
  @DisplayName("도서 등록 실패 - 이미 존재하는 ISBN일 때")
  void createBook_Fail_DuplicateIsbn() {
    // given
    given(bookRepository.existsByIsbn(anyString())).willReturn(true);

    // when & then
    assertThatThrownBy(() -> bookService.createBook(request, null))
        .isInstanceOf(DuplicatedIsbnException.class);

    // 메서드들이 호출되지 않는지 확인
    verify(bookRepository, never()).saveAndFlush(any(Book.class));
    verify(bookMapper, never()).toDto(any(Book.class));
  }

  // =========================================================================
  // 단건 조회 (findBook) 테스트
  // =========================================================================

  @Test
  @DisplayName("도서 상세 조회 성공 - 존재하는 도서일 때")
  void findBook_Success() {
    // given
    UUID bookId = UUID.randomUUID();

    Book book1 = Book.builder()
        .title("해리포터1")
        .author("J.K. 롤링")
        .description("해리포터의 위대한 첫 번째 이야기입니다.")
        .publisher("문학수첩")
        .publishedDate(LocalDate.of(1997, 6, 26))
        .isbn("9788983920677")
        .build();

    // 매퍼가 반환할 가짜 DTO 세팅
    BookDto mockDto = BookDto.builder()
        .id(bookId)
        .title(book1.getTitle())
        .author(book1.getAuthor())
        .description(book1.getDescription())
        .build();

    given(bookRepository.findByIdAndIsDeletedFalse(bookId)).willReturn(Optional.of(book1));
    given(bookMapper.toDto(book1)).willReturn(mockDto);

    // when
    BookDto result = bookService.findBook(bookId);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("해리포터1");

    verify(bookRepository).findByIdAndIsDeletedFalse(bookId);
    verify(bookMapper).toDto(book1);
  }

  @Test
  @DisplayName("도서 상세 조회 실패 - 존재하지 않거나 삭제된 도서일 때")
  void findBook_Fail_NotFound() {
    // given
    UUID bookId = UUID.randomUUID();
    given(bookRepository.findByIdAndIsDeletedFalse(bookId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> bookService.findBook(bookId))
        .isInstanceOf(BookNotFoundException.class);

    verify(bookRepository).findByIdAndIsDeletedFalse(bookId);
    verify(bookMapper, never()).toDto(any());
  }

  // =========================================================================
  // 목록 조회 (findAllBooks) 테스트
  // =========================================================================

  @Test
  @DisplayName("도서 목록 조회 성공 - 정상적인 커서 페이징 요청일 때")
  void findAllBooks_Success() {
    // given
    String keyword = "해리포터";
    String orderBy = "title";
    String direction = "ASC";
    Integer limit = 10;

    Book book1 = Book.builder()
        .title("해리포터1")
        .author("J.K. 롤링")
        .description("해리포터의 위대한 첫 번째 이야기입니다.")
        .publisher("문학수첩")
        .publishedDate(LocalDate.of(1997, 6, 26))
        .isbn("9788983920677")
        .build();

    BookDto mockDto = BookDto.builder()
        .id(UUID.randomUUID())
        .title(book1.getTitle())
        .createdAt(OffsetDateTime.now()) // 커서 추출기를 위해 시간 세팅 필요
        .build();

    List<Book> books = List.of(book1);
    long totalElements = 1L;

    // Repository 및 Mapper 모킹
    given(bookRepository.findBooks(eq(keyword), eq(orderBy), eq(direction), isNull(), isNull(), anyInt()))
        .willReturn(books);
    given(bookRepository.countBooks(keyword)).willReturn(totalElements);
    given(bookMapper.toDto(book1)).willReturn(mockDto);

    // when
    CursorPageResponse<BookDto> result = bookService.findAllBooks(
        keyword, orderBy, direction, null, null, limit
    );

    // then
    assertThat(result).isNotNull();
    assertThat(result.content()).hasSize(1);
    assertThat(result.content().get(0).getTitle()).isEqualTo("해리포터1");
    assertThat(result.totalElements()).isEqualTo(1L);

    verify(bookRepository).findBooks(eq(keyword), eq(orderBy), eq(direction), isNull(), isNull(), anyInt());
    verify(bookRepository).countBooks(keyword);
  }

  @Test
  @DisplayName("도서 목록 조회 실패 - 허용되지 않은 정렬 기준(orderBy)일 때")
  void findAllBooks_Fail_InvalidOrderBy() {
    // given
    String invalidOrderBy = "이상한정렬기준";

    // when & then
    assertThatThrownBy(() -> bookService.findAllBooks("keyword", invalidOrderBy, "ASC", null, null, 10))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("올바른 정렬기준이 아닙니다");

    // 검증 로직에서 컷 당했으므로 DB 조회가 일어나면 안 됨
    verify(bookRepository, never()).findBooks(any(), any(), any(), any(), any(), anyInt());
  }

  @Test
  @DisplayName("도서 목록 조회 실패 - 허용되지 않은 정렬 방향(direction)일 때")
  void findAllBooks_Fail_InvalidDirection() {
    // given
    String invalidDirection = "이상한방향";

    // when & then
    assertThatThrownBy(() -> bookService.findAllBooks("keyword", "title", invalidDirection, null, null, 10))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("올바른 정렬 방향이 아닙니다");

    // 마찬가지로 DB 조회 차단 확인
    verify(bookRepository, never()).findBooks(any(), any(), any(), any(), any(), anyInt());
  }
}
