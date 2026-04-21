package com.team01.deokhugam.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.team01.deokhugam.book.entity.Book;
import com.team01.deokhugam.book.repository.BookRepository;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.exception.book.BookNotFoundException;
import com.team01.deokhugam.review.dto.CursorPageResponseReviewDto;
import com.team01.deokhugam.review.dto.ReviewCreateRequest;
import com.team01.deokhugam.review.dto.ReviewDto;
import com.team01.deokhugam.review.dto.ReviewSearchCondition;
import com.team01.deokhugam.review.entity.Review;
import com.team01.deokhugam.review.mapper.ReviewMapper;
import com.team01.deokhugam.review.repository.ReviewRepository;
import com.team01.deokhugam.user.entity.User;
import com.team01.deokhugam.user.repository.UserRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class) // Mockito 기반 단위 테스트
class ReviewServiceImplTest {

  @Mock
  private ReviewRepository reviewRepository; // 리뷰 저장/조회 mock 객체

  @Mock
  private BookRepository bookRepository; // 도서 조회 mock 객체

  @Mock
  private UserRepository userRepository; // 유저 조회 mock 객체

  @Mock
  private ReviewMapper reviewMapper; // 엔티티 -> DTO 변환 mock 객체

  @InjectMocks
  private ReviewServiceImpl reviewServiceImpl; // mock 객체들이 주입될 테스트 대상

  private UUID bookId;
  private UUID userId;
  private UUID reviewId;
  private UUID requestUserId;

  @BeforeEach
  void setUp() {
    // 각 테스트에서 사용할 식별자 초기화
    bookId = UUID.randomUUID();
    userId = UUID.randomUUID();
    reviewId = UUID.randomUUID();
    requestUserId = UUID.randomUUID();
  }

  @Test
  @DisplayName("리뷰 생성 - 성공")
  void createReview_success() {
    // given
    ReviewCreateRequest reviewCreateRequest =
        new ReviewCreateRequest(bookId, userId, "테스트 리뷰", 4.5);

    // Book/User 내부 필드는 안 쓰므로 mock 객체로 대체
    Book book = org.mockito.Mockito.mock(Book.class);
    User user = org.mockito.Mockito.mock(User.class);
    Review savedReview = org.mockito.Mockito.mock(Review.class);

    // 서비스가 최종적으로 반환할 DTO
    ReviewDto reviewDto = new ReviewDto(
        reviewId, bookId, "테스트 책", "thumb.jpg", userId, "tester",
        "테스트 리뷰", 4.5, 0, 0, false, OffsetDateTime.now(), null
    );

    // 도서 조회 성공
    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
    // 유저 조회 성공
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    // 중복 리뷰 없음
    given(reviewRepository.existsByBook_IdAndUser_IdAndIsDeletedFalse(bookId, userId))
        .willReturn(false);
    // 저장 성공
    given(reviewRepository.save(any(Review.class))).willReturn(savedReview);
    // DTO 변환 결과
    given(reviewMapper.toDto(savedReview)).willReturn(reviewDto);

    // when
    ReviewDto result = reviewServiceImpl.createReview(reviewCreateRequest);

    // then
    assertThat(result.content()).isEqualTo("테스트 리뷰");
    assertThat(result.rating()).isEqualTo(4.5);
  }

  @Test
  @DisplayName("리뷰 생성 - 존재하지 않는 도서면 예외 발생")
  void createReview_fail_whenBookNotFound() {
    // given
    ReviewCreateRequest reviewCreateRequest =
        new ReviewCreateRequest(bookId, userId, "테스트 리뷰", 4.5);

    // 도서를 찾지 못한 상황
    given(bookRepository.findById(bookId)).willReturn(Optional.empty());

    // when
    Exception exception = assertThrows(
        BookNotFoundException.class,
        () -> reviewServiceImpl.createReview(reviewCreateRequest)
    );

    // then
    assertThat(exception).isInstanceOf(BookNotFoundException.class);
  }

