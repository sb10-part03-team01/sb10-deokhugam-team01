package com.team01.deokhugam.book.dto.naver;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record NaverBookDto (
    String title,
    String author,
    String description,
    String publisher,
    LocalDate publishedDate,
    String isbn,
    byte[] thumbnailImage
){}
