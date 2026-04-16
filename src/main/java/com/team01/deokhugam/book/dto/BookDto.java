package com.team01.deokhugam.book.dto;

import com.team01.deokhugam.book.Book;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class BookDto {
  private UUID id;
  private String title;
  private String author;
  private String description;
  private String publisher;
  private LocalDate publishedDate;
  private String isbn;
  private String thumbnailUrl;
  private int reviewCount;
  private double rating;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

}
