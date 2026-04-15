package com.team01.deokhugam.global.pagination;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class CursorPaginationUtils {

  private CursorPaginationUtils() {
  }

  public static <T> CursorPageResponse<T> of(
      List<T> results,
      int limit,
      long totalElements,
      Function<T, String> cursorExtractor,
      Function<T, OffsetDateTime> afterExtractor
  ) {
    Objects.requireNonNull(results, "results must not be null");
    Objects.requireNonNull(cursorExtractor, "cursorExtractor must not be null");
    Objects.requireNonNull(afterExtractor, "afterExtractor must not be null");

    if (limit < 1) {
      throw new IllegalArgumentException("limit must be greater than 0");
    }

    boolean hasNext = results.size() > limit;

    List<T> content = hasNext
        ? results.subList(0, limit)
        : results;

    List<T> safeContent = List.copyOf(content);

    String nextCursor = null;
    OffsetDateTime nextAfter = null;

    if (hasNext && !safeContent.isEmpty()) {
      T lastElement = safeContent.get(safeContent.size() - 1);
      nextCursor = cursorExtractor.apply(lastElement);
      nextAfter = afterExtractor.apply(lastElement);
    }

    return new CursorPageResponse<>(
        safeContent,
        nextCursor,
        nextAfter,
        safeContent.size(),
        totalElements,
        hasNext
    );
  }
}