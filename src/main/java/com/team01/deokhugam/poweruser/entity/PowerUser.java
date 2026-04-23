package com.team01.deokhugam.poweruser.entity;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.global.entity.BaseEntity;
import com.team01.deokhugam.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "power_users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PowerUser extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "period_type", nullable = false)
  private DashboardPeriod period;

  @Column(name = "calculated_date", nullable = false)
  private OffsetDateTime calculatedDate;

  @Column(nullable = false)
  private int rank;

  @Column(nullable = false)
  private double score;

  @Column(nullable = false)
  private double reviewScoreSum;

  @Column(nullable = false)
  private long likeCount;

  @Column(nullable = false)
  private long commentCount;

  @Builder
  public PowerUser(User user, DashboardPeriod period, OffsetDateTime calculatedDate, int rank,
      double score, double reviewScoreSum, long likeCount, long commentCount) {
    this.user = user;
    this.period = period;
    this.calculatedDate = calculatedDate;
    this.rank = rank;
    this.score = score;
    this.reviewScoreSum = reviewScoreSum;
    this.likeCount = likeCount;
    this.commentCount = commentCount;
  }
}
