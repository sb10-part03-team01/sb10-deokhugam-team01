package com.team01.deokhugam.dashboard.poweruser.service;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import com.team01.deokhugam.user.dto.PowerUserDto;
import java.time.OffsetDateTime;

public interface PowerUserService {
  CursorPageResponse<PowerUserDto> getRanking(
      DashboardPeriod period,
      SortDirection direction,
      String cursor,
      OffsetDateTime after,
      int limit
  );

}
