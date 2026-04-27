package com.team01.deokhugam.notification.repository;

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

  @Override
  public List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable) {
    return jpaQueryFactory
        .selectFrom(notification)
        .join(notification.user).fetchJoin()
        .join(notification.review).fetchJoin()
        .where(notification.user.id.eq(userId))
        .orderBy(notification.createdAt.desc(), notification.id.desc())
        .limit(pageable.getPageSize()).
        fetch();
  }

  @Override
  public List<Notification> findByUserIdOrderByCreatedAtAsc(UUID userId, Pageable pageable) {
    return jpaQueryFactory
        .selectFrom(notification)
        .join(notification.user).fetchJoin()
        .join(notification.review).fetchJoin()
        .where(notification.user.id.eq(userId))
        .orderBy(notification.createdAt.asc(), notification.id.asc())
        .limit(pageable.getPageSize()).
        fetch();
  }

  @Override
  public List<Notification> findByUserIdAndCreatedAtBeforeOrderByCreatedAtDesc(UUID userId,
      OffsetDateTime after, UUID cursor, Pageable pageable) {
    return jpaQueryFactory
        .selectFrom(notification)
        .join(notification.user).fetchJoin()
        .join(notification.review).fetchJoin()
        .where(notification.user.id.eq(userId)
            .and(notification.createdAt.lt(after)
                .or(notification.createdAt.eq(after).and(notification.id.lt(cursor)))))
        .orderBy(notification.createdAt.desc(), notification.id.desc())
        .limit(pageable.getPageSize())
        .fetch();
  }

  @Override
  public List<Notification> findByUserIdAndCreatedAtAfterOrderByCreatedAtAsc(UUID userId,
      OffsetDateTime after, UUID cursor, Pageable pageable) {
    return jpaQueryFactory
        .selectFrom(notification)
        .join(notification.user).fetchJoin()
        .join(notification.review).fetchJoin()
        .where(notification.user.id.eq(userId)
            .and(notification.createdAt.gt(after)
                .or(notification.createdAt.eq(after).and(notification.id.gt(cursor)))))
        .orderBy(notification.createdAt.asc(), notification.id.asc())
        .limit(pageable.getPageSize())
        .fetch();
  }
}
