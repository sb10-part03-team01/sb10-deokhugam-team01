package com.team01.deokhugam.dashboard.repository;

import com.team01.deokhugam.dashboard.entity.PopularBook;
import com.team01.deokhugam.global.enums.RankingPeriod;
import com.team01.deokhugam.global.enums.SortDirection;
import java.time.OffsetDateTime;
import java.util.List;

public interface PopularBookRepositoryCustom {
  List<PopularBook> findAllByCursor(
      RankingPeriod period,
      SortDirection direction,
      String cursor,
      OffsetDateTime after,
      int limit);

  long countByPeriod(RankingPeriod period);
}
