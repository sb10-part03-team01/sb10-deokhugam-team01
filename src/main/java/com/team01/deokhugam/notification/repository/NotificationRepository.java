package com.team01.deokhugam.notification.repository;

import com.team01.deokhugam.notification.entity.Notification;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID>,
    NotificationRepositoryCustom {

  List<Notification> findAllByUserIdAndIsReadFalse(UUID userId);

  void deleteAllByIsReadTrueAndConfirmedAtBefore(OffsetDateTime cutoff);

  long countByUserId(UUID userId);

  boolean existsByReviewIdAndUserIdAndContent(UUID reviewId, UUID userId, String content);
}
