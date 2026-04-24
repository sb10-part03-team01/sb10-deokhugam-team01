package com.team01.deokhugam.book.service;


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

import com.team01.deokhugam.book.BookMapper;
import com.team01.deokhugam.book.dto.BookCreateRequest;
import com.team01.deokhugam.book.dto.BookDto;
import com.team01.deokhugam.book.dto.OcrSpaceResponse;
import com.team01.deokhugam.book.dto.naver.NaverBookDto;
import com.team01.deokhugam.book.dto.naver.NaverBookResponse;
import com.team01.deokhugam.book.dto.naver.NaverBookResponse.NaverBookItem;
import com.team01.deokhugam.book.entity.Book;
import com.team01.deokhugam.book.repository.BookRepository;
import com.team01.deokhugam.book.storage.S3ThumbnailStorage;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.book.dto.BookUpdateRequest;
import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

  @Mock
  private BookRepository bookRepository;

  @Mock
  private BookMapper bookMapper;

  @Mock
  private S3ThumbnailStorage s3ThumbnailStorage;

  @InjectMocks
  private BookService bookService;

  @Mock(answer = org.mockito.Answers.RETURNS_DEEP_STUBS)
  private RestClient naverRestClient;

  @Mock(answer = org.mockito.Answers.RETURNS_DEEP_STUBS)
  private RestClient defaultRestClient;

  @Mock(answer = org.mockito.Answers.RETURNS_DEEP_STUBS)
  private RestClient ocrRestClient;

  private BookCreateRequest request;
  private Book book;
  private BookDto bookDto;

  @BeforeEach
  void setUp() {
    bookService = new BookService(bookMapper, bookRepository, naverRestClient, defaultRestClient, ocrRestClient, s3ThumbnailStorage);
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
  // =========================================================================
  // 등록 (createBook) 테스트
  // =========================================================================

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
  @DisplayName("썸네일 포함 도서 등록 성공 - S3 URL이 반환되고 엔티티에 저장된다.")
  void createBook_with_thumbnail_Success() throws IOException {
    // given
    MultipartFile mockFile = new MockMultipartFile("thumbnail", "test.jpg", "image/jpeg", "test data".getBytes());
    String expectedS3Url = "https://s3.aws.com/test.jpg";

    given(bookRepository.existsByIsbn(anyString())).willReturn(false);
    given(s3ThumbnailStorage.upload(mockFile)).willReturn(expectedS3Url);
    given(bookRepository.saveAndFlush(any(Book.class))).willReturn(book);
    given(bookMapper.toDto(book)).willReturn(bookDto);

    //가짜 트랜잭션 동기화 환경 활성화
    TransactionSynchronizationManager.initSynchronization();

    try {
      // when
      bookService.createBook(request, mockFile);

      // then
      verify(s3ThumbnailStorage).upload(mockFile);
      verify(bookRepository).saveAndFlush(any(Book.class));
    } finally {
      // 테스트가 끝나면 다른 테스트에 영향 안 주게 청소
      TransactionSynchronizationManager.clearSynchronization();
    }
  }

  @Test
  @DisplayName("도서 등록 실패 - DB 저장 중 예외 발생 시 트랜잭션 롤백 콜백이 실행되어 S3 이미지가 삭제된다.")
  void createBook_Fail_Rollback_Deletes_Thumbnail() throws IOException {
    // given
    MultipartFile mockFile = new MockMultipartFile("thumbnail", "test.jpg", "image/jpeg", "test data".getBytes());
    String expectedS3Url = "https://s3.aws.com/test.jpg";

    given(bookRepository.existsByIsbn(anyString())).willReturn(false);
    given(s3ThumbnailStorage.upload(mockFile)).willReturn(expectedS3Url);

    // S3 업로드는 성공했지만, DB 저장 시점에 강제로 예외 터뜨리기
    given(bookRepository.saveAndFlush(any(Book.class)))
        .willThrow(new DataIntegrityViolationException("DB 저장 실패 (고의 에러)"));

    // 가짜 트랜잭션 환경 열기
    TransactionSynchronizationManager.initSynchronization();

    try {
      // when 예외가 발생하는지 먼저 확인 (동시성 에러로 변환되는 로직 검증)
      assertThatThrownBy(() -> bookService.createBook(request, mockFile))
          .isInstanceOf(DeokhugamException.class)
          .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATED_ISBN);

      // then
      // 서비스 로직에서 익명 클래스로 등록해둔 TransactionSynchronization을 꺼내옴
      List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
      assertThat(synchronizations).isNotEmpty();

      // 우리가 등록한 콜백 함수(afterCompletion)에 강제로 롤백 상태(STATUS_ROLLED_BACK'를 쏴줌
      for (TransactionSynchronization sync : synchronizations) {
        sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
      }

      // 결과 검증: S3 저장소의 delete() 메서드가 해당 URL로 정확히 호출되었는가?
      verify(s3ThumbnailStorage).delete(expectedS3Url);

    } finally {
      // 청소
      TransactionSynchronizationManager.clearSynchronization();
    }
  }

  @Test
  @DisplayName("도서 등록 실패 - 이미 존재하는 ISBN일 때")
  void createBook_Fail_DuplicateIsbn() {
    // given
    given(bookRepository.existsByIsbn(anyString())).willReturn(true);

    // when & then
    assertThatThrownBy(() -> bookService.createBook(request, null))
        .isInstanceOf(DeokhugamException.class);

    // 메서드들이 호출되지 않는지 확인
    verify(bookRepository, never()).saveAndFlush(any(Book.class));
    verify(bookMapper, never()).toDto(any(Book.class));
  }

  @Test
  @DisplayName("도서 등록 실패 - 동시성 문제(TOCTOU)로 DB 제약조건 위반 시 커스텀 예외 반환")
  void createBook_Fail_DataIntegrityViolation() {
    // given
    given(bookRepository.existsByIsbn(anyString())).willReturn(false);
    // saveAndFlush 시점에 DB에서 강제로 중복 예외 발생
    given(bookRepository.saveAndFlush(any(Book.class)))
        .willThrow(new org.springframework.dao.DataIntegrityViolationException("Unique constraint violation"));

    // when & then
    assertThatThrownBy(() -> bookService.createBook(request, null))
        .isInstanceOf(DeokhugamException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATED_ISBN);
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
        .isInstanceOf(DeokhugamException.class);

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
    SortDirection direction = SortDirection.ASC;
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
  @DisplayName("도서 목록 조회 성공 - 다양한 정렬 기준(switch문) 커버리지 테스트")
  void findAllBooks_Success_SwitchCoverage() {
    // given
    long totalElements = 1L;
    given(bookRepository.findBooks(any(), any(), any(), any(), any(), anyInt())).willReturn(List.of(book));
    given(bookRepository.countBooks(any())).willReturn(totalElements);
    given(bookMapper.toDto(any())).willReturn(bookDto);

    // when & then - 1. rating
    CursorPageResponse<BookDto> ratingResult = bookService.findAllBooks(null, "rating", SortDirection.DESC, null, null, 10);
    assertThat(ratingResult).isNotNull();

    // when & then - 2. reviewCount
    CursorPageResponse<BookDto> reviewResult = bookService.findAllBooks(null, "reviewCount", SortDirection.DESC, null, null, 10);
    assertThat(reviewResult).isNotNull();

    // when & then - 3. publishedDate
    CursorPageResponse<BookDto> dateResult = bookService.findAllBooks(null, "publishedDate", SortDirection.DESC, null, null, 10);
    assertThat(dateResult).isNotNull();
  }

  @Test
  @DisplayName("도서 목록 조회 실패 - 허용되지 않은 정렬 기준(orderBy)일 때 DeokhugamException 발생")
  void findAllBooks_Fail_InvalidOrderBy() {
    // given
    String invalidOrderBy = "이상한정렬기준";

    // when & thenz
    assertThatThrownBy(() -> bookService.findAllBooks("keyword", invalidOrderBy, SortDirection.ASC, null, null, 10))
        .isInstanceOf(DeokhugamException.class);

    verify(bookRepository, never()).findBooks(any(), any(), any(), any(), any(), anyInt());
  }

  // =========================================================================
  // 도서 수정 (updateBook) 테스트
  // =========================================================================

  @Test
  @DisplayName("썸네일 수정 없이 도서 수정 성공 - null이 아닌 필드만 정상적으로 업데이트된다.")
  void updateBook_without_thumbnail_Success() {
    // given
    UUID bookId = UUID.randomUUID();

    // 수정 요청 (제목과 설명만 바꾸고, 저자는 null로 보냄)
    BookUpdateRequest request = new BookUpdateRequest(
        "새로운 제목", "새로운 저자", null, null, null
    );

    bookDto.setTitle(request.getTitle());
    bookDto.setPublisher(request.getPublisher());

    given(bookRepository.findByIdAndIsDeletedFalse(bookId)).willReturn(Optional.of(book));
    given(bookMapper.toDto(book)).willReturn(bookDto);

    // when
    BookDto result = bookService.updateBook(request, bookId, null);

    // then
    AssertionsForClassTypes.assertThat(book.getTitle()).isEqualTo("새로운 제목");
    AssertionsForClassTypes.assertThat(book.getAuthor()).isEqualTo("새로운 저자");
    AssertionsForClassTypes.assertThat(book.getDescription()).isEqualTo("테스트 설명"); // null이 들어왔으니 기존 값 유지

    verify(bookRepository).findByIdAndIsDeletedFalse(bookId);
    verify(bookMapper).toDto(book);
  }

  @Test
  @DisplayName("썸네일 교체 도서 수정 성공 - 기존 썸네일이 있다면 트랜잭션 커밋 후 삭제된다.")
  void updateBook_with_thumbnail_Success() throws Exception {
    // given
    UUID bookId = UUID.randomUUID();
    book.addThumbnail("old-s3-url.jpg"); // 기존 썸네일 존재

    MultipartFile mockFile = new org.springframework.mock.web.MockMultipartFile("thumbnail", "new.jpg", "image/jpeg", "data".getBytes());
    String newS3Url = "new-s3-url.jpg";

    given(bookRepository.findByIdAndIsDeletedFalse(bookId)).willReturn(Optional.of(book));
    given(s3ThumbnailStorage.upload(mockFile)).willReturn(newS3Url);
    given(bookMapper.toDto(book)).willReturn(bookDto);

    // 가짜 트랜잭션 환경 열기
    org.springframework.transaction.support.TransactionSynchronizationManager.initSynchronization();

    try {
      // when
      bookService.updateBook(new BookUpdateRequest(null, null, null, null, null), bookId, mockFile);

      // then: 새 이미지 URL로 업데이트 되었는지 확인
      assertThat(book.getThumbnailUrl()).isEqualTo(newS3Url);

      // 트랜잭션 커밋 상태 강제 발생 (afterCommit 콜백 실행)
      List<org.springframework.transaction.support.TransactionSynchronization> syncs =
          org.springframework.transaction.support.TransactionSynchronizationManager.getSynchronizations();
      for (org.springframework.transaction.support.TransactionSynchronization sync : syncs) {
        sync.afterCommit();
      }

      // 기존 썸네일(old-s3-url.jpg) 삭제 메서드가 호출되었는지 검증!
      verify(s3ThumbnailStorage).delete("old-s3-url.jpg");

    } finally {
      org.springframework.transaction.support.TransactionSynchronizationManager.clearSynchronization();
    }
  }

  @Test
  @DisplayName("도서 수정 실패 - 존재하지 않는 도서일 때 예외 발생")
  void updateBook_Fail_NotFound() {
    // given
    UUID bookId = UUID.randomUUID();
    BookUpdateRequest request = new BookUpdateRequest("새로운 제목", null, null, null, null);

    given(bookRepository.findByIdAndIsDeletedFalse(bookId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> bookService.updateBook(request, bookId, null))
        .isInstanceOf(DeokhugamException.class);
  }

  // =========================================================================
  // 도서 삭제 (Soft / Hard Delete) 테스트
  // =========================================================================

  @Test
  @DisplayName("도서 소프트 삭제 성공 - 도서의 상태가 삭제(isDeleted = true)로 변경된다.")
  void deleteBook_Success() {
    // given
    UUID bookId = UUID.randomUUID();

    given(bookRepository.findByIdAndIsDeletedFalse(bookId)).willReturn(Optional.of(book));

    // when
    bookService.deleteBook(bookId);

    // then
    AssertionsForClassTypes.assertThat(book.isDeleted()).isTrue();

    verify(bookRepository).findByIdAndIsDeletedFalse(bookId);
    verify(bookRepository, never()).delete(any());
  }

  @Test
  @DisplayName("도서 영구 삭제 성공 - S3에 이미지가 있다면 삭제 메서드가 호출되어야 한다.")
  void permanentDeleteBook_with_thumbnail_Success() {
    // given
    UUID bookId = UUID.randomUUID();
    book.addThumbnail("https://s3.aws.com/old-image.jpg"); // 기존 이미지 세팅
    given(bookRepository.findByIdAndIsDeletedTrue(bookId)).willReturn(Optional.of(book));

    // when
    bookService.permanentDeleteBook(bookId);

    // then
    verify(s3ThumbnailStorage).delete("https://s3.aws.com/old-image.jpg");
    verify(bookRepository).delete(book);
  }

  @Test
  @DisplayName("도서 삭제 실패 - 존재하지 않는 도서일 때")
  void deleteBook_Fail_NotFound() {
    UUID bookId = UUID.randomUUID();
    given(bookRepository.findByIdAndIsDeletedFalse(bookId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> bookService.deleteBook(bookId))
        .isInstanceOf(DeokhugamException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOK_NOT_FOUND);
  }

  @Test
  @DisplayName("도서 영구 삭제 실패 - 논리 삭제된 도서가 아닐 때")
  void permanentDeleteBook_Fail_NotFound() {
    UUID bookId = UUID.randomUUID();
    given(bookRepository.findByIdAndIsDeletedTrue(bookId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> bookService.permanentDeleteBook(bookId))
        .isInstanceOf(DeokhugamException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOK_NOT_FOUND);
  }

  // =========================================================================
  // 외부 API (Naver & OCR) 성공 및 방어 로직 커버리지
  // =========================================================================

  @Test
  @DisplayName("네이버 도서 검색 성공 - 정상적으로 DTO로 파싱되고 이미지를 다운로드한다.")
  void getBookInfoByIsbn_Success() {
    // given
    String isbn = "9788966263134";
    String validImageUrl = "https://shopping-phinf.pstatic.net/test.jpg"; // 허용된 도메인
    byte[] mockImageBytes = "mock-image".getBytes();

    NaverBookResponse.NaverBookItem mockItem = new NaverBookResponse.NaverBookItem(
        "테스트 제목", "저자", "설명", "출판사", "20231015", "1234", validImageUrl
    );
    List<NaverBookItem> responses = List.of(mockItem);
    NaverBookResponse mockResponse = new NaverBookResponse(responses.size(), responses);

    // Naver RestClient 모킹
    given(naverRestClient.get().uri((Function)any()).retrieve().body(NaverBookResponse.class))
        .willReturn(mockResponse);

    // Default RestClient 모킹 (이미지 다운로드 성공)
    given(defaultRestClient.get().uri(validImageUrl).retrieve().body(byte[].class))
        .willReturn(mockImageBytes);

    // when
    NaverBookDto result = bookService.getBookInfoByIsbn(isbn);

    // then
    assertThat(result.title()).isEqualTo("테스트 제목");
    assertThat(result.publishedDate()).isEqualTo(LocalDate.of(2023, 10, 15));
    assertThat(result.thumbnailImage()).isEqualTo(mockImageBytes);
  }

  @Test
  @DisplayName("네이버 도서 검색 방어 - SSRF 방어를 위해 허용되지 않은 이미지 도메인은 다운로드하지 않는다.")
  void getBookInfoByIsbn_SSRF_Blocked() {
    // given
    String isbn = "9788966263134";
    String hackingImageUrl = "https://hacker-domain.com/malicious.exe"; // 허용되지 않은 도메인

    NaverBookResponse.NaverBookItem mockItem = new NaverBookResponse.NaverBookItem(
        "테스트 제목", "저자", "설명", "출판사", "20231015", "1234", hackingImageUrl
    );

    List<NaverBookItem> responses = List.of(mockItem);
    given(naverRestClient.get().uri((Function)any()).retrieve().body(NaverBookResponse.class))
        .willReturn(new NaverBookResponse(responses.size(), responses));

    // when
    com.team01.deokhugam.book.dto.naver.NaverBookDto result = bookService.getBookInfoByIsbn(isbn);

    // then: 이미지 다운로드 로직이 차단되어 null이 반환되어야 함
    assertThat(result.thumbnailImage()).isNull();
    verify(defaultRestClient, never()).get(); // 절대 호출되면 안 됨!
  }

  @Test
  @DisplayName("OCR ISBN 추출 성공 - 이미지에서 정규식에 맞는 ISBN을 성공적으로 추출한다.")
  void getIsbnByOcr_Success() {
    // given
    org.springframework.test.util.ReflectionTestUtils.setField(bookService, "ocrApiKey", "test-key");
    MultipartFile mockImage = new org.springframework.mock.web.MockMultipartFile("image", "test.jpg", "image/jpeg", "image".getBytes());

    // OCR이 읽어온 텍스트: 잡동사니 문자열 사이에 ISBN이 숨어있는 상황
    String ocrText = "책 가격 15,000원 바코드 번호 ISBN 978-89-6626-313-4 읽어주세요";
    OcrSpaceResponse.ParsedResult parsedResult = new OcrSpaceResponse.ParsedResult(ocrText,"에러 없음");
    OcrSpaceResponse mockResponse = new OcrSpaceResponse(List.of(parsedResult), false);

    given(ocrRestClient.post().uri(anyString()).contentType(any()).body(any(Object.class)).retrieve().body(OcrSpaceResponse.class))
        .willReturn(mockResponse);

    // when
    String result = bookService.getIsbnByOcr(mockImage);

    // then (특수문자와 공백이 제거된 13자리 숫자만 딱 떨어져야 함)
    assertThat(result).isEqualTo("9788966263134");
  }

  @Test
  @DisplayName("OCR API 호출 중 서버 에러(RuntimeException) 발생 시 커스텀 예외로 감싸서 던진다.")
  void getIsbnByOcr_Fail_ApiServerError() {
    // given
    org.springframework.test.util.ReflectionTestUtils.setField(bookService, "ocrApiKey", "test-key");
    MultipartFile mockImage = new org.springframework.mock.web.MockMultipartFile("image", "test.jpg", "image/jpeg", "image".getBytes());

    // RestClient가 500 에러 등을 뱉어서 통신 자체가 터졌을 때! (catch 블록 커버리지)
    given(ocrRestClient.post().uri(anyString()).contentType(any()).body(any(Object.class)).retrieve().body(OcrSpaceResponse.class))
        .willThrow(new RuntimeException("Connection Timeout!"));

    // when & then
    assertThatThrownBy(() -> bookService.getIsbnByOcr(mockImage))
        .isInstanceOf(DeokhugamException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.API_SERVER_ERROR);
  }
}
