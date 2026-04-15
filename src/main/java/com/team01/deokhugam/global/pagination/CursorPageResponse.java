package com.team01.deokhugam.global.pagination;

import java.time.OffsetDateTime;
import java.util.List;

public record CursorPageResponse<T>(
    List<T> content,
    String nextCursor,
    OffsetDateTime nextAfter,
    int size,
    long totalElements,
    boolean hasNext
) {

}