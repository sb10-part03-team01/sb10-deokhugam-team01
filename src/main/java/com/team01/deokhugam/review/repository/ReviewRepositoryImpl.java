package com.team01.deokhugam.review.repository;

import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.review.dto.ReviewSearchCondition;
import com.team01.deokhugam.review.entity.Review;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

  @PersistenceContext
  private EntityManager em;

  @Override
  public List<Review> findAllByCondition(ReviewSearchCondition condition) {
    int limit = condition.normalizedLimit();

    String direction = condition.direction() == SortDirection.ASC ? "ASC" : "DESC";
    boolean isAsc = condition.direction() == SortDirection.ASC;
    boolean isRatingOrder = "rating".equalsIgnoreCase(condition.orderBy());

    OffsetDateTime after = condition.after();
    String rawCursor = condition.cursor();

    boolean hasCursor = StringUtils.hasText(rawCursor);
    boolean hasAfter = after != null;

    if (hasAfter != hasCursor) {
      throw new IllegalArgumentException("after와 cursor는 같이 전달 되어야 합니다.");
    }

    String comparisonOperator = isAsc ? ">" : "<";

    QueryParts queryParts = buildFilterQueryParts(condition);

    StringBuilder jpql = new StringBuilder(
        """
            select r
            from Review r
            join fetch r.user u
            join fetch r.book b
            """
    );
    jpql.append(queryParts.whereClause());

    Map<String, Object> params = new HashMap<>(queryParts.params());

    if (after != null) {
      if (isRatingOrder) {
        RatingCursor ratingCursor = parseRatingCursor(rawCursor);

        jpql.append(" and (")
            .append("r.rating ").append(comparisonOperator).append(" :ratingCursor")
            .append(" or (r.rating = :ratingCursor and r.createdAt ")
            .append(comparisonOperator).append(" :after)")
            .append(" or (r.rating = :ratingCursor and r.createdAt = :after and r.id ")
            .append(comparisonOperator).append(" :idCursor))");

        params.put("ratingCursor", ratingCursor.rating());
        params.put("after", after);
        params.put("idCursor", ratingCursor.id());
      } else {
        UUID cursor = parseUuidCursor(rawCursor);

        jpql.append(" and (r.createdAt ")
            .append(comparisonOperator)
            .append(" :after")
            .append(" or (r.createdAt = :after and r.id ")
            .append(comparisonOperator)
            .append(" :cursor))");

        params.put("after", after);
        params.put("cursor", cursor);
      }
    }

    if (isRatingOrder) {
      jpql.append(" order by r.rating ")
          .append(direction)
          .append(", r.createdAt ")
          .append(direction)
          .append(", r.id ")
          .append(direction);
    } else {
      jpql.append(" order by r.createdAt ")
          .append(direction)
          .append(", r.id ")
          .append(direction);
    }

    TypedQuery<Review> query = em.createQuery(jpql.toString(), Review.class);
    params.forEach(query::setParameter);
    query.setMaxResults(limit + 1);

    return query.getResultList();
  }

  @Override
  public long countByCondition(ReviewSearchCondition condition) {
    QueryParts queryParts = buildFilterQueryParts(condition);

    StringBuilder jpql = new StringBuilder(
        """
            select count(r)
            from Review r
            """
    );

    if (queryParts.requiresUserJoin()) {
      jpql.append(" join r.user u ");
    }

    jpql.append(queryParts.whereClause());

    TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
    queryParts.params().forEach(query::setParameter);

    return query.getSingleResult();
  }

  private QueryParts buildFilterQueryParts(ReviewSearchCondition condition) {
    StringBuilder whereClause = new StringBuilder(" where r.isDeleted = false ");
    Map<String, Object> params = new HashMap<>();
    boolean requiresUserJoin = false;

    if (condition.userId() != null) {
      whereClause.append(" and r.user.id = :userId ");
      params.put("userId", condition.userId());
    }

    if (condition.bookId() != null) {
      whereClause.append(" and r.book.id = :bookId ");
      params.put("bookId", condition.bookId());
    }

    if (condition.keyword() != null && !condition.keyword().isBlank()) {
      requiresUserJoin = true;
      whereClause.append(
          """
               and (
                 lower(u.nickname) like lower(:keyword) escape '\\'
                 or lower(r.content) like lower(:keyword) escape '\\'
               )
              """
      );
      params.put("keyword", "%" + escapeLikeKeyword(condition.keyword()) + "%");
    }

    return new QueryParts(whereClause.toString(), params, requiresUserJoin);
  }

  private UUID parseUuidCursor(String cursor) {
    if (!StringUtils.hasText(cursor)) {
      return null;
    }

    try {
      return UUID.fromString(cursor);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("cursor 형식이 올바르지 않습니다.");
    }
  }

  private RatingCursor parseRatingCursor(String cursor) {
    if (!StringUtils.hasText(cursor)) {
      throw new IllegalArgumentException("rating 정렬 cursor 형식이 올바르지 않습니다.");
    }

    String[] parts = cursor.split("\\|", 2);
    if (parts.length != 2) {
      throw new IllegalArgumentException("rating 정렬 cursor 형식이 올바르지 않습니다.");
    }

    try {
      double rating = Double.parseDouble(parts[0]);
      if (!Double.isFinite(rating)) {
        throw new IllegalArgumentException("rating 정렬 cursor 형식이 올바르지 않습니다.");
      }
      UUID id = UUID.fromString(parts[1]);
      return new RatingCursor(rating, id);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("rating 정렬 cursor 형식이 올바르지 않습니다.");
    }
  }

  private String escapeLikeKeyword(String keyword) {
    return keyword.trim()
        .replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_");
  }

  private record RatingCursor(double rating, UUID id) {

  }

  private record QueryParts(
      String whereClause,
      Map<String, Object> params,
      boolean requiresUserJoin
  ) {

  }
}
