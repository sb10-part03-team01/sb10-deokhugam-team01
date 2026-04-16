package com.team01.deokhugam.comment.entity;

import com.team01.deokhugam.global.entity.BaseRemovableEntity;
import com.team01.deokhugam.review.entity.Review;
import com.team01.deokhugam.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

@Getter
@Entity
@Table(
    name = "comments",
    indexes = {
      @Index(name = "idx_comment_review_created_at_id", columnList = "review_id, created_at, id"),
      @Index(name = "idx_comments_user_id", columnList = "user_id")
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Where(clause = "is_deleted = false")
public class Comment extends BaseRemovableEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "review_id", nullable = false)
  private Review review;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "content", length = 500, nullable = false)
  private String content;

  public Comment(Review review, User user, String content) {
    this.review = review;
    this.user = user;
    this.content = content;
  }

  public void updateContent(String content) {
    this.content = content;
  }
}
