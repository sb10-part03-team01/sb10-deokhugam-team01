package com.team01.deokhugam.book.controller;

import com.team01.deokhugam.book.dto.BookCreateRequest;
import com.team01.deokhugam.book.dto.BookDto;
import com.team01.deokhugam.book.dto.BookUpdateRequest;
import com.team01.deokhugam.book.dto.naver.NaverBookDto;
import com.team01.deokhugam.book.service.BookService;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "도서 관리", description = "도서 관련 API")
@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping("/api/books")
public class BookController implements BookApi{
  private final BookService bookService;

  @Override
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BookDto> postBook(
      @Valid @RequestPart("bookData") BookCreateRequest bookData,
      @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage) {

    log.info("도서 등록 요청 수신: title={}", bookData.getTitle());
    BookDto response = bookService.createBook(bookData, thumbnailImage);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Override
  @GetMapping
  public ResponseEntity<CursorPageResponse<BookDto>> getBooks(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false, defaultValue = "title") String orderBy,
      @RequestParam(required = false, defaultValue = "DESC") SortDirection direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) OffsetDateTime after,
      @RequestParam(required = false, defaultValue = "50") int limit) {

    log.info("도서 목록 조회 요청: keyword={}, orderBy={}", keyword, orderBy);
    CursorPageResponse<BookDto> response = bookService.findAllBooks(
        keyword, orderBy, direction, cursor, after, limit
    );
    return ResponseEntity.ok().body(response);
  }

  @Override
  @GetMapping(value = "/{bookId}")
  public ResponseEntity<BookDto> getBook(@PathVariable("bookId") UUID bookId) {
    log.info("도서 단건 조회 요청: bookId={}", bookId);
    BookDto response = bookService.findBook(bookId);
    return ResponseEntity.ok(response);
  }

  @Override
  @PatchMapping(value = "/{bookId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BookDto> updateBook(
      @PathVariable("bookId") UUID bookId,
      @Valid @RequestPart("bookData") BookUpdateRequest request,
      @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage) {

    log.info("도서 정보 수정 요청: bookId={}", bookId);
    BookDto response = bookService.updateBook(request, bookId, thumbnailImage);
    return ResponseEntity.ok(response);
  }

  @Override
  @DeleteMapping(value = "/{bookId}")
  public ResponseEntity<Void> deleteBook(@PathVariable("bookId") UUID bookId) {
    log.info("도서 논리 삭제 요청: bookId={}", bookId);
    bookService.deleteBook(bookId);
    log.info("도서 논리 삭제 완료: {}", bookId);
    return ResponseEntity.noContent().build();
  }

  @Override
  @DeleteMapping(value = "/{bookId}/hard")
  public ResponseEntity<Void> permanentDeleteBook(@PathVariable("bookId") UUID bookId) {
    log.info("도서 물리 삭제 요청: bookId={}", bookId);
    bookService.permanentDeleteBook(bookId);
    log.info("도서 물리 삭제 완료: {}", bookId);
    return ResponseEntity.noContent().build();
  }

  @Override
  @GetMapping(value = "/info")
  public ResponseEntity<NaverBookDto> getBookInfoByIsbn(@RequestParam(required = true) String isbn) {
    log.info("ISBN 네이버 도서 정보 조회 요청: isbn={}", isbn);
    NaverBookDto response = bookService.getBookInfoByIsbn(isbn);

    return ResponseEntity.ok(response);
  }

  @Override
  @PostMapping(value = "/isbn/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<String> getIsbnByOcr(
      @RequestPart("image") MultipartFile image
  ) {
    log.info("OCR 기반 ISBN 조회 요청: filename={}, size={}bytes", image.getOriginalFilename(), image.getSize());

    // 빈파일 검증
    if(image.isEmpty()){
      throw new DeokhugamException(ErrorCode.EMPTY_FILE_UPLOADED);
    }
    // 파일 크기 검증 1MB 초과시 예외
    long maxSize = 1024 * 1024;
    if (image.getSize() > maxSize) {
      throw new DeokhugamException(ErrorCode.FILE_SIZE_EXCEEDED, Map.of(
          "size", image.getSize(),
          "rule", "파일의 크기는 최대 1MB입니다"
      ));
    }
    // 파일 형식 검증
    String contentType = image.getContentType();
    if(!StringUtils.hasText(contentType) ||
        !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/jpg"))){
      throw new DeokhugamException(ErrorCode.INVALID_FILE, Map.of(
          "content-type", contentType,
          "rule", "JPG, PNG 이미지만 업로드 가능합니다."
      ));
    }

    String response = bookService.getIsbnByOcr(image);

    return ResponseEntity.ok(response);
  }
}
