package com.team01.deokhugam.batch.config;

import com.team01.deokhugam.batch.job.UserCleanupTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/*
유저 정리 배치의 Job/Step 정의
- Job: userCleanupJob -> 단일 Step 실행
- Step: userCleanupStep -> Tasklet 1개 실행

[Spring Batch의 3계층 개념]
Job (배치 전체. 시작과 끝을 관리함)
  Step (Job 안의 단계. 여러 Step을 순서대로/조건부로 연결 가능)
    Tasklet/Chunk (Step 안에서 실제로 동작하는 코드)

<전체 동작 흐름>
[애플리케이션 시작]
[jobLauncher.run(userCleanupJob, params) 호출]
[BATCH_JOB_EXECUTION에 한 행 INSERT (STARTED)]
[userCleanupStep 시작]
  - 트랜잭션 시작
  - userCleanupTasklet.execute() 호출
  - FINISHED 반환
  - 트랜잭션 커밋
[BATCH_STEP_EXECUTION에 한 행 INSERT (COMPLETED, write_count=N)]
[BATCH_JOB_EXECUTION 상태 UPDATE (COMPLETED)]
 */
@Configuration
@RequiredArgsConstructor
public class UserCleanupBatchConfig {

  public static final String JOB_NAME = "userCleanupJob";
  public static final String STEP_NAME = "userCleanupStep";

  // 배치 메타데이터를 읽고 쓰는 저장소. Spring Boot가 자동으로 생성해줌
  private final JobRepository jobRepository;
  // 트랜잭션 시작/커밋/롤백 담당. Step 실행을 트랜잭션으로 감쌀 때 사용
  private final PlatformTransactionManager transactionManager;
  // 삭제 로직이 담긴 Tasklet. UserCleanupTasklet은 @Component로 등록되어 있고, @RequiredArgsConstructor로 주입받음
  private final UserCleanupTasklet userCleanupTasklet;

  // Job을 만들기 위한 빌더 객체 생성
  @Bean
  public Job userCleanupJob() {
    // Job 이름과 메타데이터를 저장할 jobRepository를 넘김
    return new JobBuilder(JOB_NAME, jobRepository)
        // 이 Job 실행될 때 실행할 Step을 지정
        .start(userCleanupStep())
        .build();
  }

  // Step 정의
  @Bean
  public Step userCleanupStep() {
    // Step 이름과 메타데이터 저장소를 넘김
    return new StepBuilder(STEP_NAME, jobRepository)
        // 이 Step은 Tasklet 방식으로 동작한다고 선언
        // 실행할 Tasklet과 트랜잭션을 관리할 transactionManager를 넘김
        //   transactionManager: 한 Step에서 일어나는 DB 작업들을 하나의 트랜잭션으로 묶어주는 객체
        //   Spring Batch는 트랜잭션 없이 Step 돌리는 것을 허용하지 않음 (필수로 넘겨야 함)
        .tasklet(userCleanupTasklet, transactionManager)
        .build();
  }
}
