package com.team01.deokhugam.notification.repository;

import com.team01.deokhugam.notification.entity.Notification;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface NotificationRepositoryCustom {

  List<Notification> findByUserIdOrderByCreatedAtDesc(
      UUID userId,
      Pageable pageable
  );

  List<Notification> findByUserIdAndCreatedAtBeforeOrderByCreatedAtDesc(
      UUID userId,
      OffsetDateTime after,
      UUID cursor,
      Pageable pageable
  );

  List<Notification> findByUserIdOrderByCreatedAtAsc(
      UUID userId,
      Pageable pageable
  );

  List<Notification> findByUserIdAndCreatedAtAfterOrderByCreatedAtAsc(
      UUID userId,
      OffsetDateTime after,
      UUID cursor,
      Pageable pageable
  );

}
