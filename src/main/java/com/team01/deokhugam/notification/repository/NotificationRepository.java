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
      ORDER BY n.createdAt DESC
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
          AND n.createdAt < :after
        ORDER BY n.createdAt DESC
        """)
  List<Notification> findByUserIdAndCreatedAtBeforeOrderByCreatedAtDesc(
      @Param("userId") UUID userId,
      @Param("after") OffsetDateTime after,
      Pageable pageable
  );

  long countByUserId(UUID userId);




}
