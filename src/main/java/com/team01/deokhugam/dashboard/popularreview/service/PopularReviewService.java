package com.team01.deokhugam.dashboard.popularreview.service;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.dashboard.popularreview.dto.CursorPageResponsePopularReviewDto;
import com.team01.deokhugam.global.enums.SortDirection;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public interface PopularReviewService {

  void calculatePopularReviews(DashboardPeriod period, LocalDate calculatedDate,
      OffsetDateTime start, OffsetDateTime end);

  CursorPageResponsePopularReviewDto getPopularReviews(
      DashboardPeriod period,
      SortDirection direction,
      String cursor,
      OffsetDateTime after,
      Integer limit
  );
}
