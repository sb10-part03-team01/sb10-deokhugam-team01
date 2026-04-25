package com.team01.deokhugam.book.dto.naver;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.With;

@With
public record NaverBookResponse (
    int total,
    List<NaverBookItem> items
){
  public record NaverBookItem(
      String title,
      String author,
      String description,
      String publisher,
      @JsonProperty("pubdate")
      String publishedDate,
      String isbn,
      @JsonProperty("image")
      String thumbnailImage
  ){}
}
