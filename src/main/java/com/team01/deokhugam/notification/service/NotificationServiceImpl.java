package com.team01.deokhugam.notification.service;

import com.team01.deokhugam.global.exception.ErrorCode;
import com.team01.deokhugam.global.exception.notification.NotificationException;
import com.team01.deokhugam.global.pagination.CursorPageRequest;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import com.team01.deokhugam.global.pagination.CursorPaginationUtils;
import com.team01.deokhugam.global.pagination.PageLimitPolicy;
import com.team01.deokhugam.notification.dto.NotificationCreateRequest;
import com.team01.deokhugam.notification.dto.NotificationDto;
import com.team01.deokhugam.notification.entity.Notification;
import com.team01.deokhugam.notification.repository.NotificationRepository;
import com.team01.deokhugam.user.entity.User;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

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

    log.info("[CREATE_NOTIFICATION] 알림 생성 시작 reviewId={}, reviewOwnerId={}",
        request.review().getId(),
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
    log.info("[UPDATE_NOTIFICATION] 전체 알림 읽음 처리 시작 userId={}", user.getId());
    List<Notification> unReadList = notificationRepository.findAllByUserIdAndIsReadFalse(
        user.getId());

    unReadList.forEach(Notification::markAsRead);
    log.info("[UPDATE_NOTIFICATION] 전체 알림 읽음 처리 완료 userId={}", user.getId());
  }

  @Override
  @Transactional
  public void cleanupReadNotifications() {
    OffsetDateTime oneWeekAgo = OffsetDateTime.now(ZoneOffset.UTC).minusWeeks(1);
    log.info("[DELETE_NOTIFICATION] 1주일 경과 알림 삭제 시작 기준시간={}", oneWeekAgo);

    notificationRepository.deleteAllByIsReadTrueAndUpdatedAtBefore(oneWeekAgo);

    log.info("[DELETE_NOTIFICATION] 1주일 경과 알림 삭제 완료");
  }

  @Override
  @Transactional(readOnly = true)
  public CursorPageResponse<NotificationDto> findAll(UUID userId, CursorPageRequest request) {
    log.info("[FIND_ALL_NOTIFICATION] 알림 목록 조회 시작 userId={}, after={}, limit={}",
        userId, request.after(), request.limit());

    int normalizedLimit = PageLimitPolicy.normalize(request.limit());
    PageRequest pageable = PageRequest.of(0, normalizedLimit + 1);

    List<Notification> results;

    if (request.after() == null) {
      if (request.cursor() != null && !request.cursor().isBlank()) {
        throw new NotificationException(ErrorCode.INVALID_CURSOR_PAGINATION,
            Map.of("cursor", request.cursor()));
      }
      log.info("[FIND_ALL_NOTIFICATION] 첫 페이지 조회 userId={}", userId);
      results = notificationRepository
          .findByUserIdOrderByCreatedAtDesc(userId, pageable);
    } else {
      if (request.cursor() == null || request.cursor().isBlank()) {
        throw new NotificationException(ErrorCode.INVALID_CURSOR_PAGINATION,
            Map.of("after", request.after()));
      }
      UUID cursorId;
      try {
        cursorId = UUID.fromString(request.cursor());
      } catch (IllegalArgumentException e) {
        throw new NotificationException(ErrorCode.INVALID_CURSOR_FORMAT,
            Map.of("cursor", request.cursor()));
      }
      log.info("[FIND_ALL_NOTIFICATION] 다음 페이지 조회 userId={}, after={}", userId, request.after());
      results = notificationRepository
          .findByUserIdAndCreatedAtBeforeOrderByCreatedAtDesc(
              userId, request.after(), cursorId, pageable);
    }

    log.info("[FIND_ALL_NOTIFICATION] 조회 완료 userId={}, 조회된 개수={}", userId, results.size());

    long totalElements = notificationRepository.countByUserId(userId);

    List<NotificationDto> dtoList = results.stream()
        .map(n -> new NotificationDto(
            n.getId(),
            n.getUser().getId(),
            n.getReview().getId(),
            n.getReview().getContent(),
            n.getContent(),
            n.isRead(),
            n.getCreatedAt(),
            n.getUpdatedAt()
        ))
        .toList();

    return CursorPaginationUtils.of(
        dtoList,
        normalizedLimit,
        totalElements,
        dto -> dto.id().

            toString(),

        NotificationDto::createdAt
    );
  }

  @Override
  public Notification findById(UUID notificationId) {
    return notificationRepository.findById(notificationId)
        .orElseThrow(() -> new NotificationException(ErrorCode.NOTIFICATION_NOT_FOUND,
            Map.of("notificationId", notificationId)));
  }
}
