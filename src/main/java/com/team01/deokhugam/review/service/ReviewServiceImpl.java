package com.team01.deokhugam.review.service;

import com.team01.deokhugam.book.entity.Book;
import com.team01.deokhugam.book.repository.BookRepository;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.exception.book.BookNotFoundException;
import com.team01.deokhugam.global.exception.review.ReviewUpdateForbidden;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import com.team01.deokhugam.global.pagination.CursorPaginationUtils;
import com.team01.deokhugam.review.dto.CursorPageResponseReviewDto;
import com.team01.deokhugam.review.dto.ReviewCreateRequest;
import com.team01.deokhugam.review.dto.ReviewDto;
import com.team01.deokhugam.review.dto.ReviewSearchCondition;
import com.team01.deokhugam.review.dto.ReviewUpdateRequest;
import com.team01.deokhugam.review.entity.Review;
import com.team01.deokhugam.review.mapper.ReviewMapper;
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
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    // 리뷰 중복확인
    if (reviewRepository.existsByBook_IdAndUser_IdAndIsDeletedFalse(request.bookId(),
        request.userId())) {
      throw new IllegalArgumentException("해당 도서에 작성한 리뷰가 있습니다.");
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
    // 리뷰 검증
    Review review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
        .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

    /*TODO 리뷰 좋아요 기능 아직 구현 전 이라 구현 후  likedByMe 결과값 반영
       likedByMe에 requestUserId 사용
     */
    return reviewMapper.toDto(review);
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

    List<ReviewDto> content = reviewMapper.toDtoList(reviews);

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
        .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

    // 사용자가 쓴 리뷰인지 검증
    if (!review.getUser().getId().equals(requestUserId)) {
      throw new ReviewUpdateForbidden(reviewId, requestUserId);
    }

    // 리뷰 수정
    review.update(request.content(), request.rating());

    return reviewMapper.toDto(review);
  }
}
