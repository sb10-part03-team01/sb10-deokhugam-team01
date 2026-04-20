package com.team01.deokhugam.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class BookUpdateRequest {
  @Size(max = 255)
  private String title;

  @Size(max = 100)
  private String author;

  @Size(max = 100)
  private String publisher;

  private String description;
  private LocalDate publishedDate;

}
