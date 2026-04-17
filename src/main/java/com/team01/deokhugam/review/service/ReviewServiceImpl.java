package com.team01.deokhugam.review.service;

import com.team01.deokhugam.book.Book;
import com.team01.deokhugam.book.BookRepository;
import com.team01.deokhugam.global.exception.book.BookNotFoundException;
import com.team01.deokhugam.review.dto.ReviewCreateRequest;
import com.team01.deokhugam.review.dto.ReviewDto;
import com.team01.deokhugam.review.entity.Review;
import com.team01.deokhugam.review.mapper.ReviewMapper;
import com.team01.deokhugam.review.repository.ReviewRepository;
import com.team01.deokhugam.user.entity.User;
import com.team01.deokhugam.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

  private final ReviewRepository reviewRepository;
  private final BookRepository bookRepository;
  private final UserRepository userRepository;
  private final ReviewMapper reviewMapper;

  @Override
  public ReviewDto createReview(ReviewCreateRequest request) {
    //도서 확인
    Book book = bookRepository.findById(request.bookId())
        .orElseThrow(() -> new BookNotFoundException(request.bookId()));

    //사용자 확인 (interface)
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    // 리뷰 중복확인
    if (reviewRepository.existsByBook_IdAndUser_Id(request.bookId(), request.userId())) {
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

}
