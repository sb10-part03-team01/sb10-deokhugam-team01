package com.team01.deokhugam.batch.service;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.dashboard.poweruser.entity.PowerUser;
import com.team01.deokhugam.dashboard.poweruser.repository.PowerUserRepository;
import com.team01.deokhugam.global.exception.user.UserNotFoundException;
import com.team01.deokhugam.user.entity.User;
import com.team01.deokhugam.user.repository.UserRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardBatchTransactionService {

  private final PowerUserRepository powerUserRepository;
  private final UserRepository userRepository;

  @Transactional
  public void deleteAndSave(DashboardPeriod period, List<Map.Entry<UUID, Double>> rank,
      OffsetDateTime calculatedAt) {

    List<UUID> userIds = rank.stream()
        .map(Map.Entry::getKey)
        .toList();

    Map<UUID, User> mapUser = userRepository.findAllById(userIds)
        .stream().collect(Collectors.toMap(User::getId, u -> u));

    if (mapUser.size() != userIds.size()) {
      UUID missing = userIds.stream()
          .filter(id -> !mapUser.containsKey(id))
          .findFirst()
          .orElseThrow();
      throw new UserNotFoundException(missing);
    }

    //  기존 삭제
    powerUserRepository.deleteByPeriod(period);

    //  저장
    List<PowerUser> rankings = new ArrayList<>();
    for (int i = 0; i < rank.size(); i++) {
      UUID userId = rank.get(i).getKey();
      User user = mapUser.get(userId);
      if (user == null) {
        throw new UserNotFoundException(userId);
      }
      rankings.add(PowerUser.builder()
          .user(user)
          .period(period)
          .calculatedDate(calculatedAt)
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
