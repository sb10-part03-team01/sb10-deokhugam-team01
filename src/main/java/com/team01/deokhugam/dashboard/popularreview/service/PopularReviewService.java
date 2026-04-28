package com.team01.deokhugam.dashboard.popularreview.service;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public interface PopularReviewService {

  void calculatePopularReviews(DashboardPeriod period, LocalDate calculatedDate,
      OffsetDateTime start, OffsetDateTime end);
}
