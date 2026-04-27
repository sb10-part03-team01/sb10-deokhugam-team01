package com.team01.deokhugam.dashboard.popularbook.repository;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.dashboard.popularbook.entity.PopularBook;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopularBookRepository
    extends JpaRepository<PopularBook, UUID>, PopularBookRepositoryCustom {
  // 같은 기간 + 같은 calculatedDate 배치를 다시 돌릴 때 기존 데이터를 지우기 위한 메서드
  void deleteByPeriodTypeAndCalculatedDate(DashboardPeriod periodType, LocalDate calculatedDate);
}
