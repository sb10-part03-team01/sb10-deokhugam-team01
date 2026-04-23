package com.team01.deokhugam.poweruser.repository;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.poweruser.entity.PowerUser;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PowerUserRepository extends JpaRepository<PowerUser, UUID> {

  void deleteByPeriod(DashboardPeriod period);

  @EntityGraph(attributePaths = "user")
  List<PowerUser> findByPeriodOrderByRankAsc(DashboardPeriod period, Pageable pageable);

  @EntityGraph(attributePaths = "user")
  List<PowerUser> findByPeriodAndRankGreaterThanAndCreatedAtAfterOrderByRankAscCreatedAtAsc(
      DashboardPeriod period, Long rank, OffsetDateTime after, Pageable pageable);

  @EntityGraph(attributePaths = "user")
  List<PowerUser> findByPeriodOrderByRankDesc(DashboardPeriod period, Pageable pageable);

  @EntityGraph(attributePaths = "user")
  List<PowerUser> findByPeriodAndRankLessThanAndCreatedAtBeforeOrderByRankDescCreatedAtDesc(
      DashboardPeriod period, Long rank, OffsetDateTime after, Pageable pageable);

  long countByPeriod(DashboardPeriod period);
}
