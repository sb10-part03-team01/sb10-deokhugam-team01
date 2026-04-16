package com.team01.deokhugam.user.entity;

import com.team01.deokhugam.global.entity.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseUpdatableEntity {

  // id

  // createdAt

  // updatedAt

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "nickname", nullable = false)
  private String nickname;

  @Column(name = "password", nullable = false)
  private String password;

  // deletedAt이 null이면 활성, 값이 있으면 삭제됨 -> 이걸로 isDeleted 역할 대신 가능
  @Column(name = "is_deleted", nullable = false)
  private boolean isDeleted = false;

  private Instant deletedAt;

  public static User create(String email, String nickname, String password) {
    User user = new User();
    user.email = email;
    user.nickname = nickname;
    user.password = password;
    return user;
  }

  public void updateNickname(String nickname) {
    this.nickname = nickname;
  }

  public void softDelete() {
    this.deletedAt = Instant.now();
    this.isDeleted = true;
  }
}
