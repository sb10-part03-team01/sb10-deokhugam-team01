package com.team01.deokhugam.book.controller;

import com.team01.deokhugam.book.dto.BookCreateRequest;
import com.team01.deokhugam.book.dto.BookDto;
import com.team01.deokhugam.book.service.BookService;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "도서 관리", description = "도서 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookController {
  private final BookService bookService;

  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "도서 등록 성공",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDto.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (필수 값 누락 등 유효성 검사 실패)",
          content = @Content(mediaType = "application/json")),
      @ApiResponse(responseCode = "409", description = "이미 존재하는 ISBN",
          content = @Content(mediaType = "application/json"))
  })
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BookDto> postBook(
      @Parameter(description = "도서 정보", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      @Valid @RequestPart("bookData") BookCreateRequest bookData,
      @Parameter(description = "도서 썸네일 이미지", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
      @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage){

    BookDto response = bookService.createBook(bookData, thumbnailImage);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "도서 목록 조회 성공",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = CursorPageResponse.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 파라미터 (허용되지 않은 정렬 기준 등)",
          content = @Content(mediaType = "application/json"))
  })
  @GetMapping
  public ResponseEntity<CursorPageResponse<BookDto>> getBooks(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) @DefaultValue("title") String orderBy,
      @RequestParam(required = false) @DefaultValue("DESC") String direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) OffsetDateTime after,
      @RequestParam(required = false) @DefaultValue("50") int limit
  ){

    CursorPageResponse<BookDto> response = bookService.findAllBooks(keyword, orderBy, direction,cursor,after,limit);

    return ResponseEntity.ok().body(response);
  }


}
