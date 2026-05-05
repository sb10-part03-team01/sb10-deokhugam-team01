package com.team01.deokhugam.dashboard.poweruser.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.dashboard.poweruser.entity.PowerUser;
import com.team01.deokhugam.global.config.JpaConfig;
import com.team01.deokhugam.user.entity.User;
import com.team01.deokhugam.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@Transactional
@TestPropertySource(properties = "spring.profiles.active=test")
@Import({JpaConfig.class, PowerUserRepositoryTest.TestConfig.class})
public class PowerUserRepositoryTest {

  @TestConfiguration
  static class TestConfig {

    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
      return new JPAQueryFactory(entityManager);
    }
  }

  @Autowired
  private PowerUserRepository powerUserRepository;

  @Autowired
  private UserRepository userRepository;

  private User savedUser;

  @BeforeEach
  void setUp() {
    powerUserRepository.deleteAllInBatch();
    userRepository.deleteAllInBatch();

    String uniqueEmail = "test" + java.util.UUID.randomUUID() + "@test.com";
    User user = new User(uniqueEmail, "testUser", "password");
    savedUser = userRepository.save(user);
  }

  private PowerUser createPowerUser(long rank, DashboardPeriod period) {
    return PowerUser.builder()
        .user(savedUser)
        .period(period)
        .calculatedDate(OffsetDateTime.now())
        .rank(rank)
        .score(100.0)
        .reviewScoreSum(50.0)
        .likeCount(10L)
        .commentCount(5L)
        .build();
  }

  @Nested
  @DisplayName("첫페이지 조회")
  class findByPeriodOrder {

    @Test
    @DisplayName("ASC 방향")
    void findByPeriodOrderByAsc() {
      //given
      PowerUser p1 = powerUserRepository.save(createPowerUser(1, DashboardPeriod.DAILY));
      PowerUser p2 = powerUserRepository.save(createPowerUser(2, DashboardPeriod.DAILY));
      PowerUser p3 = powerUserRepository.save(createPowerUser(3, DashboardPeriod.DAILY));
      //when
      List<PowerUser> result = powerUserRepository.findByPeriodOrderByRankAsc(DashboardPeriod.DAILY,
          PageRequest.of(0, 10));
      //then
      assertThat(result).hasSize(3);
      assertThat(result.get(0).getRank()).isEqualTo(1);
      assertThat(result.get(1).getRank()).isEqualTo(2);
      assertThat(result.get(2).getRank()).isEqualTo(3);
    }

    @Test
    @DisplayName("DESC 방향")
    void findByPeriodOrderByDesc() {
      //given
      PowerUser p1 = powerUserRepository.save(createPowerUser(1, DashboardPeriod.DAILY));
      PowerUser p2 = powerUserRepository.save(createPowerUser(2, DashboardPeriod.DAILY));
      PowerUser p3 = powerUserRepository.save(createPowerUser(3, DashboardPeriod.DAILY));
      //when
      List<PowerUser> result = powerUserRepository.findByPeriodOrderByRankDesc(
          DashboardPeriod.DAILY, PageRequest.of(0, 10));
      //then
      assertThat(result).hasSize(3);
      assertThat(result.get(0).getRank()).isEqualTo(3);
      assertThat(result.get(1).getRank()).isEqualTo(2);
      assertThat(result.get(2).getRank()).isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("다음 페이지 조회")
  class findNextPage {

    @Test
    @DisplayName("ASC 방향")
    void findNextPageAsc() {
      //given
      PowerUser p1 = powerUserRepository.save(createPowerUser(1, DashboardPeriod.DAILY));
      PowerUser p2 = powerUserRepository.save(createPowerUser(2, DashboardPeriod.DAILY));
      PowerUser p3 = powerUserRepository.save(createPowerUser(3, DashboardPeriod.DAILY));

      powerUserRepository.flush();

      PowerUser cursor = powerUserRepository.findById(p2.getId()).get();
      //when
      List<PowerUser> result = powerUserRepository.findNextPageAsc(DashboardPeriod.DAILY, 2L,
          cursor.getCreatedAt(), PageRequest.of(0, 2));
      //then
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getRank()).isEqualTo(3);
    }

    @Test
    @DisplayName("DESC 방향")
    void findNextPageDesc() {
      //given
      PowerUser p1 = powerUserRepository.save(createPowerUser(1, DashboardPeriod.DAILY));
      PowerUser p2 = powerUserRepository.save(createPowerUser(2, DashboardPeriod.DAILY));
      PowerUser p3 = powerUserRepository.save(createPowerUser(3, DashboardPeriod.DAILY));

      powerUserRepository.flush();

      PowerUser cursor = powerUserRepository.findById(p2.getId()).get();
      //when
      List<PowerUser> result = powerUserRepository.findNextPageDesc(DashboardPeriod.DAILY, 2L,
          cursor.getCreatedAt(), PageRequest.of(0, 2));
      //then
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getRank()).isEqualTo(1);

    }
  }

}
