package com.team01.deokhugam.notification.repository;

import com.team01.deokhugam.notification.entity.Notification;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID>,
    NotificationRepositoryCustom {

  List<Notification> findAllByUserIdAndIsReadFalse(UUID userId);

  void deleteAllByIsReadTrueAndUpdatedAtBefore(OffsetDateTime cutoff);

  long countByUserId(UUID userId);
}
