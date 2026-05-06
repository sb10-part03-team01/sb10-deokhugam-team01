package com.team01.deokhugam.review.service;

import com.team01.deokhugam.book.entity.Book;
import com.team01.deokhugam.book.repository.BookRepository;
import com.team01.deokhugam.book.service.BookService;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import com.team01.deokhugam.global.pagination.CursorPaginationUtils;
import com.team01.deokhugam.notification.dto.NotificationCreateRequest;
import com.team01.deokhugam.notification.service.NotificationService;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

  private final ReviewRepository reviewRepository;
  private final ReviewLikeRepository reviewLikeRepository;
  private final BookRepository bookRepository;
  private final UserRepository userRepository;
  private final ReviewMapper reviewMapper;
  private final BookService bookService;
  private final NotificationService notificationService;
  private final PlatformTransactionManager transactionManager;

  @Override
  @Transactional
  public ReviewDto createReview(ReviewCreateRequest request) {
    //도서 확인
    Book book = bookRepository.findById(request.bookId())
        .orElseThrow(() -> new DeokhugamException(
            ErrorCode.BOOK_NOT_FOUND,
            Map.of("bookId", request.bookId())
        ));

    //사용자 확인
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new DeokhugamException(ErrorCode.USER_NOT_FOUND,
            Map.of("userId", request.userId())
        ));

    // 리뷰 중복확인
    if (reviewRepository.existsByBookIdAndUserIdAndIsDeletedFalse(request.bookId(),
        request.userId())) {
      log.warn("리뷰 등록 실패 - 중복 리뷰 존재: bookId={}, userId={} ", request.bookId(), request.userId());
      throw new DeokhugamException(
          ErrorCode.REVIEW_ALREADY_EXISTS,
          Map.of(
              "bookId", request.bookId(),
              "userId", request.userId()
          ));

    }

    // 리뷰 엔티티 생성
    Review review = new Review(
        book,
        user,
        request.content(),
        request.rating()
    );
    Review savedReview = reviewRepository.save(review);

    bookService.plusBookReviewRating(book.getId(), review.getRating());

    return reviewMapper.toDto(savedReview);
  }

  @Override
  public ReviewDto getReview(UUID reviewId, UUID requestUserId) {
    Review review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
        .orElseThrow(() -> new DeokhugamException(
            ErrorCode.REVIEW_NOT_FOUND,
            Map.of("reviewId", reviewId
            )));

    boolean likedByMe = reviewLikeRepository.existsByReviewIdAndUserId(reviewId, requestUserId);
    return reviewMapper.toDto(review).withLikedByMe(likedByMe);
  }

  @Override
  public CursorPageResponseReviewDto searchReviews(
      UUID requestUserId,
      UUID userId,
      UUID bookId,
      String keyword,
      String orderBy,
      SortDirection direction,
      String cursor,
      OffsetDateTime after,
      Integer limit
  ) {
    ReviewSearchCondition condition = new ReviewSearchCondition(
        userId,
        bookId,
        keyword,
        orderBy,
        direction,
        cursor,
        after,
        limit
    );

    List<Review> reviews = reviewRepository.findAllByCondition(condition);
    long totalElements = reviewRepository.countByCondition(condition);

    List<UUID> reviewIds = reviews.stream()
        .map(Review::getId)
        .toList();

    Set<UUID> likedReviewIds = new HashSet<>(
        reviewLikeRepository.findLikedReviewIdsByReviewIdInAndUserId(reviewIds, requestUserId)
    );

    List<ReviewDto> content = reviews.stream()
        .map(review -> reviewMapper.toDto(review)
            .withLikedByMe(likedReviewIds.contains(review.getId())))
        .toList();

    CursorPageResponse<ReviewDto> page = CursorPaginationUtils.of(
        content,
        condition.normalizedLimit(),
        totalElements,
        reviewDto -> {
          if ("rating".equalsIgnoreCase(condition.orderBy())) {
            return reviewDto.rating() + "|" + reviewDto.id();
          }
          return reviewDto.id().toString();
        },
        ReviewDto::createdAt
    );

    return new CursorPageResponseReviewDto(
        page.content(),
        page.nextCursor(),
        page.nextAfter(),
        page.size(),
        page.totalElements(),
        page.hasNext()
    );
  }

  @Override
  @Transactional
  public ReviewDto updateReview(UUID reviewId, UUID requestUserId, ReviewUpdateRequest request) {

    // 리뷰  존재 검증
    Review review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
        .orElseThrow(
            () -> new DeokhugamException(ErrorCode.REVIEW_NOT_FOUND, Map.of("reviewId", reviewId)));

    double oldRating = review.getRating();
    double newRating = request.rating();
    // 사용자가 쓴 리뷰인지 검증(NPE)
    if (!review.getUser().getId().equals(requestUserId)) {
      log.warn("리뷰 수정 실패 - 권한 없음: reviewId={}, requestUserId={}, authorUserId={}", reviewId,
          requestUserId, review.getUser().getId());
      throw new DeokhugamException(ErrorCode.REVIEW_UPDATE_FORBIDDEN,
          Map.of(
              "reviewId", reviewId,
              "requestUserId", requestUserId
          ));
    }

    // 리뷰 수정
    review.update(request.content(), newRating);

    // 평균 평점 수정
    bookService.modifyBookReviewRating(review.getBook().getId(), oldRating, newRating);

    return reviewMapper.toDto(review);
  }

  @Override
  @Transactional
  public void deleteReview(UUID reviewId, UUID requestUserId) {
    // 리뷰  존재 검증
    Review review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
        .orElseThrow(() -> new DeokhugamException(ErrorCode.REVIEW_NOT_FOUND,
            Map.of(
                "reviewId", reviewId
            )));

    // 사용자가 쓴 리뷰인지 검증
    if (!review.getUser().getId().equals(requestUserId)) {
      log.warn("리뷰 논리 삭제 실패 - 권한 없음: reviewId={}, requestUserId={}, authorUserId={}", reviewId,
          requestUserId, review.getUser().getId());
      throw new DeokhugamException(ErrorCode.REVIEW_UPDATE_FORBIDDEN,
          Map.of(
              "reviewId", reviewId,
              "requestUserId", requestUserId
          ));
    }

    // 평균 평점 수정
    bookService.minusBookReviewRating(review.getBook().getId(), review.getRating());

    // 리뷰 논리 삭제
    review.softDelete();
  }

  @Override
  @Transactional
  public void hardDeleteReview(UUID reviewId, UUID requestUserId) {
    // 리뷰 존재 검증
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new DeokhugamException(ErrorCode.REVIEW_NOT_FOUND,
            Map.of(
                "reviewId", reviewId
            )));

    // 사용자가 쓴 리뷰인지 검증
    if (!review.getUser().getId().equals(requestUserId)) {
      log.warn("리뷰 물리 삭제 실패 - 권한 없음: reviewId={}, requestUserId={}, authorUserId={}", reviewId,
          requestUserId, review.getUser().getId());
      throw new DeokhugamException(ErrorCode.REVIEW_UPDATE_FORBIDDEN,
          Map.of(
              "reviewId", reviewId,
              "requestUserId", requestUserId
          ));
    }

    // 논리 삭제된 리뷰인지 검증
    if (!review.isDeleted()) {
      log.warn("리뷰 물리 삭제 실패 - 논리 삭제되지 않은 리뷰: reviewId={}, requestUserId={}", reviewId,
          requestUserId);
      throw new DeokhugamException(ErrorCode.REVIEW_NOT_SOFT_DELETED);
    }
    // 물리 삭제
    reviewRepository.delete(review);
  }

  @Override
  @Transactional
  public ReviewLikeDto toggleLike(UUID reviewId, UUID requestUserId) {
    Review review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
        .orElseThrow(
            () -> new DeokhugamException(ErrorCode.REVIEW_NOT_FOUND, Map.of("reviewId", reviewId)));

    User user = userRepository.findById(requestUserId)
        .orElseThrow(() -> new DeokhugamException(ErrorCode.USER_NOT_FOUND,
            Map.of("requestUserId", requestUserId)));

    return reviewLikeRepository.findByReviewIdAndUserId(reviewId, requestUserId)
        .map(reviewLike -> {
          reviewLikeRepository.delete(reviewLike);
          review.decreaseLikeCount();
          log.info("리뷰 좋아요 취소: reviewId={}, requestUserId={}", reviewId, requestUserId);
          return new ReviewLikeDto(reviewId, requestUserId, false);
        })
        .orElseGet(() -> {
          try {
            ReviewLike reviewLike = new ReviewLike(review, user);
            reviewLikeRepository.save(reviewLike);
            review.increaseLikeCount();

            if (!review.getUser().getId().equals(requestUserId)) {
              try {
                TransactionTemplate transactionTemplate = new TransactionTemplate(
                    transactionManager);
                transactionTemplate.setPropagationBehavior(
                    TransactionDefinition.PROPAGATION_REQUIRES_NEW);

                transactionTemplate.executeWithoutResult(status ->
                    notificationService.create(
                        new NotificationCreateRequest(
                            review,
                            user,
                            "내가 작성한 리뷰에 좋아요가 추가되었습니다."
                        )
                    )
                );
              } catch (Exception e) {
                log.error("리뷰 좋아요 알림 생성 실패: reviewId={}, actorUserId={}",
                    reviewId, requestUserId, e);
              }
            }

            log.info("리뷰 좋아요 추가: reviewId={}, requestUserId={}", reviewId, requestUserId);

            return new ReviewLikeDto(reviewId, requestUserId, true);

          } catch (DataIntegrityViolationException e) {
            log.warn("리뷰 좋아요 추가 중복 감지: reviewId={}, requestUserId={}", reviewId, requestUserId);

            return new ReviewLikeDto(reviewId, requestUserId, true);
          }
        });
  }
}
