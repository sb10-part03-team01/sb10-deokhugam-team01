package com.team01.deokhugam.notification.controller;

import com.team01.deokhugam.notification.dto.NotificationDto;
import com.team01.deokhugam.notification.dto.NotificationUpdateRequest;
import com.team01.deokhugam.notification.entity.Notification;
import com.team01.deokhugam.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "알림 관리", description = "알림 관련 API")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @PatchMapping("/{notificationId}")
  public ResponseEntity<NotificationDto> confirm(
      @PathVariable UUID notificationId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID userId,
      @RequestBody NotificationUpdateRequest request
  ) {
    Notification notification = notificationService.findById(notificationId);
    NotificationDto dto = notificationService.confirm(notification, userId);
    return ResponseEntity.ok(dto);
  }


}
