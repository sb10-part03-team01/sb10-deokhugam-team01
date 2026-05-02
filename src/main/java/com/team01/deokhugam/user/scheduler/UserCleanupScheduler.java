package com.team01.deokhugam.user.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/*
@Scheduled를 트리커 역할로만 사용하고, 실제 삭제는 Spring Batch Job에 위임
- 동일 JobParameter로는 Spring Batch가 재실행을 거부하므로 runId(timestamp) 부여
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {

  private final JobLauncher jobLauncher;
  private final Job userCleanupJob;

  /*
  @Scheduled의 cron은 zone 명시 필요 (서버 JVM 기본 타임존 의존 방지)
   */
  @Scheduled(
      cron = "${deokhugam.user.cleanup.cron}",
      zone = "UTC"
  )
  public void runUserCleanupJob() throws Exception {
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong("runId", System.currentTimeMillis()) // 고유한 JobParameter로 재실행 허용
        .toJobParameters();

    log.info("[USER_CLEANUP] 배치 실행 요청");
    jobLauncher.run(userCleanupJob, jobParameters);
  }
}
