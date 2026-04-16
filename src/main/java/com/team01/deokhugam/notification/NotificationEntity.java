package com.team01.deokhugam.notification;

import com.team01.deokhugam.global.entity.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
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
    this.reviewId = reviewId;
    this.userId = userId;
    this.content = content;
    this.isRead = false;
  }

  public void markAsRead() {
    isRead = true;
  }


}
