package com.team01.deokhugam.book.repository.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team01.deokhugam.book.Book;
import com.team01.deokhugam.book.repository.BookRepositoryQueryDsl;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import static com.team01.deokhugam.book.QBook.book;

@Repository
@RequiredArgsConstructor
public class BookRepositoryQueryDslImpl implements BookRepositoryQueryDsl {
  private final JPAQueryFactory queryFactory;

  @Override
  public List<Book> findBooks(String keyword,
      String orderBy,
      String direction,
      String cursor,
      OffsetDateTime after,
      int limit) {

    return queryFactory
        .selectFrom(book)
        // 조건
        .where(
            book.isDeleted.eq(false),
            // keyword를 포함하는 조건
            containsKeyword(keyword),
            // 커서 기준으로 다음 값인지 판변하는 조건
            cursorCondition(cursor, after, orderBy, direction)
        )
        // 정렬 조건
        .orderBy(
            // 1순위 조건: 정렬기준과 방향으로 정렬하는 조건
            dynamicOrder(orderBy, direction),
            // 2순위
            book.createdAt.desc(),
            // 3순위
            book.id.desc()
        )
        // 11개를 가져옴
        .limit(limit + 1)
        // 리스트로 반환
        .fetch();
  }

  // 키워드에 해당하는 책 개수 반환
  @Override
  public long countBooks(String keyword) {
    Long count = queryFactory
        .select(book.count())
        .from(book)
        .where(
            book.isDeleted.eq(false),
            containsKeyword(keyword)
        )
        .fetchOne();
    return count != null ? count : 0L;
  }

  // 키워드가 포함되는지 안되는지 판별하는 조건문을 반환
  private BooleanBuilder containsKeyword(String keyword){
    // 공백인지 아닌지 검사
    if(!StringUtils.hasText(keyword)){
      return null; // 만약 키워드가 없으면 해당 조건문은 그냥 무시가 된다.
    }
    BooleanBuilder builder = new BooleanBuilder();
    return builder
        .or(book.title.containsIgnoreCase(keyword)) // 도서의 제목에 keyword가 포함되는지(대소문자 상관x)
        .or(book.author.containsIgnoreCase(keyword)) // 도서의 작가이름에 keyword가 포함되는지(대소문자 상관x)
        .or(book.isbn.containsIgnoreCase(keyword)); // 도서의 isbn에 keyword가 포함되는지(대소문자 상관x)
  }

  // OrderSpecifier<>(a,b) -> b를 기준으로 a방향으로 정렬 조건문을 반환
  private OrderSpecifier<?> dynamicOrder(String orderBy, String direction){
    Order order = "ASC".equalsIgnoreCase(direction) ? Order.ASC : Order.DESC;

    if(!StringUtils.hasText(orderBy)){
      // 정렬 기준이 없으면 생성일을 기본 정렬 기준으로 설정
      return new OrderSpecifier<>(order, book.createdAt);
    }

    return switch (orderBy){
      case "title" -> new OrderSpecifier<>(order, book.title);
      case "rating" -> new OrderSpecifier<>(order, book.rating);
      case "reviewCount" -> new OrderSpecifier<>(order, book.reviewCount);
      case "publishDate" -> new OrderSpecifier<>(order, book.publishedDate);
      default -> new OrderSpecifier<>(order, book.createdAt);
    };
  }

  // 커서, 보조커서, 정렬기준, 정렬방향에 따른 조건문
  private BooleanExpression cursorCondition(String cursor, OffsetDateTime after, String orderBy, String direction){
    if(!StringUtils.hasText(cursor) || after == null){
      // 커서가 없고 보조 커서도 없으면 조건문은 무시한다.
      return null;
    }

    boolean isAsc = "ASC".equalsIgnoreCase(direction);

    // 정렬 기준에 따라서 커서도 바뀌어야함
    // 커서 기준으로 오름차순/내림차순에 따라 where cursor < 값 or (cursor = 값 and 보조커서 <  after)인 쿼리문과 같다
    return switch (orderBy){
      case "title" -> {
        yield isAsc
            ? book.title.gt(cursor).or(book.title.eq(cursor).and(book.createdAt.gt(after)))
            : book.title.lt(cursor).or(book.title.eq(cursor).and(book.createdAt.lt(after)));
      }

      case "rating" -> {
        double ratingCursor = Double.parseDouble(cursor);
        yield isAsc
            ? book.rating.gt(ratingCursor).or(book.rating.eq(ratingCursor).and(book.createdAt.gt(after)))
            : book.rating.lt(ratingCursor).or(book.rating.eq(ratingCursor).and(book.createdAt.lt(after)));
      }

      case "reviewCount" -> {
        int countCursor = Integer.parseInt(cursor);
        yield isAsc
            ? book.reviewCount.gt(countCursor).or(book.reviewCount.eq(countCursor).and(book.createdAt.gt(after)))
            : book.reviewCount.lt(countCursor).or(book.reviewCount.eq(countCursor).and(book.createdAt.lt(after)));
      }

      case "publishedDate" -> {
        LocalDate dateCursor = LocalDate.parse(cursor);
        yield isAsc
            ? book.publishedDate.gt(dateCursor).or(book.publishedDate.eq(dateCursor).and(book.createdAt.gt(after)))
            : book.publishedDate.lt(dateCursor).or(book.publishedDate.eq(dateCursor).and(book.createdAt.lt(after)));
      }
      // 그 외의 정렬 기준이 들어온다며 날짜 기준으로 함
      default -> {
        yield  isAsc
            ? book.createdAt.gt(after)
            : book.createdAt.lt(after);
      }
    };

  }
}
