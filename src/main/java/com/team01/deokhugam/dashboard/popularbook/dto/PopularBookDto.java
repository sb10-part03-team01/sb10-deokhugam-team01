package com.team01.deokhugam.dashboard.popularbook.dto;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.dashboard.popularbook.entity.PopularBook;
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
  private DashboardPeriod period;
  private long rank;
  private double score;
  private long reviewCount;
  private double rating;
  private OffsetDateTime createdAt;

  public static PopularBookDto from(PopularBook popularBook, String thumbnailUrl) {
    return PopularBookDto.builder()
        .id(popularBook.getId())
        .bookId(popularBook.getBook().getId())
        .title(popularBook.getBook().getTitle())
        .author(popularBook.getBook().getAuthor())
        // 원본 key가 아닌, local/s3 환경에 맞게 변환된 URL사용
        .thumbnailUrl(thumbnailUrl)
        .period(popularBook.getPeriodType())
        .rank(popularBook.getRank())
        .score(popularBook.getScore())
        .reviewCount(popularBook.getReviewCount())
        .rating(popularBook.getRating())
        .createdAt(popularBook.getCreatedAt())
        .build();
  }
}
