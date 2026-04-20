package com.team01.deokhugam.book;

import com.team01.deokhugam.book.dto.BookCreateRequest;
import com.team01.deokhugam.book.dto.BookDto;
import com.team01.deokhugam.book.repository.BookRepository;
import com.team01.deokhugam.global.exception.book.BookNotFoundException;
import com.team01.deokhugam.global.exception.book.DuplicatedIsbnException;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import com.team01.deokhugam.global.pagination.CursorPaginationUtils;
import com.team01.deokhugam.global.pagination.PageLimitPolicy;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class BookService {
  private final BookMapper bookMapper;
  private final BookRepository bookRepository;

  @Transactional
  public BookDto createBook(BookCreateRequest request, MultipartFile thumbnail){
    // isbn이 빈 문자열(공백)로 들어올 시 방어 로직
    String safeIsbn = StringUtils.hasText(request.getIsbn()) ? request.getIsbn().trim() : null;

    // isbn 중복 예외 처리
    if(safeIsbn != null && bookRepository.existsByIsbn(safeIsbn)){
      throw new DuplicatedIsbnException(safeIsbn);
    }
    // 도서 객체 생성
    Book book = Book.builder().
        title(request.getTitle()).
        author(request.getAuthor()).
        description(request.getDescription()).
        publisher(request.getPublisher()).
        publishedDate(request.getPublishedDate()).
        isbn(safeIsbn).
        build();
    // 썸네일 저장
    if(thumbnail != null && !thumbnail.isEmpty()){
      // 추후 s3로 업로드, presignURL 가져오는 로직 추가
    }
    // 만약 두 사용자가 동시에 같은 isbn으로 등록시 둘다 중복 검사에서는 통과하지만 등록시에는 uinque제약 조건으로
    // DataIntegrityViolationException가 발생하기 때문에 해당 예외 발생시 커스텀 예외로 응답하도록 함
    // 이를 TOCTOU (Time-Of-Check-Time-Of-Use) 문제라고 함
    try {
      Book savedBook = bookRepository.saveAndFlush(book);
      return bookMapper.toDto(savedBook);
    }
    catch (DataIntegrityViolationException ex) {
      throw new DuplicatedIsbnException(safeIsbn);
    }

  }

  @Transactional(readOnly = true)
  public CursorPageResponse<BookDto> findAllBooks(String keyword, String orderBy, String direction, String cursor, OffsetDateTime after, Integer limit){
    Set<String> allowedOrderBy = Set.of("title", "rating", "reviewCount", "publishedDate");
    Set<String> allowedDirection = Set.of("ASC", "DESC");

    // 추후에 커스텀 예외로 바꿀 예정
    if(!allowedOrderBy.contains(orderBy)){
      throw new IllegalArgumentException("올바른 정렬기준이 아닙니다");
    }
    if(!allowedDirection.contains(direction)){
      throw new IllegalArgumentException("올바른 정렬 방향이 아닙니다");
    }

    int normalizedLimit = PageLimitPolicy.normalize(limit);

    List<Book> books = bookRepository.findBooks(keyword, orderBy, direction, cursor, after, normalizedLimit);

    long totalElements = bookRepository.countBooks(keyword);

    List<BookDto> bookDtos = books.stream()
        .map(bookMapper::toDto)
        .toList();

    // BookDto를 받으면 String 타입으로 반환
    Function<BookDto, String> dynamicCursorExtractor = dto ->
        switch (orderBy){
          case "rating" -> String.valueOf(dto.getRating());
          case "reviewCount" -> String.valueOf(dto.getReviewCount());
          case "publishedDate" -> dto.getPublishedDate().toString();
          default -> dto.getTitle();
        };

    return CursorPaginationUtils.of(
        bookDtos,
        normalizedLimit,
        totalElements,
        dynamicCursorExtractor,
        BookDto::getCreatedAt
    );
  }

  @Transactional(readOnly = true)
  public BookDto findBook(UUID bookId){
    Book book = bookRepository.findByIdAndIsDeletedFalse(bookId)
        .orElseThrow(() -> new BookNotFoundException(bookId));

    return bookMapper.toDto(book);
  }

}
