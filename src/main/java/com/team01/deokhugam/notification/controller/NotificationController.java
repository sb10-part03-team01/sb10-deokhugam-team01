package com.team01.deokhugam.notification.controller;

import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.pagination.CursorPageRequest;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import com.team01.deokhugam.notification.dto.NotificationDto;
import com.team01.deokhugam.notification.dto.NotificationUpdateRequest;
import com.team01.deokhugam.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    if (!request.confirmed()) {
      return ResponseEntity.badRequest().build();
    }

    NotificationDto dto = notificationService.confirm(notificationId, userId);
    return ResponseEntity.ok(dto);
  }

  @PatchMapping("/read-all")
  public ResponseEntity<Void> readAll(@RequestHeader("Deokhugam-Request-User-ID") UUID userId) {
    notificationService.confirmAll(userId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<CursorPageResponse<NotificationDto>> findAll(
      @RequestHeader("Deokhugam-Request-User-ID") UUID userId,
      @RequestParam(defaultValue = "DESC") SortDirection direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) OffsetDateTime after,
      @RequestParam(required = false) Integer limit
  ){
    CursorPageRequest request = new CursorPageRequest(cursor, after, limit);
    CursorPageResponse<NotificationDto> result = notificationService.findAll(userId,request, direction);
    return ResponseEntity.ok(result);
  }


}
