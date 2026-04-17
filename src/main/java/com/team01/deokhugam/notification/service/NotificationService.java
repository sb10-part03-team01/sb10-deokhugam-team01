package com.team01.deokhugam.notification.service;

import com.team01.deokhugam.notification.dto.NotificationCreateRequest;
import com.team01.deokhugam.notification.entity.Notification;
import com.team01.deokhugam.review.entity.Review;
import com.team01.deokhugam.user.entity.User;

public interface NotificationService {
  void create(NotificationCreateRequest request);
  void confirm(Notification notification);
  void confirmAll(User user);
  // void cleanupReadNotifications();
}
