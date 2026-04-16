package com.team01.deokhugam.review.entity;

import com.team01.deokhugam.global.entity.BaseRemovableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "reviews",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_review_book_user",
            columnNames = {"book_id", "user_id"}
        )
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseRemovableEntity {

  //도서 ID
  @Column(name = "book_id", nullable = false)
  private UUID bookId;

  // 작성자 ID
  @Column(name = "user_id", nullable = false)
  private UUID userId;

  // 리뷰 내용
  @Column(name = "content", nullable = false, length = 1000)
  private String content;

  // 평점(1.0~5.0)
  @Column(name = "rating", nullable = false)
  private Double rating;

  // 좋아요 수
  @Column(name = "like_count", nullable = false)
  private long likeCount;

  // 댓글 수
  @Column(name = "comment_count", nullable = false)
  private long commentCount;

  public Review(UUID bookId, UUID userId, String content, Double rating) {
    this.bookId = bookId;
    this.userId = userId;
    this.content = content;
    this.rating = rating;
    this.likeCount = 0L;
    this.commentCount = 0L;
  }

  // 리뷰 내용/평점 수정
  public void update(String content, Double rating) {
    this.content = content;
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


}
