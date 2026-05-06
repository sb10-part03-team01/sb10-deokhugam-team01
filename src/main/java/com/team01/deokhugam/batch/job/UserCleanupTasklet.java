package com.team01.deokhugam.batch.job;

import com.team01.deokhugam.user.config.UserCleanupProperties;
import com.team01.deokhugam.user.repository.UserRepository;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

/*
논리 삭제된 사용자 중 보존기간이 지난 대상을 하나의 SQL로 일괄 물리 삭제
- Step에 의해 한 번 실행되고 RepeatStatus.FINISHED로 종료됨
- 처리 건수는 contribution을 통해 메타데이블 (BATCH_STEP_EXECUTION.write_count)에 기록

[스케줄러/수동 실행]

[Job 실행]

[Step 실행]

[UserCleanupTasklet.execute()]
  1. expiredBefore 계산
  2. userRepository.deleteAllByIsDeletedTrueAndDeletedAtBefore(expiredBefore) 실행 -> 일괄 삭제 SQL
  3. 로그 출력
  4. write_count 메타테이블에 기록
  5. FINISHED 반환

[Step 종료] -> BATCH_STEP_EXECUTION 한 행 남김

[Job 종료] -> BATCH_JOB_EXECUTION 한 행 남김

 */
@Slf4j
@Component
@RequiredArgsConstructor
// Tasklet 인터페이스를 구현하여 배치 작업의 단일 단위를 정의
public class UserCleanupTasklet implements Tasklet {

  // DB에 DELETE 쿼리를 날릴 JPA Repository
  private final UserRepository userRepository;
  // application.yaml에서 보존 기간을 가져올 프로퍼티 클래스
  private final UserCleanupProperties userCleanupProperties;

  // StepContribution: Step 실행에 대한 기록 (예: 처리된 아이템 수 기록)
  // ChunkContext: 잡 파라미터, 스텝 이름 같은 실행 컨텍스트 정보 (여기서는 사용 안함)
  // RepeatStatus: Spring Batch는 이 반환값을 보고 같은 Step 안에서 execute()를 또 호출할지 결정
  //   RepeatStatus.FINISHED: Step 종료 -> 다음 Step으로 진행 (또는 Job 종료)
  //   RepeatStatus.CONTINUABLE: execute()를 다시 호출 (루프)
  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    // 지금으로부터 N분 전 시각 계산 (보존 기간이 지난 시점)
    OffsetDateTime expiredBefore = OffsetDateTime.now()
        .minusMinutes(userCleanupProperties.retentionMinutes());

    // 일괄 삭제 SQL 실행
    int deletedCount = userRepository.deleteAllByIsDeletedTrueAndDeletedAtBefore(expiredBefore);

    log.info("[USER_CLEANUP] 논리 삭제 유저 물리 삭제 완료: "
        + "count={}, expiredBefore={}", deletedCount, expiredBefore);

    // 처리 건수를 메타테이블에 기록 (BATCH_STEP_EXECUTION.write_count)
    // Spring Batch는 실행할 때마다 BATCH_STEP_EXECUTION 테이블애 한 행을 생성함
    // 배치에서 몇 명의 유저를 삭제했는지 확인하기 위한 값
    contribution.incrementWriteCount(deletedCount);
    return RepeatStatus.FINISHED; // 단일 쿼리 처리 종료
  }

}
