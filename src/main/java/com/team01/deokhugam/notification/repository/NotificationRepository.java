package com.team01.deokhugam.notification.repository;

import com.team01.deokhugam.notification.entity.Notification;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  List<Notification> findAllByUserIdAndIsReadFalse(UUID userId);
  void deleteAllByIsReadTrueAndUpdatedAtBefore(OffsetDateTime cutoff);

  @Query("""
      SELECT n FROM Notification n
      JOIN FETCH n.user
      JOIN FETCH n.review
      WHERE n.user.id = :userId
      ORDER BY n.createdAt DESC, n.id DESC
      """)
  List<Notification> findByUserIdOrderByCreatedAtDesc(
      @Param("userId") UUID userId,
      Pageable pageable
  );

  @Query("""
      SELECT n FROM Notification n
      JOIN FETCH n.user
      JOIN FETCH n.review
      WHERE n.user.id = :userId
      AND (n.createdAt < :after
      OR (n.createdAt = :after AND n.id < :cursor))
      ORDER BY n.createdAt DESC, n.id DESC
      """)
  List<Notification> findByUserIdAndCreatedAtBeforeOrderByCreatedAtDesc(
      @Param("userId") UUID userId,
      @Param("after") OffsetDateTime after,
      @Param("cursor") UUID cursor,
      Pageable pageable
  );

  @Query("""
    SELECT n FROM Notification n
    JOIN FETCH n.user
    JOIN FETCH n.review
    WHERE n.user.id = :userId
    ORDER BY n.createdAt ASC, n.id ASC
    """)
  List<Notification> findByUserIdOrderByCreatedAtAsc(
      @Param("userId") UUID userId,
      Pageable pageable
  );

  @Query("""
    SELECT n FROM Notification n
    JOIN FETCH n.user
    JOIN FETCH n.review
    WHERE n.user.id = :userId
    AND (n.createdAt > :after
    OR (n.createdAt = :after AND n.id > :cursor))
    ORDER BY n.createdAt ASC, n.id ASC
    """)
  List<Notification> findByUserIdAndCreatedAtAfterOrderByCreatedAtAsc(
      @Param("userId") UUID userId,
      @Param("after") OffsetDateTime after,
      @Param("cursor") UUID cursor,
      Pageable pageable
  );
  long countByUserId(UUID userId);
}
