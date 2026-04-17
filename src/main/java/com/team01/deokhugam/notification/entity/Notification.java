package com.team01.deokhugam.notification.entity;

import com.team01.deokhugam.global.entity.BaseUpdatableEntity;
import com.team01.deokhugam.review.entity.Review;
import com.team01.deokhugam.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Objects;
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
public class Notification extends BaseUpdatableEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "review_id", nullable = false)
  private Review review;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "content", length = 255, nullable = false)
  private String content;

  @Column(name = "is_read", nullable = false)
  private boolean isRead = false;

  @Column(name = "confirmed_at")
  private OffsetDateTime confirmedAt; // 스키마 파일에 없는 필드 추가

  public Notification(Review review, User user, String content) {
    this.review = Objects.requireNonNull(review, "리뷰ID는 null 일 수 없습니다");
    this.user = Objects.requireNonNull(user, "유저ID는 null 일 수 없습니다");
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
    if (this.isRead) {
      return;
    }
    this.isRead = true;
    this.confirmedAt = OffsetDateTime.now();
  }


}
