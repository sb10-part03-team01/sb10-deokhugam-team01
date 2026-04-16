package com.team01.deokhugam.book;

import com.team01.deokhugam.book.dto.BookCreateRequest;
import com.team01.deokhugam.book.dto.BookDto;
import com.team01.deokhugam.global.exception.book.DuplicatedIsbnException;
import lombok.RequiredArgsConstructor;
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
    String safeIsbn = StringUtils.hasText(request.getIsbn()) ? request.getIsbn() : null;

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
    // 저장
    bookRepository.save(book);

    return bookMapper.toDto(book);
  }

}
