package com.team01.deokhugam.notification.mapper;

import com.team01.deokhugam.notification.dto.NotificationDto;
import com.team01.deokhugam.notification.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

  public NotificationDto toDto(Notification notification) {
    return new NotificationDto(
        notification.getId(),
        notification.getUser().getId(),
        notification.getReview().getId(),
        notification.getReview().getContent(),
        notification.getContent(),
        notification.isRead(),
        notification.getCreatedAt(),
        notification.getUpdatedAt()
    );
  }
}
