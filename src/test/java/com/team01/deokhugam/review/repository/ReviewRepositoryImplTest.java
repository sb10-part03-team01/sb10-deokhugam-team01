package com.team01.deokhugam.review.repository;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(ReviewRepositoryImpl.class)
class ReviewRepositoryImplTest {

  @Autowired
  private ReviewRepository reviewRepository;

  @Autowired
  private TestEntityManager em;

  private UUID userId1;
  private UUID userId2;
  private UUID bookId1;
  private UUID bookId2;

  @BeforeEach
  void setUp() {
    userId1 = UUID.randomUUID();
    userId2 = UUID.randomUUID();
    bookId1 = UUID.randomUUID();
    bookId2 = UUID.randomUUID();

    // 테스트용 데이터 저장
  }

  @Test
  @DisplayName("리뷰 목록 조회 - createdAt 기준 내림차순 정렬 성공")
  void findAllByCondition_orderByCreatedAtDesc_success() {
  }

  @Test
  @DisplayName("리뷰 목록 조회 - rating 기준 내림차순 정렬 성공")
  void findAllByCondition_orderByRatingDesc_success() {
  }

  @Test
  @DisplayName("리뷰 목록 조회 - userId 조건으로 필터링 성공")
  void findAllByCondition_filterByUserId_success() {
  }

  @Test
  @DisplayName("리뷰 목록 조회 - bookId 조건으로 필터링 성공")
  void findAllByCondition_filterByBookId_success() {
  }

  @Test
  @DisplayName("리뷰 목록 조회 - keyword 조건으로 필터링 성공")
  void findAllByCondition_filterByKeyword_success() {
  }

  @Test
  @DisplayName("리뷰 목록 조회 - after와 cursor로 다음 페이지 조회 성공")
  void findAllByCondition_cursorPaging_success() {
  }

  @Test
  @DisplayName("리뷰 목록 조회 - after만 전달되면 예외 발생")
  void findAllByCondition_fail_whenAfterOnly() {
  }

  @Test
  @DisplayName("리뷰 목록 조회 - cursor만 전달되면 예외 발생")
  void findAllByCondition_fail_whenCursorOnly() {
  }

  @Test
  @DisplayName("리뷰 개수 조회 - 조건에 맞는 개수 반환 성공")
  void countByCondition_success() {
  }
}
