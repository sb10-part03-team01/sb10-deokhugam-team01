package com.team01.deokhugam.notification.service;

import com.team01.deokhugam.notification.entity.Notification;
import com.team01.deokhugam.notification.repository.NotificationRepository;
import com.team01.deokhugam.review.entity.Review;
import com.team01.deokhugam.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationsServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;

  @Override
  public void create(Review review, User user, String content) {

    log.info("[CREATE_NOTIFICATION] 리뷰 유저 확인 : reviewId={}", review.getId());
    User reviewOwner = review.getUser();

    if (reviewOwner.equals(user)) {
      log.info("[CREATE_NOTIFICATION] 리뷰 유저, 알림 받을 유저 동일 userId={}", user.getId());
      return;
    }

    log.info("[CREATE_NOTIFICATION] 알림 생성 시작 reviewId={}, reviewOwnerId={}", review.getId(),
        reviewOwner.getId());
    Notification notification = new Notification(review, reviewOwner, content);
    notificationRepository.save(notification);
    log.info("[CREATE_NOTIFICATION] 알림 저장 완료 notificationId={}", notification.getId());
  }

  @Override
  public void confirmed(Notification notification) {

    log.info("[UPDATE_NOTIFICATION] 알림 확인 : notificationId={}", notification.getId());

    if (!notification.isRead()) {
      log.info("[UPDATE_NOTIFICATION] 알림 읽음 상태로 변경 notificationId={}", notification.getId());

      notification.markAsRead();
      notificationRepository.save(notification);

      log.info("[UPDATE_NOTIFICATION] 알림 저장 완료 notificationId={}", notification.getId());
    } else {

      log.info("[UPDATE_NOTIFICATION] 이미 읽음 상태 입니다 notificationId={}", notification.getId());

    }
  }

  @Override
  public void confirmedAll(User user) {
    log.info("[UPDATE_NOTIFICATION] 전체 알림 읽음 처리 시작 userId={}",user.getId());
    List<Notification> unReadList = notificationRepository.findAllByUserIdAndIsReadFalse(user.getId());

    unReadList.forEach(Notification::markAsRead);
    log.info("[UPDATE_NOTIFICATION] 전체 알림 읽음 처리 완료 userId={}",user.getId());
    notificationRepository.saveAll(unReadList);
    log.info("[UPDATE_NOTIFICATION] 알림 저장 완료 userId={}", user.getId());
  }

  @Override
  @Scheduled
  public void delete(User user) {

  }
}
