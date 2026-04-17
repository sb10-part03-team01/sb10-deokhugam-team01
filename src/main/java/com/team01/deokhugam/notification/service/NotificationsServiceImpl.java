package com.team01.deokhugam.notification.service;

import com.team01.deokhugam.notification.dto.NotificationCreateRequest;
import com.team01.deokhugam.notification.entity.Notification;
import com.team01.deokhugam.notification.repository.NotificationRepository;
import com.team01.deokhugam.review.entity.Review;
import com.team01.deokhugam.user.entity.User;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationsServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;

  @Override
  @Transactional
  public void create(NotificationCreateRequest request) {

    log.info("[CREATE_NOTIFICATION] 리뷰 유저 확인 : reviewId={}", request.review().getId());
    User reviewOwner = request.review().getUser();

    if (Objects.equals(reviewOwner.getId(), request.actor().getId())) {
      log.info("[CREATE_NOTIFICATION] 리뷰 작성자와 행위자가 동일 userId={}", request.actor().getId());
      return;
    }

    log.info("[CREATE_NOTIFICATION] 알림 생성 시작 reviewId={}, reviewOwnerId={}", request.review().getId(),
        reviewOwner.getId());
    Notification notification = new Notification(request.review(), reviewOwner, request.content());
    notificationRepository.save(notification);
    log.info("[CREATE_NOTIFICATION] 알림 저장 완료 notificationId={}", notification.getId());
  }

  @Override
  @Transactional
  public void confirm(Notification notification) {

    log.info("[UPDATE_NOTIFICATION] 알림 확인 : notificationId={}", notification.getId());

    if (!notification.isRead()) {
      log.info("[UPDATE_NOTIFICATION] 알림 읽음 상태로 변경 notificationId={}", notification.getId());
      notification.markAsRead();
    } else {

      log.info("[UPDATE_NOTIFICATION] 이미 읽음 상태 입니다 notificationId={}", notification.getId());

    }
  }

  @Override
  @Transactional
  public void confirmAll(User user) {
    log.info("[UPDATE_NOTIFICATION] 전체 알림 읽음 처리 시작 userId={}",user.getId());
    List<Notification> unReadList = notificationRepository.findAllByUserIdAndIsReadFalse(user.getId());

    unReadList.forEach(Notification::markAsRead);
    log.info("[UPDATE_NOTIFICATION] 전체 알림 읽음 처리 완료 userId={}",user.getId());
  }


}
