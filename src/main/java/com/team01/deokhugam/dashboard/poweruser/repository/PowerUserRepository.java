package com.team01.deokhugam.dashboard.poweruser.repository;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.dashboard.poweruser.entity.PowerUser;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PowerUserRepository extends JpaRepository<PowerUser, UUID> {

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("DELETE FROM PowerUser p WHERE p.period = :period")
  void deleteByPeriod(@Param("period") DashboardPeriod period);

  @EntityGraph(attributePaths = "user")
  List<PowerUser> findByPeriodOrderByRankAsc(DashboardPeriod period, Pageable pageable);

  @EntityGraph(attributePaths = "user")
  @Query("""
      select pu from PowerUser pu
      where pu.period = :period
        and (pu.rank > :rank
             or (pu.rank = :rank and pu.createdAt > :after))
      order by pu.rank asc, pu.createdAt asc
      """)
  List<PowerUser> findNextPageAsc(@Param("period") DashboardPeriod period,
      @Param("rank") Long rank,
      @Param("after") OffsetDateTime after,
      Pageable pageable);

  @EntityGraph(attributePaths = "user")
  List<PowerUser> findByPeriodOrderByRankDesc(DashboardPeriod period, Pageable pageable);

  @EntityGraph(attributePaths = "user")
  @Query("""
      select pu from PowerUser pu
      where pu.period = :period
      and (pu.rank < :rank
      or (pu.rank = :rank and pu.createdAt < :after))
      order by pu.rank desc, pu.createdAt desc
      """)
  List<PowerUser> findNextPageDesc(
      @Param("period") DashboardPeriod period,
      @Param("rank") Long rank,
      @Param("after") OffsetDateTime after,
      Pageable pageable);

  long countByPeriod(DashboardPeriod period);
}
