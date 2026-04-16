package com.team01.deokhugam.notification;

import com.team01.deokhugam.global.entity.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "notifications",
    indexes = {
        @Index(name = "idx_notifications_user_read", columnList = "user_id,is_read"),
        @Index(name = "idx_notifications_review_id", columnList = "review_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationEntity extends BaseUpdatableEntity {

  @Column(name = "review_id", nullable = false)
  private UUID reviewId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "content", length = 255, nullable = false)
  private String content;

  @Column(name = "is_read", nullable = false)
  private boolean isRead = false;

  public NotificationEntity(UUID reviewId, UUID userId, String content) {
    this.reviewId = Objects.requireNonNull(reviewId, "리뷰ID는 null 일 수 없습니다");
    this.userId = Objects.requireNonNull(userId, "유저ID는 null 일 수 없습니다");
    if (content == null || content.isBlank()) {
      throw new IllegalArgumentException("알람 내용은 비어있을 수 없습니다");
    }
    if (content.length() > 255) {
      throw new IllegalArgumentException("알람 내용은 255자 제한입니다");
    }
    this.content = content;
    this.isRead = false;
  }

  public void markAsRead() {
    isRead = true;
  }


}
