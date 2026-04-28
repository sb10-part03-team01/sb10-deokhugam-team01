package com.team01.deokhugam.notification.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team01.deokhugam.notification.entity.Notification;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import static com.team01.deokhugam.notification.entity.QNotification.notification;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

  private final JPAQueryFactory jpaQueryFactory;

  private List<Notification> find(UUID userId, OffsetDateTime after, UUID cursor,
      Pageable pageable, boolean asc) {
    BooleanExpression cursorPredicate = null;
    if (after != null && cursor != null) {
      cursorPredicate = asc
          ? notification.createdAt.gt(after)
          .or(notification.createdAt.eq(after).and(notification.id.gt(cursor)))
          : notification.createdAt.lt(after)
              .or(notification.createdAt.eq(after).and(notification.id.lt(cursor)));
    }
    OrderSpecifier<?> createdAtOrder = asc ? notification.createdAt.asc() : notification.createdAt.desc();
    OrderSpecifier<?> idOrder = asc ? notification.id.asc() : notification.id.desc();

    return jpaQueryFactory
        .selectFrom(notification)
        .join(notification.user).fetchJoin()
        .join(notification.review).fetchJoin()
        .where(notification.user.id.eq(userId), cursorPredicate)
        .orderBy(createdAtOrder, idOrder)
        .limit(pageable.getPageSize())
        .fetch();
  }

  @Override
  public List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable) {
    return find(userId, null, null, pageable, false);
  }

  @Override
  public List<Notification> findByUserIdAndCreatedAtBeforeOrderByCreatedAtDesc(UUID userId,
      OffsetDateTime after, UUID cursor, Pageable pageable) {
    return find(userId, after, cursor, pageable, false);
  }

  @Override
  public List<Notification> findByUserIdOrderByCreatedAtAsc(UUID userId, Pageable pageable) {
    return find(userId, null, null, pageable, true);
  }

  @Override
  public List<Notification> findByUserIdAndCreatedAtAfterOrderByCreatedAtAsc(UUID userId,
      OffsetDateTime after, UUID cursor, Pageable pageable) {
    return find(userId, after, cursor, pageable, true);
  }
}