  @Test
  @DisplayName("리뷰 생성 - 존재하지 않는 유저면 예외 발생")
  void createReview_fail_whenUserNotFound() {
    // given
    ReviewCreateRequest reviewCreateRequest =
        new ReviewCreateRequest(bookId, userId, "테스트 리뷰", 4.5);

    // 도서는 존재
    Book book = org.mockito.Mockito.mock(Book.class);
    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));

    // 유저는 존재하지 않음
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when
    Exception exception = assertThrows(
        IllegalArgumentException.class,
        () -> reviewServiceImpl.createReview(reviewCreateRequest)
    );

    // then
    assertThat(exception.getMessage()).isEqualTo("사용자를 찾을 수 없습니다.");
  }

  @Test
  @DisplayName("리뷰 생성 - 같은 유저가 같은 도서에 중복 리뷰 작성 시 예외 발생")
  void createReview_fail_whenDuplicateReview() {
    // given
    ReviewCreateRequest reviewCreateRequest =
        new ReviewCreateRequest(bookId, userId, "테스트 리뷰", 4.5);

    Book book = org.mockito.Mockito.mock(Book.class);
    User user = org.mockito.Mockito.mock(User.class);

    // 도서/유저는 존재
    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
    given(userRepository.findById(userId)).willReturn(Optional.of(user));

    // 이미 같은 리뷰가 존재
    given(reviewRepository.existsByBook_IdAndUser_IdAndIsDeletedFalse(bookId, userId))
        .willReturn(true);

    // when
    Exception exception = assertThrows(
        IllegalArgumentException.class,
        () -> reviewServiceImpl.createReview(reviewCreateRequest)
    );

    // then
    assertThat(exception.getMessage()).isEqualTo("해당 도서에 작성한 리뷰가 있습니다.");
  }

  @Test
  @DisplayName("리뷰 상세 조회 - 성공")
  void getReview_success() {
    // given
    Review review = org.mockito.Mockito.mock(Review.class);

    ReviewDto reviewDto = new ReviewDto(
        reviewId, bookId, "테스트 책", "thumb.jpg", userId, "tester",
        "테스트 리뷰", 4.5, 1, 2, false, OffsetDateTime.now(), null
    );

    // 리뷰 조회 성공
    given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));
    // DTO 변환
    given(reviewMapper.toDto(review)).willReturn(reviewDto);

    // when
    ReviewDto result = reviewServiceImpl.getReview(reviewId, requestUserId);

    // then
    assertThat(result.id()).isEqualTo(reviewId);
    assertThat(result.content()).isEqualTo("테스트 리뷰");
  }

  @Test
  @DisplayName("리뷰 상세 조회 - 리뷰가 없으면 예외 발생")
  void getReview_fail_whenReviewNotFound() {
    // given
    given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.empty());

    // when
    Exception exception = assertThrows(
        IllegalArgumentException.class,
        () -> reviewServiceImpl.getReview(reviewId, requestUserId)
    );

    // then
    assertThat(exception.getMessage()).isEqualTo("리뷰를 찾을 수 없습니다.");
  }

  @Test
  @DisplayName("리뷰 목록 조회 - 성공")
  void searchReviews_success() {
    // given
    Review review = org.mockito.Mockito.mock(Review.class);

    ReviewDto reviewDto = new ReviewDto(
        reviewId, bookId, "테스트 책", "thumb.jpg", userId, "tester",
        "테스트 리뷰", 4.5, 1, 2, false, OffsetDateTime.now(), null
    );

    // 조건에 맞는 리뷰 1개 조회
    given(reviewRepository.findAllByCondition(any(ReviewSearchCondition.class)))
        .willReturn(List.of(review));
    // 전체 개수 1개
    given(reviewRepository.countByCondition(any(ReviewSearchCondition.class))).willReturn(1L);
    // DTO 목록 변환
    given(reviewMapper.toDtoList(any())).willReturn(List.of(reviewDto));

    // when
    CursorPageResponseReviewDto result = reviewServiceImpl.searchReviews(
        requestUserId,
        userId,
        bookId,
        "테스트",
        "createdAt",
        SortDirection.DESC,
        null,
        null,
        10
    );

    // then
    assertThat(result.content()).hasSize(1);
    assertThat(result.totalElements()).isEqualTo(1);
  }

  @Test
  @DisplayName("리뷰 목록 조회 - 조건에 맞는 리뷰가 없으면 빈 목록 반환")
  void searchReviews_returnsEmpty_whenNoMatchedResult() {
    // given
    given(reviewRepository.findAllByCondition(any(ReviewSearchCondition.class)))
        .willReturn(List.of());
    given(reviewRepository.countByCondition(any(ReviewSearchCondition.class))).willReturn(0L);
    given(reviewMapper.toDtoList(any())).willReturn(List.of());

    // when
    CursorPageResponseReviewDto result = reviewServiceImpl.searchReviews(
        requestUserId,
        userId,
        bookId,
        "없는키워드",
        "createdAt",
        SortDirection.DESC,
        null,
        null,
        10
    );

    // then
    assertThat(result.content()).isEmpty();
    assertThat(result.totalElements()).isZero();
  }

  @Test
  @DisplayName("리뷰 목록 조회 - orderBy가 createdAt 또는 rating이 아니면 예외 발생")
  void searchReviews_fail_whenInvalidOrderBy() {
    // when
    Exception exception = assertThrows(
        IllegalArgumentException.class,
        () -> reviewServiceImpl.searchReviews(
            requestUserId,
            userId,
            bookId,
            "테스트",
            "invalid",
            SortDirection.DESC,
            null,
            null,
            10
        )
    );

    // then
    assertThat(exception.getMessage()).isEqualTo("orderBy는 createdAt 또는 rating만 가능합니다.");
  }

  @Test
  @DisplayName("리뷰 목록 조회 - after와 cursor 중 하나만 있으면 예외 발생")
  void searchReviews_fail_whenAfterAndCursorMismatch() {
    // given
    // 현재 구현에서는 repository에서 after/cursor 동시 전달 여부를 검사함
    given(reviewRepository.findAllByCondition(any(ReviewSearchCondition.class)))
        .willThrow(new IllegalArgumentException("after와 cursor는 같이 전달 되어야 합니다."));

    // when
    Exception exception = assertThrows(
        IllegalArgumentException.class,
        () -> reviewServiceImpl.searchReviews(
            requestUserId,
            userId,
            bookId,
            "테스트",
            "createdAt",
            SortDirection.DESC,
            null,
            OffsetDateTime.now(),
            10
        )
    );

    // then
    assertThat(exception.getMessage()).isEqualTo("after와 cursor는 같이 전달 되어야 합니다.");
  }
}
