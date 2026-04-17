package com.team01.deokhugam.book;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.team01.deokhugam.book.dto.BookCreateRequest;
import com.team01.deokhugam.book.dto.BookDto;
import com.team01.deokhugam.book.repository.BookRepository;
import com.team01.deokhugam.global.exception.book.DuplicatedIsbnException;
import java.time.LocalDate;
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
}