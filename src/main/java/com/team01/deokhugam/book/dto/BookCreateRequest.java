package com.team01.deokhugam.book.dto;

import jakarta.validation.constraints.NotBlank;
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

  @NotBlank(message = "제목은 비어 있을 수 없다")
  @Size(max = 255)
  private String title;

  @NotBlank(message = "작가는 비어 있을 수 없다")
  @Size(max = 100)
  private String author;

  @NotBlank(message = "설명은 비어 있을 수 없다")
  private String description;

  @NotNull(message = "출판사는 Null일 수 없다")
  @Size(max = 100)
  private String publisher;

  @NotBlank(message = "출판일은 비어 있을 수 없다")
  private LocalDate publishedDate;

  @Size(max = 20)
  private String isbn;
}
