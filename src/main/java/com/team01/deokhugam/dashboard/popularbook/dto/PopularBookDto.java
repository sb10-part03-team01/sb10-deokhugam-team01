package com.team01.deokhugam.dashboard.popularbook.dto;

import com.team01.deokhugam.dashboard.popularbook.entity.PopularBook;
import com.team01.deokhugam.global.enums.RankingPeriod;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class PopularBookDto {
  private UUID id;
  private UUID bookId;
  private String title;
  private String author;
  private String thumbnailUrl;
  private RankingPeriod period;
  private long rank;
  private double score;
  private long reviewCount;
  private double rating;
  private OffsetDateTime createdAt;

  public static PopularBookDto from(PopularBook popularBook) {
    return PopularBookDto.builder()
        .id(popularBook.getId())
        .bookId(popularBook.getBook().getId())
        .title(popularBook.getBook().getTitle())
        .author(popularBook.getBook().getAuthor())
        .thumbnailUrl(popularBook.getBook().getThumbnailUrl())
        .period(popularBook.getPeriodType())
        .rank(popularBook.getRank())
        .score(popularBook.getScore())
        .reviewCount(popularBook.getReviewCount())
        .rating(popularBook.getRating())
        .createdAt(popularBook.getCreatedAt())
        .build();
  }
}
