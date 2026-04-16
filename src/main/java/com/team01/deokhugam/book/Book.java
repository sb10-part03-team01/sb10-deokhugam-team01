package com.team01.deokhugam.book;

import com.team01.deokhugam.global.entity.BaseRemovableEntity;
import com.team01.deokhugam.global.entity.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends BaseRemovableEntity {
  @Column(name = "title", length = 255, nullable = false)
  private String title;

  @Column(name = "author", length = 100, nullable = false)
  private String author;

  @Column(name = "description", nullable = false)
  @Lob
  private String description;

  @Column(name = "publisher", length = 100, nullable = false)
  private String publisher;

  @Column(name = "published_date" , nullable = false)
  private LocalDate publishedDate;

  @Column(name = "isbn", unique = true, length = 20, nullable = false)
  private String isbn;

  @Column(name = "thumbnail_url", length = 255)
  private String thumbnailUrl;

  @Column(name = "review_count", nullable = false)
  private int reviewCount = 0;

  @Column(name = "rating", nullable = false)
  private double rating = 0.0;

  @Builder
  public Book(String title, String author, String description, String publisher, LocalDate publishedDate, String isbn, String thumbnailUrl) {
    this.title = title;
    this.author = author;
    this.description = description;
    this.publisher = publisher;
    this.publishedDate = publishedDate;
    this.isbn = isbn;
    // reviewCount, rating, isDeleted는 생성 시에는 필요없이 default로 들어가야해서 없음
  }

  public void addThumbnail(String thumbnailUrl){
    this.thumbnailUrl = thumbnailUrl;
  }
}
