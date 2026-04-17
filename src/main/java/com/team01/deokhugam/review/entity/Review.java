package com.team01.deokhugam.review.entity;

import com.team01.deokhugam.book.Book;
import com.team01.deokhugam.global.entity.BaseRemovableEntity;
import com.team01.deokhugam.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

@Getter
@Entity
@Table(
    name = "reviews",
    // 같은 사용자가 같은 도서에 리뷰 2개 못 쓰게 막는 제약
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_review_book_user",
            columnNames = {"book_id", "user_id"}
        )
    },
    indexes = {
        // 조회 성능용 인덱스
        @Index(name = "idx_reviews_book_id", columnList = "book_id"),
        @Index(name = "idx_reviews_user_id", columnList = "user_id"),
        @Index(name = "idx_reviews_created_at", columnList = "created_at"),
        @Index(name = "idx_reviews_rating_created_at", columnList = "rating, created_at")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Where(clause = "is_deleted = false")
public class Review extends BaseRemovableEntity {

  // 리뷰가 달린 도서
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "book_id", nullable = false)
  private Book book;

  // 리뷰 작성자
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // 리뷰 내용
  @Column(name = "content", nullable = false, length = 1000)
  private String content;

  // 평점
  @Column(name = "rating", nullable = false)
  private double rating;

  // 좋아요 수
  @Column(name = "like_count", nullable = false)
  private int likeCount;

  // 댓글 수
  @Column(name = "comment_count", nullable = false)
  private int commentCount;

  // 낙관적 락
  @Version
  @Column(name = "version", nullable = false)
  private Long version;

  public Review(Book book, User user, String content, double rating) {
    validateBook(book);
    validateUser(user);
    validateContent(content);
    validateRating(rating);

    this.book = book;
    this.user = user;
    this.content = content.trim();
    this.rating = rating;
    this.likeCount = 0;
    this.commentCount = 0;
  }

  // 리뷰 수정
  public void update(String content, double rating) {
    validateContent(content);
    validateRating(rating);

    this.content = content.trim();
    this.rating = rating;
  }

  // 좋아요 수 증가
  public void increaseLikeCount() {
    this.likeCount++;
  }

  // 좋아요 수 감소
  public void decreaseLikeCount() {
    if (this.likeCount > 0) {
      this.likeCount--;
    }
  }

  // 댓글 수 증가
  public void increaseCommentCount() {
    this.commentCount++;
  }

  // 댓글 수 감소
  public void decreaseCommentCount() {
    if (this.commentCount > 0) {
      this.commentCount--;
    }
  }

  private void validateBook(Book book) {
    Objects.requireNonNull(book, "도서 정보는 null일 수 없습니다.");
  }

  private void validateUser(User user) {
    Objects.requireNonNull(user, "사용자 정보는 null일 수 없습니다.");
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

  private void validateRating(double rating) {
    if (rating < 1.0 || rating > 5.0) {
      throw new IllegalArgumentException("평점은 1.0 이상 5.0 이하여야 합니다.");
    }
  }
}