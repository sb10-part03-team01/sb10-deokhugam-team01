package com.team01.deokhugam.global.pagination;

public final class PageLimitPolicy {

  public static final int DEFAULT_LIMIT = 50;
  public static final int MAX_LIMIT = 100;

  private PageLimitPolicy() {
  }

  public static int normalize(Integer limit) {
    if (limit == null || limit < 1) {
      return DEFAULT_LIMIT;
    }

    return Math.min(limit, MAX_LIMIT);
  }
}