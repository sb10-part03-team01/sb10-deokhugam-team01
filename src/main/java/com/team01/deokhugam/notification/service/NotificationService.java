package com.team01.deokhugam.notification.service;

import com.team01.deokhugam.notification.entity.Notification;
import com.team01.deokhugam.review.entity.Review;
import com.team01.deokhugam.user.entity.User;

public interface NotificationService {
  void create(Review review, User user, String content);
  void confirmed(Notification notification);
  void confirmedAll(User user);
  void delete(User user);
}
