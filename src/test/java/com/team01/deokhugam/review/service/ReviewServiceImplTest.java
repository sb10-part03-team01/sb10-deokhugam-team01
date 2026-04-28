package com.team01.deokhugam.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.team01.deokhugam.book.entity.Book;
import com.team01.deokhugam.book.repository.BookRepository;
import com.team01.deokhugam.book.service.BookService;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.exception.ErrorCode;
import com.team01.deokhugam.global.exception.book.BookNotFoundException;
import com.team01.deokhugam.global.exception.review.ReviewAlreadyExistsException;
import com.team01.deokhugam.global.exception.review.ReviewNotFoundException;
import com.team01.deokhugam.global.exception.review.ReviewNotSoftDeletedException;
import com.team01.deokhugam.global.exception.review.ReviewUpdateForbiddenException;
import com.team01.deokhugam.global.exception.user.UserNotFoundException;
import com.team01.deokhugam.review.dto.CursorPageResponseReviewDto;
import com.team01.deokhugam.review.dto.ReviewCreateRequest;
import com.team01.deokhugam.review.dto.ReviewDto;
import com.team01.deokhugam.review.dto.ReviewLikeDto;
import com.team01.deokhugam.review.dto.ReviewSearchCondition;
import com.team01.deokhugam.review.dto.ReviewUpdateRequest;
import com.team01.deokhugam.review.entity.Review;
import com.team01.deokhugam.review.entity.ReviewLike;
import com.team01.deokhugam.review.mapper.ReviewMapper;
import com.team01.deokhugam.review.repository.ReviewLikeRepository;
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

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private BookRepository bookRepository;

  @Mock
  private BookService bookService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ReviewMapper reviewMapper;

  @Mock
  private ReviewLikeRepository reviewLikeRepository;

  @InjectMocks
  private ReviewServiceImpl reviewServiceImpl;

  private UUID bookId;
  private UUID userId;
  private UUID reviewId;
  private UUID requestUserId;
  private UUID requestBookId;
  private UUID authorId;

  @BeforeEach
  void setUp() {
    bookId = UUID.randomUUID();
    userId = UUID.randomUUID();
    reviewId = UUID.randomUUID();
    requestUserId = UUID.randomUUID();
    requestBookId = UUID.randomUUID();
    authorId = UUID.randomUUID();
  }

  @Test
  @DisplayName("리뷰 생성 - 성공")
  void createReview_success() {
    ReviewCreateRequest reviewCreateRequest =
        new ReviewCreateRequest(bookId, userId, "테스트 리뷰", 4.5);

    Book book = mock(Book.class);
    User user = mock(User.class);
    Review savedReview = mock(Review.class);

    ReviewDto reviewDto = new ReviewDto(
        reviewId, bookId, "테스트 책", "thumb.jpg", userId, "tester",
        "테스트 리뷰", 4.5, 0, 0, false, OffsetDateTime.now(), null
    );

    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(reviewRepository.existsByBookIdAndUserIdAndIsDeletedFalse(bookId, userId))
        .willReturn(false);
    given(reviewRepository.save(any(Review.class))).willReturn(savedReview);
    given(reviewMapper.toDto(savedReview)).willReturn(reviewDto);

    ReviewDto result = reviewServiceImpl.createReview(reviewCreateRequest);

    assertThat(result.content()).isEqualTo("테스트 리뷰");
    assertThat(result.rating()).isEqualTo(4.5);
  }

  @Test
  @DisplayName("리뷰 생성 - 존재하지 않는 도서면 예외 발생")
  void createReview_fail_whenBookNotFound() {
    ReviewCreateRequest reviewCreateRequest =
        new ReviewCreateRequest(bookId, userId, "테스트 리뷰", 4.5);

    given(bookRepository.findById(bookId)).willReturn(Optional.empty());

    Exception exception = assertThrows(
        BookNotFoundException.class,
        () -> reviewServiceImpl.createReview(reviewCreateRequest)
    );

    assertThat(exception).isInstanceOf(BookNotFoundException.class);
  }

  @Test
  @DisplayName("리뷰 생성 - 존재하지 않는 유저면 예외 발생")
  void createReview_fail_whenUserNotFound() {
    ReviewCreateRequest reviewCreateRequest =
        new ReviewCreateRequest(bookId, userId, "테스트 리뷰", 4.5);

    Book book = mock(Book.class);
    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> reviewServiceImpl.createReview(reviewCreateRequest))
        .isInstanceOf(UserNotFoundException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.USER_NOT_FOUND);
  }


  @Test
  @DisplayName("리뷰 생성 - 같은 유저가 같은 도서에 중복 리뷰 작성 시 예외 발생")
  void createReview_fail_whenDuplicateReview() {
    ReviewCreateRequest reviewCreateRequest =
        new ReviewCreateRequest(bookId, userId, "테스트 리뷰", 4.5);

    Book book = mock(Book.class);
    User user = mock(User.class);

    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(reviewRepository.existsByBookIdAndUserIdAndIsDeletedFalse(bookId, userId))
        .willReturn(true);

    assertThatThrownBy(() -> reviewServiceImpl.createReview(reviewCreateRequest))
        .isInstanceOf(ReviewAlreadyExistsException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.REVIEW_ALREADY_EXISTS);
  }

  @Test
  @DisplayName("리뷰 상세 조회 - 성공")
  void getReview_success() {
    Review review = mock(Review.class);

    ReviewDto reviewDto = new ReviewDto(
        reviewId, bookId, "테스트 책", "thumb.jpg", userId, "tester",
        "테스트 리뷰", 4.5, 1, 2, false, OffsetDateTime.now(), null
    );

    given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));
    given(reviewMapper.toDto(review)).willReturn(reviewDto);

    ReviewDto result = reviewServiceImpl.getReview(reviewId, requestUserId);

    assertThat(result.id()).isEqualTo(reviewId);
    assertThat(result.content()).isEqualTo("테스트 리뷰");
  }

  @Test
  @DisplayName("리뷰 상세 조회 - 리뷰가 없으면 예외 발생")
  void getReview_fail_whenReviewNotFound() {
    given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> reviewServiceImpl.getReview(reviewId, requestUserId))
        .isInstanceOf(ReviewNotFoundException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
  }

  @Test
  @DisplayName("리뷰 목록 조회 - 성공")
  void searchReviews_success() {
    Review review = mock(Review.class);

    ReviewDto reviewDto = new ReviewDto(
        reviewId, bookId, "테스트 책", "thumb.jpg", userId, "tester",
        "테스트 리뷰", 4.5, 1, 2, false, OffsetDateTime.now(), null
    );

    given(reviewRepository.findAllByCondition(any(ReviewSearchCondition.class)))
        .willReturn(List.of(review));
    given(reviewRepository.countByCondition(any(ReviewSearchCondition.class))).willReturn(1L);
    given(reviewMapper.toDto(review)).willReturn(reviewDto);
    given(review.getId()).willReturn(reviewId);
    given(reviewLikeRepository.findLikedReviewIdsByReviewIdInAndUserId(List.of(reviewId),
        requestUserId))
        .willReturn(List.of());

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

    assertThat(result.content()).hasSize(1);
    assertThat(result.totalElements()).isEqualTo(1);
    assertThat(result.content().get(0).likedByMe()).isFalse();
  }

  @Test
  @DisplayName("리뷰 목록 조회 - 조건에 맞는 리뷰가 없으면 빈 목록 반환")
  void searchReviews_returnsEmpty_whenNoMatchedResult() {
    given(reviewRepository.findAllByCondition(any(ReviewSearchCondition.class)))
        .willReturn(List.of());
    given(reviewRepository.countByCondition(any(ReviewSearchCondition.class)))
        .willReturn(0L);

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

    assertThat(result.content()).isEmpty();
    assertThat(result.totalElements()).isZero();
  }

  @Test
  @DisplayName("리뷰 목록 조회 - orderBy가 createdAt 또는 rating이 아니면 예외 발생")
  void searchReviews_fail_whenInvalidOrderBy() {
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

    assertThat(exception.getMessage()).isEqualTo("orderBy는 createdAt 또는 rating만 가능합니다.");
  }

  @Test
  @DisplayName("리뷰 수정 - 성공")
  void updateReview_success() {
    User user = mock(User.class);
    Book book = mock(Book.class);
    Review review = mock(Review.class);
    ReviewUpdateRequest request = new ReviewUpdateRequest("수정된 리뷰", 4.5);
    ReviewDto reviewDto = new ReviewDto(
        reviewId,
        requestBookId,
        "테스트 책",
        "thumb.jpg",
        requestUserId,
        "테스트 유저",
        "수정된 리뷰",
        4.5,
        0,
        0,
        false,
        OffsetDateTime.now(),
        OffsetDateTime.now()
    );

    given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));
    given(review.getUser()).willReturn(user);
    given(user.getId()).willReturn(requestUserId);
    given(review.getBook()).willReturn(book);
    given(book.getId()).willReturn(requestBookId);
    given(reviewMapper.toDto(review)).willReturn(reviewDto);

    ReviewDto result = reviewServiceImpl.updateReview(reviewId, requestUserId, request);

    verify(review).update("수정된 리뷰", 4.5);
    assertThat(result.content()).isEqualTo("수정된 리뷰");
    assertThat(result.rating()).isEqualTo(4.5);
  }

  @Test
  @DisplayName("리뷰 수정 - 권한 없는 사용자가 수정 시 예외 발생")
  void updateReview_fail_whenRequesterIsNotAuthor() {
    User author = mock(User.class);
    Review review = mock(Review.class);
    ReviewUpdateRequest request = new ReviewUpdateRequest("수정 시도", 4.5);

    given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));
    given(review.getUser()).willReturn(author);
    given(author.getId()).willReturn(authorId);

    assertThatThrownBy(() -> reviewServiceImpl.updateReview(reviewId, requestUserId, request))
        .isInstanceOf(ReviewUpdateForbiddenException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.REVIEW_UPDATE_FORBIDDEN);

    verify(review, never()).update(anyString(), anyDouble());
  }

  @Test
  @DisplayName("리뷰 논리 삭제 - 성공")
  void deleteReview_success() {
    User author = mock(User.class);
    Book book = mock(Book.class);
    Review review = mock(Review.class);

    given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));
    given(review.getUser()).willReturn(author);
    given(review.getBook()).willReturn(book);
    given(book.getId()).willReturn(requestBookId);
    given(author.getId()).willReturn(requestUserId);

    reviewServiceImpl.deleteReview(reviewId, requestUserId);

    verify(review).softDelete();
  }

  @Test
  @DisplayName("리뷰 논리 삭제 - 리뷰가 없으면 예외 발생")
  void deleteReview_fail_whenReviewNotFound() {
    given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> reviewServiceImpl.deleteReview(reviewId, requestUserId))
        .isInstanceOf(ReviewNotFoundException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
  }

  @Test
  @DisplayName("리뷰 논리 삭제 - 권한 없는 사용자가 삭제 시 예외 발생")
  void deleteReview_fail_whenRequesterIsNotAuthor() {
    User author = mock(User.class);
    Review review = mock(Review.class);

    given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));
    given(review.getUser()).willReturn(author);
    given(author.getId()).willReturn(authorId);

    assertThatThrownBy(() -> reviewServiceImpl.deleteReview(reviewId, requestUserId))
        .isInstanceOf(ReviewUpdateForbiddenException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.REVIEW_UPDATE_FORBIDDEN);

    verify(review, never()).softDelete();
  }

  @Test
  @DisplayName("리뷰 물리 삭제 - 성공")
  void hardDeleteReview_success() {
    User author = mock(User.class);
    Review review = mock(Review.class);

    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
    given(review.getUser()).willReturn(author);
    given(author.getId()).willReturn(requestUserId);
    given(review.isDeleted()).willReturn(true);

    reviewServiceImpl.hardDeleteReview(reviewId, requestUserId);

    verify(reviewRepository).delete(review);
  }

  @Test
  @DisplayName("리뷰 물리 삭제 - 리뷰가 없으면 예외 발생")
  void hardDeleteReview_fail_whenReviewNotFound() {
    given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> reviewServiceImpl.hardDeleteReview(reviewId, requestUserId))
        .isInstanceOf(ReviewNotFoundException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
  }

  @Test
  @DisplayName("리뷰 물리 삭제 - 권한 없는 사용자가 삭제 시 예외 발생")
  void hardDeleteReview_fail_whenRequesterIsNotAuthor() {
    User author = mock(User.class);
    Review review = mock(Review.class);

    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
    given(review.getUser()).willReturn(author);
    given(author.getId()).willReturn(authorId);

    assertThatThrownBy(() -> reviewServiceImpl.hardDeleteReview(reviewId, requestUserId))
        .isInstanceOf(ReviewUpdateForbiddenException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.REVIEW_UPDATE_FORBIDDEN);

    verify(reviewRepository, never()).delete(any(Review.class));
  }

  @Test
  @DisplayName("리뷰 물리 삭제 - 논리 삭제되지 않은 리뷰면 예외 발생")
  void hardDeleteReview_fail_whenReviewIsNotSoftDeleted() {
    User author = mock(User.class);
    Review review = mock(Review.class);

    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
    given(review.getUser()).willReturn(author);
    given(author.getId()).willReturn(requestUserId);
    given(review.isDeleted()).willReturn(false);

    assertThatThrownBy(() -> reviewServiceImpl.hardDeleteReview(reviewId, requestUserId))
        .isInstanceOf(ReviewNotSoftDeletedException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.REVIEW_NOT_SOFT_DELETED);

    verify(reviewRepository, never()).delete(any(Review.class));
  }

  @Test
  @DisplayName("리뷰 좋아요 - 좋아요 추가 정상 반영")
  void reviewLike_increase_success() {
    // given
    Review review = mock(Review.class);
    User user = mock(User.class);

    given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));
    given(userRepository.findById(requestUserId)).willReturn(Optional.of(user));
    given(reviewLikeRepository.findByReviewIdAndUserId(reviewId, requestUserId))
        .willReturn(Optional.empty());

    //when
    ReviewLikeDto result = reviewServiceImpl.toggleLike(reviewId, requestUserId);

    // then
    verify(reviewLikeRepository).save(any(ReviewLike.class));
    verify(review).increaseLikeCount();
    verify(review, never()).decreaseLikeCount();
    assertThat(result.liked()).isTrue();
    assertThat(result.reviewId()).isEqualTo(reviewId);
    assertThat(result.userId()).isEqualTo(requestUserId);
  }

  @Test
  @DisplayName("리뷰 좋아요 - 좋아요 감소 정상 반영")
  void reviewLike_decrease_success() {
    // given
    Review review = mock(Review.class);
    User user = mock(User.class);
    ReviewLike reviewLike = mock(ReviewLike.class);

    given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));
    given(userRepository.findById(requestUserId)).willReturn(Optional.of(user));
    given(reviewLikeRepository.findByReviewIdAndUserId(reviewId, requestUserId))
        .willReturn(Optional.of(reviewLike));

    //when
    ReviewLikeDto result = reviewServiceImpl.toggleLike(reviewId, requestUserId);

    // then
    verify(reviewLikeRepository).delete(reviewLike);
    verify(review).decreaseLikeCount();
    verify(review, never()).increaseLikeCount();
    assertThat(result.liked()).isFalse();
    assertThat(result.reviewId()).isEqualTo(reviewId);
    assertThat(result.userId()).isEqualTo(requestUserId);
  }

  @Test
  @DisplayName("리뷰 좋아요 - 논리삭제 되었거나 리뷰가 없을 시 예외 발생")
  void reviewLike_fail_whenReviewNotFound() {
    // given
    given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> reviewServiceImpl.toggleLike(reviewId, requestUserId))
        .isInstanceOf(ReviewNotFoundException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
  }

  @Test
  @DisplayName("리뷰 좋아요 - 사용자가 없을 시 예외 발생")
  void reviewLike_fail_whenUserIsNotFound() {
    // given
    Review review = mock(Review.class);

    given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));
    given(userRepository.findById(requestUserId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> reviewServiceImpl.toggleLike(reviewId, requestUserId))
        .isInstanceOf(UserNotFoundException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.USER_NOT_FOUND);
  }
}
