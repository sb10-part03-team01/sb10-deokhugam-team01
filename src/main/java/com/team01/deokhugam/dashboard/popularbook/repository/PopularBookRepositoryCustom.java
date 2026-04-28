package com.team01.deokhugam.dashboard.popularbook.repository;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.dashboard.popularbook.entity.PopularBook;
import com.team01.deokhugam.global.enums.SortDirection;
import java.time.OffsetDateTime;
import java.util.List;

public interface PopularBookRepositoryCustom {
  List<PopularBook> findAllByCursor(
      DashboardPeriod period,
      SortDirection direction,
      String cursor,
      OffsetDateTime after,
      int limit);

  long countByPeriod(DashboardPeriod period);
}
