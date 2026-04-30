package com.team01.deokhugam.dashboard.popularreview.dto;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.pagination.PageLimitPolicy;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record PopularReviewSearchCondition(
    DashboardPeriod period,
    SortDirection direction,
    String cursor,
    OffsetDateTime after,
    Integer limit,
    LocalDate calculatedDate
) {

  public PopularReviewSearchCondition {
    limit = PageLimitPolicy.normalize(limit);
  }
}
