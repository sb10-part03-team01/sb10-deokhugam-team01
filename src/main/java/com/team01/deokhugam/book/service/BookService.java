package com.team01.deokhugam.book.service;

import com.team01.deokhugam.book.BookMapper;
import com.team01.deokhugam.book.dto.BookCreateRequest;
import com.team01.deokhugam.book.dto.BookDto;
import com.team01.deokhugam.book.dto.BookUpdateRequest;
import com.team01.deokhugam.book.dto.OcrSpaceResponse;
import com.team01.deokhugam.book.dto.naver.NaverBookDto;
import com.team01.deokhugam.book.dto.naver.NaverBookResponse;
import com.team01.deokhugam.book.dto.naver.NaverBookResponse.NaverBookItem;
import com.team01.deokhugam.book.entity.Book;
import com.team01.deokhugam.book.repository.BookRepository;
import com.team01.deokhugam.book.storage.ThumbnailStorage;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.exception.book.BookNotFoundException;
import com.team01.deokhugam.global.exception.book.DuplicatedIsbnException;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import com.team01.deokhugam.global.pagination.CursorPaginationUtils;
import com.team01.deokhugam.global.pagination.PageLimitPolicy;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class BookService {

  private final BookMapper bookMapper;
  private final BookRepository bookRepository;
  private final RestClient naverRestClient;
  private final RestClient defaultRestClient;
  private final RestClient ocrRestClient;
  private final ThumbnailStorage thumbnailStorage;

  // isbn패턴 정규표현식: 978 혹은 979로 시작해서 (-)와 숫자가 조합되서 나오는 10자리~20자리의 문자열
  private static final Pattern ISBN_PATTERN = Pattern.compile("97[89][0-9\\s-]{10,20}");
  @Value("${deokhugam.ocr.api.key}")
  private String ocrApiKey;

  public BookService(BookMapper bookMapper,
      BookRepository bookRepository,
      @Qualifier("naverRestClient") RestClient naverRestClient,
      @Qualifier("defaultRestClient") RestClient defaultRestClient,
      @Qualifier("ocrRestClient") RestClient ocrRestClient,
      ThumbnailStorage thumbnailStorage) {
    this.bookMapper = bookMapper;
    this.bookRepository = bookRepository;
    this.naverRestClient = naverRestClient;
    this.defaultRestClient = defaultRestClient;
    this.ocrRestClient = ocrRestClient;
    this.thumbnailStorage = thumbnailStorage;
  }

  @Transactional
  public BookDto createBook(BookCreateRequest request, MultipartFile thumbnail) {
    // isbn이 빈 문자열(공백)로 들어올 시 방어 로직
    String safeIsbn = StringUtils.hasText(request.getIsbn()) ? request.getIsbn().trim() : null;

    // isbn 중복 예외 처리
    if (safeIsbn != null && bookRepository.existsByIsbn(safeIsbn)) {
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
    if (thumbnail != null && !thumbnail.isEmpty()) {
      try {
        String s3Url = thumbnailStorage.upload(thumbnail);
        book.addThumbnail(s3Url);
      } catch (IOException e){
        throw new RuntimeException("파일 업로드중 문제가 발생했습니다");
      }
    }
    // 만약 두 사용자가 동시에 같은 isbn으로 등록시 둘다 중복 검사에서는 통과하지만 등록시에는 uinque제약 조건으로
    // DataIntegrityViolationException가 발생하기 때문에 해당 예외 발생시 커스텀 예외로 응답하도록 함
    // 이를 TOCTOU (Time-Of-Check-Time-Of-Use) 문제라고 함
    try {
      Book savedBook = bookRepository.saveAndFlush(book); // 즉시 INSERT 실행
      return bookMapper.toDto(savedBook);
    } catch (DataIntegrityViolationException ex) {
      throw new DuplicatedIsbnException(safeIsbn);
    }

  }

  @Transactional(readOnly = true)
  public CursorPageResponse<BookDto> findAllBooks(String keyword, String orderBy, SortDirection direction, String cursor, OffsetDateTime after, Integer limit){
    Set<String> allowedOrderBy = Set.of("title", "rating", "reviewCount", "publishedDate");

    // 추후에 커스텀 예외로 바꿀 예정
    if(!allowedOrderBy.contains(orderBy)){
      throw new IllegalArgumentException("올바른 정렬기준이 아닙니다");
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

  @Transactional
  public BookDto updateBook(BookUpdateRequest request, UUID bookId, MultipartFile thumbnailImage) {
    Book book = bookRepository.findByIdAndIsDeletedFalse(bookId)
        .orElseThrow(() -> new BookNotFoundException(bookId));

    if(request.getTitle() != null){
      book.updateTitle(request.getTitle());
    }
    if(request.getAuthor() != null){
      book.updateAuthor(request.getAuthor());
    }
    if(request.getDescription() != null){
      book.updateDescription(request.getDescription());
    }
    if(request.getPublisher() != null){
      book.updatePublisher(request.getPublisher());
    }
    if(request.getPublishedDate() != null){
      book.updatePublishedDate(request.getPublishedDate());
    }
    if(thumbnailImage != null && !thumbnailImage.isEmpty()){
      if(book.getThumbnailUrl() != null){
        thumbnailStorage.delete(book.getThumbnailUrl());
      }

      try {
        String s3Url = thumbnailStorage.upload(thumbnailImage);
        book.updateThumbnailUrl(s3Url);
      } catch (IOException e){
        throw new RuntimeException("파일 업로드중 오류가 발생했습니다",e);
      }

    }

    return bookMapper.toDto(book);
  }

  @Transactional
  public void deleteBook(UUID bookId){
    Book book = bookRepository.findByIdAndIsDeletedFalse(bookId)
        .orElseThrow(() -> new BookNotFoundException(bookId));

    book.softDelete();
  }

  @Transactional
  public void permanentDeleteBook(UUID bookId){
    Book book = bookRepository.findByIdAndIsDeletedTrue(bookId)
        .orElseThrow(() -> new BookNotFoundException(bookId));

    bookRepository.delete(book);
  }

  public NaverBookDto getBookInfoByIsbn(String isbn){
    String safeIsbn = isbn.trim();

    NaverBookResponse response = naverRestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/book_adv.json")
            .queryParam("d_isbn",safeIsbn)
            .build())
        .retrieve()
        .body(NaverBookResponse.class);

    if (response == null || response.items() == null || response.items().isEmpty()) {
      throw new IllegalArgumentException("해당 isbn으로 검색된 도서가 없습니다.");
    }

    NaverBookItem naverBookItem = response.items().get(0);

    // String으로 받은 출판일자를 date형태로 변환
    LocalDate parsedDate = LocalDate.parse(
        naverBookItem.publishedDate(),
        DateTimeFormatter.ofPattern("yyyyMMdd")
    );

    byte[] imageBytes = downloadImageFromUrl(naverBookItem.thumbnailImage());

    return NaverBookDto.builder()
        .title(naverBookItem.title())
        .author(naverBookItem.author())
        .publisher(naverBookItem.publisher())
        .publishedDate(parsedDate)
        .isbn(naverBookItem.isbn())
        .description(naverBookItem.description())
        .thumbnailImage(imageBytes)
        .build();
  }

  public String getIsbnByOcr(MultipartFile image){
    if (!StringUtils.hasText(ocrApiKey)) {
      throw new IllegalStateException("OCR SPACE API 인증 정보(자격 증명)를 설정해야 합니다");
    }
    try {
      MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
      formData.add("apikey", ocrApiKey);
      formData.add("file", image.getResource());
      formData.add("language", "auto");
      formData.add("isOverlayRequired", "false");
      formData.add("OCREngine", "2"); // 숫자 및 특수문자 인식에 더 강한 엔진 사용

      OcrSpaceResponse response = ocrRestClient.post()
          .uri("/parse/image")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .body(formData)
          .retrieve()
          .body(OcrSpaceResponse.class);

      List<OcrSpaceResponse.ParsedResult> parsedResults =
          response == null ? null : response.parsedResults();
      // ocr 응답 데이터의 npe, 응답 데이터는 있지만 안의 요소가 npe, 안의 요소인 리스트는 있지만 리스트의 요소가 npe,
      // 리스트의 요소는 있지만 그 요소가 공백일때의 방어로직
      if (response == null || response.isErroredOnProcessing() || parsedResults == null
          || parsedResults.isEmpty() || !StringUtils.hasText(parsedResults.get(0).parsedText())) {
        throw new IllegalArgumentException("이미지에서 텍스트를 추출할 수 없습니다.");
      }

      // parseResults에는 페이지의 개수만큼 parseText가 담김
      // parseText는 한 페이지의 모든 문자열을 담은 문자열로 반환됨
      String parsedText = response.parsedResults().get(0).parsedText();
      // 그 모든 문자열에서 내가 정한 패턴의 문자열만 추출
      Matcher matcher = ISBN_PATTERN.matcher(parsedText);

      // 해당 패턴의 문자열을 찾았다면
      while(matcher.find()) {
        // 숫자를 제외하고 모두 없앰
        String rawIsbn = matcher.group().replaceAll("[^0-9]", "");
        // 만약 길이가 13 이상이면 길이 13에 맞게 반환
        String isbn = rawIsbn.length() >= 13 ? rawIsbn.substring(0, 13) : null;
        if (isbn != null) {
          return isbn;
        }
      }

      throw new IllegalArgumentException("이미지에서 ISBN 형식을 찾을 수 없습니다.");
    }
    catch (Exception e){
      log.error("OCR 처리 중 오류 발생: {}", e.getMessage());
      throw new RuntimeException("OCR 이미지 처리 중 서버 오류가 발생했습니다.");
    }
  }

  private byte[] downloadImageFromUrl(String imageUrl){
    if(imageUrl == null || imageUrl.isBlank()){
      return null;
    }
    // SSRF(Server-Side Request Forgery) 문제 해결
    if (!imageUrl.startsWith("https://shopping-phinf.pstatic.net/") &&
        !imageUrl.startsWith("https://bookthumb-phinf.pstatic.net/")) { // 네이버 도서 썸네일 도메인들
      log.warn("허용되지 않은 이미지 호스트라서 다운로드를 차단합니다: {}", imageUrl);
      return null;
    }

    try {
      // 네이버 호스트만 허용했으므로 악성 거대 파일(OOM)이 올 확률은 사실상 0%.
      return defaultRestClient.get()
          .uri(imageUrl)
          .retrieve()
          .body(byte[].class);
    } catch (Exception e) {
      // 이미지 다운로드에 실패해도 전체 비즈니스 로직이 터지지 않도록 null 반환
      log.warn("이미지 다운로드 실패 (URL: {}), 사유: {}", imageUrl, e.getMessage());
      return null;
    }
  }
}
