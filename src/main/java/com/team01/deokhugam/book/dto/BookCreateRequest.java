package com.team01.deokhugam.book.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookCreateRequest {
  @NotNull(message = "제목은 null일 수 없다")
  @Size(max = 255)
  private String title;

  @NotNull(message = "저자는 null일 수 없다")
  @Size(max = 100)
  private String author;

  @NotNull(message = "설명은 null일 수 없다")
  private String description;

  @NotNull(message = "출판사는 null일 수 없다")
  @Size(max = 100)
  private String publisher;

  @NotNull(message = "출판 일자는 null일 수 없다")
  private LocalDate publishedDate;

  @Size(max = 20)
  private String isbn;
}
