package com.team01.deokhugam.dashboard.entity;

import com.team01.deokhugam.book.entity.Book;
import com.team01.deokhugam.global.entity.BaseEntity;
import com.team01.deokhugam.global.enums.RankingPeriod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "popular_books",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_popular_books_period_rank",
          columnNames = {"period_type", "calculated_date", "rank"}),
      @UniqueConstraint(
          name = "uk_popular_books_period_book",
          columnNames = {"period_type", "calculated_date", "book_id"})
    },
    indexes = {@Index(name = "idx_popular_books_book_id", columnList = "book_id")})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularBook extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "book_id", nullable = false)
  private Book book;

  @Enumerated(EnumType.STRING)
  @Column(name = "period_type", length = 20, nullable = false)
  private RankingPeriod periodType;

  @Column(name = "calculated_date", nullable = false)
  private LocalDate calculatedDate;

  @Column(name = "rank", nullable = false)
  private int rank;

  @Column(name = "score", nullable = false)
  private double score;

  @Column(name = "rating", nullable = false)
  private double rating;

  @Column(name = "review_count", nullable = false)
  private int reviewCount;

  public PopularBook(
      Book book,
      RankingPeriod periodType,
      LocalDate calculatedDate,
      int rank,
      double score,
      double rating,
      int reviewCount) {
    this.book = book;
    this.periodType = periodType;
    this.calculatedDate = calculatedDate;
    this.rank = rank;
    this.score = score;
    this.rating = rating;
    this.reviewCount = reviewCount;
  }
}
