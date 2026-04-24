package com.team01.deokhugam.batch.service;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.dashboard.poweruser.entity.PowerUser;
import com.team01.deokhugam.dashboard.poweruser.repository.PowerUserRepository;
import com.team01.deokhugam.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardBatchService {
  private final PowerUserRepository powerUserRepository;
  private final UserRepository userRepository;

  @Transactional
  public void calculatePowerUserRanking(LocalDate baseDate) {

    for (DashboardPeriod period : DashboardPeriod.values()) {
      //1. 기간 시작/종료 시간
      OffsetDateTime start = period.getStartDateTime(baseDate).atOffset(ZoneOffset.UTC);
      OffsetDateTime end = period.getEndDateTime(baseDate).atOffset(ZoneOffset.UTC);
      // 2.유저별 데이터 조회
      // TODO: 유저별 리뷰 인기점수 합 조회
      // TODO: 유저별 좋아요 수 조회
      // TODO: 유저별 댓글 수 조회
      // 3. 점수 계산
      Map<UUID, Double> activityScoreMap = new HashMap<>();
      // activityScoreMap.put(userId, (reviewScoreSum * 0.5) + (likeCount * 0.2) + (commentCount * 0.3));
      // 4. rank 부여
      List<Map.Entry<UUID, Double>> rank = activityScoreMap.entrySet().stream()
          .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
          .toList();
      // 5. 기존 삭제
      powerUserRepository.deleteByPeriod(period);
      // 6. 저장
      List<PowerUser> rankings = new ArrayList<>();
      for (int i = 0; i < rank.size(); i++) {
        UUID userId = rank.get(i).getKey();
        rankings.add(PowerUser.builder()
            .user(userRepository.findById(userId).orElseThrow())
            .period(period)
            .calculatedDate(OffsetDateTime.now(ZoneOffset.UTC))
            .rank(i + 1)
            .score(rank.get(i).getValue())
            .reviewScoreSum(0.0) // TODO
            .likeCount(0L) // TODO
            .commentCount(0L) // TODO
            .build());
      }
      powerUserRepository.saveAll(rankings);
    }

  }
}
