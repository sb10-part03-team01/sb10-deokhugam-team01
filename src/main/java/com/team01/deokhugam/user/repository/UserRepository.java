package com.team01.deokhugam.user.repository;

import com.team01.deokhugam.user.entity.User;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

  /*
  SELECT COUNT(*) > 0
  FROM User u
  WHERE u.email = ? AND u.deletedAt IS NULL
   */
  boolean existsByEmailAndDeletedAtIsNull(String email);

  /*
  SELECT u
  FROM User u
  WHERE u.email = :email AND u.deletedAt IS NULL
   */
  Optional<User> findByEmailAndDeletedAtIsNull(String email);

  /*
  SELECT u
  FROM User u
  WHERE u.id = :id AND u.deletedAt IS NULL
   */
  Optional<User> findByIdAndDeletedAtIsNull(UUID id);

  /*
  SELECT u
  FROM User u
  WHERE u.isDeleted = true AND u.deletedAt < :expiredBefore
   */
  List<User> findAllByIsDeletedTrueAndDeletedAtBefore(OffsetDateTime expiredBefore);

  /*
  DELETE FROM users
  WHERE is_deleted = true AND deleted_at < :expiredBefore
  - 하나의 SQL로 일괄 삭제 (데이터 양이 Chunk를 쓰기에는 양이 많지 않음)
  - 반환값: 실제 삭제된 행 수 (메타테이블 기록용)
   */
  // JPA의 @Query는 기본적으로 SELECT 전용. INSERT/UPDATE/DELETE는 @Modifying 필요
  // clearAutomatically = true: 쿼리 실행 후 영속성 컨텍스트를 비워버림
  // 영속성 컨텍스트 초기화하여 1차 캐시와 DB 동기화
  // 삭제된 엔티티가 1차 캐시에 남아있으면 이후 쿼리에서 이상한 결과 나올 수 있음
  @Modifying(clearAutomatically = true)
  @Query("DELETE FROM User u WHERE u.isDeleted = true AND u.deletedAt < :expiredBefore")
  int deleteAllByIsDeletedTrueAndDeletedAtBefore(
      @Param("expiredBefore") OffsetDateTime expiredBefore
  );
}
