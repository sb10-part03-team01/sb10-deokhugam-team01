package com.team01.deokhugam.dashboard.popularreview.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record CursorPageResponsePopularReviewDto(
    List<PopularReviewDto> content,
    String nextCursor,
    OffsetDateTime nextAfter,
    int size,
    long totalElements,
    boolean hasNext
) {

}
