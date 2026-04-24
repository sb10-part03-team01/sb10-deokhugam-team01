package com.team01.deokhugam.review.entity;


import com.team01.deokhugam.global.entity.BaseEntity;
import com.team01.deokhugam.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "review_likes",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_review_like_review_user", columnNames = {"review_id",
            "user_id"})
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewLike extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "review_id", nullable = false)
  private Review review;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  public ReviewLike(Review review, User user) {
    this.review = review;
    this.user = user;
  }
}
