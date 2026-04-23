package com.team01.deokhugam.poweruser.repository;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.poweruser.entity.PowerUser;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PowerUserRepository extends JpaRepository<PowerUser, UUID> {

  void deleteByPeriod(DashboardPeriod period);


  List<PowerUser> findByPeriodOrderByRankAsc(DashboardPeriod period, Pageable pageable);

  List<PowerUser> findByPeriodAndRankGreaterThanOrderByRankAsc(DashboardPeriod period, Long cursor,
      Pageable pageable);


  List<PowerUser> findByPeriodOrderByRankDesc(DashboardPeriod period, Pageable pageable);

  List<PowerUser> findByPeriodAndRankLessThanOrderByRankDesc(DashboardPeriod period, Long cursor,
      Pageable pageable);

  long countByPeriod(DashboardPeriod period);

}
