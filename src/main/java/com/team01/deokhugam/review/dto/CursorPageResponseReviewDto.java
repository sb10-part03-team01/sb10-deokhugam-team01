package com.team01.deokhugam.review.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record CursorPageResponseReviewDto(
    List<ReviewDto> content,
    String nextCursor,
    OffsetDateTime nextAfter,
    int size,
    long totalElements,
    boolean hasNext
) {

}