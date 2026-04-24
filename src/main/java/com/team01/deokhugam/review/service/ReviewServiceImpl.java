package com.team01.deokhugam.review.service;

import com.team01.deokhugam.book.entity.Book;
import com.team01.deokhugam.book.repository.BookRepository;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.exception.book.BookNotFoundException;
import com.team01.deokhugam.global.exception.review.ReviewAlreadyExistsException;
import com.team01.deokhugam.global.exception.review.ReviewNotFoundException;
import com.team01.deokhugam.global.exception.review.ReviewNotSoftDeletedException;
import com.team01.deokhugam.global.exception.review.ReviewUpdateForbiddenException;
import com.team01.deokhugam.global.exception.user.UserNotFoundException;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import com.team01.deokhugam.global.pagination.CursorPaginationUtils;
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
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

  private final ReviewRepository reviewRepository;
  private final ReviewLikeRepository reviewLikeRepository;
  private final BookRepository bookRepository;
  private final UserRepository userRepository;
  private final ReviewMapper reviewMapper;

  @Override
  @Transactional
  public ReviewDto createReview(ReviewCreateRequest request) {
    //도서 확인
    Book book = bookRepository.findById(request.bookId())
        .orElseThrow(() -> new BookNotFoundException(request.bookId()));

    //사용자 확인
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new UserNotFoundException(request.userId()));

    // 리뷰 중복확인
    if (reviewRepository.existsByBookIdAndUserIdAndIsDeletedFalse(request.bookId(),
        request.userId())) {
      throw new ReviewAlreadyExistsException(request.bookId(), request.userId());
    }

    // 리뷰 엔티티 생성
    Review review = new Review(
        book,
        user,
        request.content(),
        request.rating()
    );
    Review savedReview = reviewRepository.save(review);

    return reviewMapper.toDto(savedReview);
  }

  @Override
  public ReviewDto getReview(UUID reviewId, UUID requestUserId) {
    Review review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
        .orElseThrow(() -> new ReviewNotFoundException(reviewId));

    ReviewDto reviewDto = reviewMapper.toDto(review);
    boolean likedByMe = reviewLikeRepository.existsByReviewIdAndUserId(reviewId, requestUserId);

    return new ReviewDto(
        reviewDto.id(),
        reviewDto.bookId(),
        reviewDto.bookTitle(),
        reviewDto.bookThumbnailUrl(),
        reviewDto.userId(),
        reviewDto.userNickname(),
        reviewDto.content(),
        reviewDto.rating(),
        reviewDto.likeCount(),
        reviewDto.commentCount(),
        likedByMe,
        reviewDto.createdAt(),
        reviewDto.updatedAt()
    );
  }

  // 개선 필요
  // * JPA 기존 페이징 처리 확인 *****
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

    List<ReviewDto> content = reviews.stream()
        .map(review -> {
          ReviewDto reviewDto = reviewMapper.toDto(review);
          boolean likedByMe = reviewLikeRepository.existsByReviewIdAndUserId(review.getId(),
              requestUserId);

          return new ReviewDto(
              reviewDto.id(),
              reviewDto.bookId(),
              reviewDto.bookTitle(),
              reviewDto.bookThumbnailUrl(),
              reviewDto.userId(),
              reviewDto.userNickname(),
              reviewDto.content(),
              reviewDto.rating(),
              reviewDto.likeCount(),
              reviewDto.commentCount(),
              likedByMe,
              reviewDto.createdAt(),
              reviewDto.updatedAt()
          );
        })
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
        .orElseThrow(() -> new ReviewNotFoundException(reviewId));

    // 사용자가 쓴 리뷰인지 검증(NPE)
    if (!review.getUser().getId().equals(requestUserId)) {
      throw new ReviewUpdateForbiddenException(reviewId, requestUserId);
    }

    // 리뷰 수정
    review.update(request.content(), request.rating());

    return reviewMapper.toDto(review);
  }

  @Override
  @Transactional
  public void deleteReview(UUID reviewId, UUID requestUserId) {
    // 리뷰  존재 검증
    Review review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
        .orElseThrow(() -> new ReviewNotFoundException(reviewId));

    // 사용자가 쓴 리뷰인지 검증
    if (!review.getUser().getId().equals(requestUserId)) {
      throw new ReviewUpdateForbiddenException(reviewId, requestUserId);
    }

    // 리뷰 논리 삭제
    review.softDelete();
  }

  @Override
  @Transactional
  public void hardDeleteReview(UUID reviewId, UUID requestUserId) {
    // 리뷰 존재 검증
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new ReviewNotFoundException(reviewId));

    // 사용자가 쓴 리뷰인지 검증
    if (!review.getUser().getId().equals(requestUserId)) {
      throw new ReviewUpdateForbiddenException(reviewId, requestUserId);
    }

    // 논리 삭제된 리뷰인지 검증
    if (!review.isDeleted()) {
      throw new ReviewNotSoftDeletedException();
    }

    // 물리 삭제
    reviewRepository.delete(review);
  }

  @Override
  @Transactional
  public ReviewLikeDto toggleLike(UUID reviewId, UUID requestUserId) {
    // 리뷰 검증(논리 삭제 된 것들 제외 )
    Review review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
        .orElseThrow(() -> new ReviewNotFoundException(reviewId));

    //사용자 확인
    User user = userRepository.findById(requestUserId)
        .orElseThrow(() -> new UserNotFoundException(requestUserId));

    // (이 메소드가 필요한 상황은 이미 토글버튼을 눌렀기 때문에 버튼을 눌렀는가?
    // 혹은 진짜 눌렀는지 인증을 할 필요가 없다고 판단)
    // 좋아요가 이미 있으면 취소를 한다
    return reviewLikeRepository.findByReviewIdAndUserId(reviewId, requestUserId)
        .map(reviewLike -> {
          reviewLikeRepository.delete(reviewLike);
          review.decreaseLikeCount();
          return new ReviewLikeDto(reviewId, requestUserId, false);
        })
        .orElseGet(() -> {
          // 그 전 상태가 좋아요가 없으면 좋아요 증가를 한다
          ReviewLike reviewLike = new ReviewLike(review, user);
          reviewLikeRepository.save(reviewLike);
          review.increaseLikeCount();
          return new ReviewLikeDto(reviewId, requestUserId, true);
        });
  }
}
