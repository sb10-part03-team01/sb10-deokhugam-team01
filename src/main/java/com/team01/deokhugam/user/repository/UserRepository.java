package com.team01.deokhugam.user.repository;

import com.team01.deokhugam.user.entity.User;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

  /*
  SELECT COUNT(*) > 0
  FROM User u
  WHERE u.email = ? AND u.deletedAt IS NULL
   */
  boolean existsByEmailAndDeletedAtIsNull(String email);

  /*
  SELECT u
  FROM User u
  WHERE u.email = :email AND u.deletedAt IS NULL
   */
  Optional<User> findByEmailAndDeletedAtIsNull(String email);

  /*
  SELECT u
  FROM User u
  WHERE u.id = :id AND u.deletedAt IS NULL
   */
  Optional<User> findByIdAndDeletedAtIsNull(UUID id);

  /*
  SELECT u
  FROM User u
  WHERE u.isDeleted = true AND u.deletedAt < :expiredBefore
   */
  List<User> findAllByIsDeletedTrueAndDeletedAtBefore(OffsetDateTime expiredBefore);
}
