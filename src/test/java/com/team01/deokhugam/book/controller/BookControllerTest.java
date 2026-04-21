package com.team01.deokhugam.book.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team01.deokhugam.book.dto.BookCreateRequest;
import com.team01.deokhugam.book.dto.BookDto;
import com.team01.deokhugam.book.dto.BookUpdateRequest;
import com.team01.deokhugam.book.service.BookService;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(BookController.class)
class BookControllerTest {
  @Autowired
  private MockMvc mockMvc; // 요청을 흉내 내는 객체

  @Autowired
  private ObjectMapper objectMapper; // 객체 <-> JSON 변환기

  @MockitoBean
  private BookService bookService;

  private UUID bookId;
  private BookDto bookDto;

  @BeforeEach
  void setUp() {
    bookId = UUID.randomUUID();
    bookDto = BookDto.builder()
        .id(bookId)
        .title("테스트 도서")
        .author("테스트 저자")
        .description("이 책은 테스트를 위해 만들어진 아주 훌륭한 도서입니다.")
        .publisher("테스트 출판사")
        .publishedDate(LocalDate.of(2026, 4, 16))
        .isbn("1234567890")
        .thumbnailUrl("https://image.example.com/test-thumbnail.jpg")
        .reviewCount(150)
        .rating(4.8)
        .createdAt(OffsetDateTime.now().minusDays(1))
        .updatedAt(OffsetDateTime.now())
        .build();
  }

  @Test
  @DisplayName("도서 등록 API - JSON 데이터와 썸네일을 보내면 201 응답과 Location 헤더가 반환된다.")
  void postBook_Success() throws Exception {
    // given
    BookCreateRequest request = new BookCreateRequest(
        "테스트 도서", "테스트 저자", "설명", "출판사", LocalDate.now(), "1234567890"
    );

    MockMultipartFile bookData = new MockMultipartFile(
        "bookData",
        "", // json파일은 이름 필요 없음
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request) // 객체를 JSON Byte로 변환
    );


    MockMultipartFile thumbnailImage = new MockMultipartFile(
        "thumbnailImage",
        "test.jpg",
        MediaType.IMAGE_JPEG_VALUE,
        "dummy image content".getBytes()
    );

    given(bookService.createBook(any(BookCreateRequest.class), any())).willReturn(bookDto);

    // when & then
    mockMvc.perform(multipart("/api/books")
            .file(bookData)
            .file(thumbnailImage)
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print()) // 콘솔에 요청/응답 로그 출력 (디버깅용)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("테스트 도서")); // JSON 응답 검증
  }

  @Test
  @DisplayName("도서 목록 조회 API - 파라미터가 Enum 및 지정된 타입으로 잘 변환되어 200 응답이 반환된다.")
  void getBooks_Success() throws Exception {

    // given
    CursorPageResponse<BookDto> response = new CursorPageResponse<>(
        List.of(bookDto),         // content: 실제 데이터 리스트
        null,                     // nextCursor: 다음 커서 (마지막 페이지라 가정한 null)
        null,                     // nextAfter: 다음 보조 커서 시간
        10,                       // size: 요청한 limit 사이즈
        1L,                       // totalElements: 전체 데이터 개수
        false                     // hasNext: 다음 페이지 존재 여부
    );

    given(bookService.findAllBooks(eq("해리"), eq("rating"), eq(SortDirection.DESC), any(), any(), eq(10)))
        .willReturn(response);

    // when & then
    mockMvc.perform(get("/api/books")
            .param("keyword", "해리")
            .param("orderBy", "rating")
            .param("direction", "DESC")
            .param("limit", "10")
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].title").value("테스트 도서"));
  }

  @Test
  @DisplayName("도서 단건 조회 API - 정상적으로 200 응답과 도서 정보가 반환된다.")
  void getBook_Success() throws Exception {
    // given
    given(bookService.findBook(bookId)).willReturn(bookDto);

    // when & then
    mockMvc.perform(get("/api/books/{bookId}", bookId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(bookId.toString()))
        .andExpect(jsonPath("$.title").value("테스트 도서"));
  }

  @Test
  @DisplayName("도서 수정 API - Multipart PATCH 요청 시 200 응답이 반환된다.")
  void updateBook_Success() throws Exception {
    // given
    BookUpdateRequest request = new BookUpdateRequest("수정된 제목", null, null, null, null);

    MockMultipartFile bookData = new MockMultipartFile(
        "bookData",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    bookDto.setTitle("수정된 제목");
    given(bookService.updateBook(any(BookUpdateRequest.class), eq(bookId), any())).willReturn(bookDto);

    // Spring MockMvc의 multipart()는 기본적으로 POST로만 작동
    // PATCH로 보내려면 아래처럼 빌더를 통해 강제로 HTTP Method를 덮어씌워야 함
    MockMultipartHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.multipart("/api/books/{bookId}", bookId);
    // 강제로 PATCH로 덮어씌움
    builder.with(req -> {
      req.setMethod(HttpMethod.PATCH.name());
      return req;
    });

    // when & then
    mockMvc.perform(builder
            .file(bookData)
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("수정된 제목"));
  }

  @Test
  @DisplayName("도서 논리 삭제 API - 정상 호출 시 204 No Content가 반환된다.")
  void deleteBook_Success() throws Exception {

    // when & then
    mockMvc.perform(delete("/api/books/{bookId}", bookId))
        .andDo(print())
        .andExpect(status().isNoContent());

    verify(bookService).deleteBook(bookId); // 서비스가 한 번 호출되었는지 확인
  }

  @Test
  @DisplayName("도서 물리 삭제 API - 정상 호출 시 204 No Content가 반환된다.")
  void permanentDeleteBook_success() throws Exception{

    // when & then
    mockMvc.perform(delete("/api/books/{bookId}/hard", bookId))
        .andDo(print())
        .andExpect(status().isNoContent());

    verify(bookService).permanentDeleteBook(bookId);
  }
}
