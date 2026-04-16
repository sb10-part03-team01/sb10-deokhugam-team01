package com.team01.deokhugam.review.entity;

import com.team01.deokhugam.global.entity.BaseRemovableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "reviews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseRemovableEntity {

  @Column(name = "book_id", nullable = false)
  private UUID bookId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "content", nullable = false, length = 1000)
  private String content;

  @Column(name = "rating", nullable = false)
  private Double rating;

  @Column(name = "like_count", nullable = false)
  private long likeCount;

  @Column(name = "comment_count", nullable = false)
  private long commentCount;

  @Version
  @Column(name = "version", nullable = false)
  private Long version;

  public Review(UUID bookId, UUID userId, String content, Double rating) {
    validateBookId(bookId);
    validateUserId(userId);
    validateContent(content);
    validateRating(rating);

    this.bookId = bookId;
    this.userId = userId;
    this.content = content.trim();
    this.rating = rating;
    this.likeCount = 0L;
    this.commentCount = 0L;
  }

  public void update(String content, Double rating) {
    validateContent(content);
    validateRating(rating);

    this.content = content.trim();
    this.rating = rating;
  }

  public void increaseLikeCount() {
    this.likeCount++;
  }

  public void decreaseLikeCount() {
    if (this.likeCount > 0) {
      this.likeCount--;
    }
  }

  public void increaseCommentCount() {
    this.commentCount++;
  }

  public void decreaseCommentCount() {
    if (this.commentCount > 0) {
      this.commentCount--;
    }
  }

  private void validateBookId(UUID bookId) {
    Objects.requireNonNull(bookId, "도서 ID는 null일 수 없습니다.");
  }

  private void validateUserId(UUID userId) {
    Objects.requireNonNull(userId, "사용자 ID는 null일 수 없습니다.");
  }

  private void validateContent(String content) {
    if (content == null) {
      throw new IllegalArgumentException("리뷰 내용은 null일 수 없습니다.");
    }

    String trimmedContent = content.trim();

    if (trimmedContent.isEmpty()) {
      throw new IllegalArgumentException("리뷰 내용은 비어 있을 수 없습니다.");
    }

    if (trimmedContent.length() > 1000) {
      throw new IllegalArgumentException("리뷰는 1000자를 초과할 수 없습니다.");
    }
  }

  private void validateRating(Double rating) {
    Objects.requireNonNull(rating, "평점은 null일 수 없습니다.");
    if (rating < 1.0 || rating > 5.0) {
      throw new IllegalArgumentException("평점은 1.0 이상 5.0 이하여야 합니다.");
    }
  }
}
