package com.team01.deokhugam.user.scheduler;

import com.team01.deokhugam.user.config.UserCleanupProperties;
import com.team01.deokhugam.user.entity.User;
import com.team01.deokhugam.user.repository.UserRepository;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// TODO: Spring Batch로 리펙터링
// 논리 삭제된 사용자를 보존 기간 경과 후 물리 삭제하는 스케줄러
@Slf4j
@Component
public class UserCleanupScheduler {

  private final UserRepository userRepository;
  private final UserCleanupProperties userCleanupProperties;

  public UserCleanupScheduler(
      UserRepository userRepository,
      UserCleanupProperties userCleanupProperties
  ) {
    this.userRepository = userRepository;
    this.userCleanupProperties = userCleanupProperties;
  }

  /*
   초 분 시 일 월 요일
   0  0  3 *  *  *
     * 모든 값
     / 간격 지정
     , 여러 값 나열
   배포 환경에서는 매일 3시에 메서드 실행. 개발 환경에서는 1분마다 메서드 실행
   */
  // @Scheduled의 cron은 서버 JVM의 기본 타임존을 따른다. -> zone 명시 필요
  @Scheduled(
      cron = "${deokhugam.user.cleanup.cron}",
      zone = "Asia/Seoul"
  )
  @Transactional
  public void cleanupSoftDeletedUsers() {
    /*
    삭제 기준 시간(expiredBefore) = 현재시간 - 보존기간
    예시) 현재 2025-04-19 14:00 -> expiredBefore 2025-04-18 14:00
    */
    OffsetDateTime expiredBefore
        = OffsetDateTime.now().minusMinutes(userCleanupProperties.retentionMinutes());
    // 전체 대상을 List로 한 번에 메모리에 올림. 데이터가 많을 경우 OOM 이슈 발생 가능 -> 추후 Spring Batch로 리팩터링
    List<User> targets = userRepository.findAllByIsDeletedTrueAndDeletedAtBefore(expiredBefore);
    if (targets.isEmpty()) {
      return;
    }
    log.info("논리삭제 유저 물리 삭제 시작: count={}, expiredBefore={}", targets.size(), expiredBefore);
    userRepository.deleteAll(targets);
    log.info("논리삭제 유저 물리 삭제 완료: count={}", targets.size());
  }
}
