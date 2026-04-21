package com.team01.deokhugam.book.controller;

import com.team01.deokhugam.book.dto.BookCreateRequest;
import com.team01.deokhugam.book.dto.BookDto;
import com.team01.deokhugam.book.dto.BookUpdateRequest;
import com.team01.deokhugam.book.dto.naver.NaverBookDto;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "도서 관리", description = "도서 관련 API")
public interface BookApi {

  /// POST - /api/books - 도서 등록
  @Operation(summary = "새로운 도서 등록", description = "JSON 데이터와 썸네일 이미지를 함께 전송하여 새로운 도서를 등록합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "도서 등록 성공",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDto.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (필수 값 누락 등)",
          content = @Content(mediaType = "application/json")),
      @ApiResponse(responseCode = "409", description = "이미 존재하는 ISBN",
          content = @Content(mediaType = "application/json"))
  })
  ResponseEntity<BookDto> postBook(
      @Parameter(description = "도서 기본 정보 (JSON 포맷)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      BookCreateRequest bookData,
      @Parameter(description = "도서 썸네일 이미지 (선택 사항)", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
      MultipartFile thumbnailImage
  );

  /// GET - /api/books - 도서 목록 조회
  @Operation(summary = "도서 목록 페이징 조회", description = "검색어, 정렬 기준, 커서를 기반으로 도서 목록을 무한 스크롤(커서 페이징) 방식으로 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "도서 목록 조회 성공",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = CursorPageResponse.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 파라미터 (허용되지 않은 정렬 기준 등)",
          content = @Content(mediaType = "application/json"))
  })
  ResponseEntity<CursorPageResponse<BookDto>> getBooks(
      @Parameter(description = "검색어 (제목, 저자 등)", example = "해리포터") String keyword,
      @Parameter(description = "정렬 기준 (title, rating, reviewCount, publishedDate)", example = "rating") String orderBy,
      @Parameter(description = "정렬 방향 (ASC 또는 DESC)", example = "DESC") SortDirection direction,
      @Parameter(description = "메인 커서 (이전 페이지 마지막 항목의 정렬 기준 값)", example = "4.5") String cursor,
      @Parameter(description = "보조 커서 (이전 페이지 마지막 항목의 생성 일시)", example = "2026-04-17T12:00:00Z") OffsetDateTime after,
      @Parameter(description = "페이지 당 조회 건수", example = "50") int limit
  );

  /// GET - /api/books/{bookId} - 도서 단건 조회
  @Operation(summary = "도서 단건 조회", description = "도서 식별자(UUID)를 통해 특정 도서의 상세 정보를 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "도서 조회 성공",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDto.class))),
      @ApiResponse(responseCode = "404", description = "존재하지 않거나 삭제된 도서",
          content = @Content(mediaType = "application/json"))
  })
  ResponseEntity<BookDto> getBook(
      @Parameter(description = "조회할 도서의 ID (UUID)", example = "123e4567-e89b-12d3-a456-426614174000") UUID bookId
  );

  /// PATCH - /api/books/{bookId} - 도서 정보 수정
  @Operation(summary = "도서 정보 수정", description = "도서의 일부 정보(JSON) 및 썸네일 이미지를 수정합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "도서 수정 성공",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDto.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 값",
          content = @Content(mediaType = "application/json")),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 도서",
          content = @Content(mediaType = "application/json"))
  })
  ResponseEntity<BookDto> updateBook(
      @Parameter(description = "수정할 도서의 ID (UUID)", example = "123e4567-e89b-12d3-a456-426614174000") UUID bookId,
      @Parameter(description = "수정할 도서 정보 (JSON 포맷)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) BookUpdateRequest request,
      @Parameter(description = "변경할 썸네일 이미지 (선택 사항)", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) MultipartFile thumbnailImage
  );

  /// DELETE - /api/books/{bookId} - 도서 논리 삭제
  @Operation(summary = "도서 논리 삭제", description = "도서 식별자(UUID)를 통해 도서를 삭제(논리적 삭제) 처리합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "도서 삭제 성공 (반환 데이터 없음)"),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 도서",
          content = @Content(mediaType = "application/json"))
  })
  ResponseEntity<Void> deleteBook(
      @Parameter(description = "삭제할 도서의 ID (UUID)", example = "123e4567-e89b-12d3-a456-426614174000") UUID bookId
  );

  /// DELETE - /api/books/{bookId}/hard - 도서 물리 삭제
  @Operation(summary = "도서 물리 삭제", description = "도서 식별자(UUID)를 통해 논리 삭제가 진행된 도서를 삭제(물리적 삭제) 처리합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "도서 삭제 성공 (반환 데이터 없음)"),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 도서",
          content = @Content(mediaType = "application/json"))
  })
  ResponseEntity<Void> permanentDeleteBook(
      @Parameter(description = "삭제할 도서의 ID (UUID)", example = "123e4567-e89b-12d3-a456-426614174000") UUID bookId
  );

  /// GET - /api/books/info - ISBN으로 도서 정보 조회
  @Operation(summary = "ISBN 도서 정보 자동 완성 (외부 연동)", description = "ISBN 번호를 통해 네이버 도서 검색 API를 호출하여 폼 자동 완성용 도서 정보를 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "도서 정보 조회 성공",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = NaverBookDto.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (ISBN 파라미터 누락)",
          content = @Content(mediaType = "application/json")),
      @ApiResponse(responseCode = "404", description = "해당 ISBN으로 검색된 도서가 없음",
          content = @Content(mediaType = "application/json")),
      @ApiResponse(responseCode = "500", description = "외부 API(네이버) 통신 오류",
          content = @Content(mediaType = "application/json"))
  })
  ResponseEntity<NaverBookDto> getBookInfoByIsbn(
      @Parameter(description = "검색할 도서의 ISBN 번호 (10자리 또는 13자리)", example = "9788966263158")
      String isbn
  );
}
