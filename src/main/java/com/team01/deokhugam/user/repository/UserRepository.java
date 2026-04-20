package com.team01.deokhugam.user.repository;

import com.team01.deokhugam.user.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

  /*
  SELECT COUNT(*) > 0
  FROM users u
  WHERE u.email = ? AND u.deleted_at IS NULL
   */
  boolean existsByEmailAndDeletedAtIsNull(String email);

  /*
  SELECT u
  FROM User u
  WHERE u.email = :email AND u.deleted_at IS NULL
   */
  Optional<User> findByEmailAndDeletedAtIsNull(String email);

  /*
  SELECT u
  FROM User u
  WHERE u.id = :id AND u.deleted_at IS NULL
   */
  Optional<User> findByIdAndDeletedAtIsNull(UUID id);
}
