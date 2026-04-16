package com.team01.deokhugam.notification.repository;

import com.team01.deokhugam.notification.entity.Notification;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  Page<Notification> findAllByUserId(UUID userId, Pageable pageable);

  void deleteAllByIsReadTrueAndUpdatedAtBefore(OffsetDateTime cutoff);

}
