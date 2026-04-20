package com.team01.deokhugam.notification.service;

import com.team01.deokhugam.global.pagination.CursorPageRequest;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import com.team01.deokhugam.notification.dto.NotificationCreateRequest;
import com.team01.deokhugam.notification.dto.NotificationDto;
import com.team01.deokhugam.notification.entity.Notification;

import com.team01.deokhugam.user.entity.User;
import java.util.UUID;

public interface NotificationService {
  void create(NotificationCreateRequest request);
  void confirm(Notification notification);
  void confirmAll(User user);
  void cleanupReadNotifications();
  CursorPageResponse<NotificationDto> findAll(UUID userId, CursorPageRequest request);
}
