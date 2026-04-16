package com.team01.deokhugam.user.entity;

import com.team01.deokhugam.global.entity.BaseRemovableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseRemovableEntity {

  // id

  // createdAt

  // updatedAt

  // isDeleted

  // deletedAt

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "nickname", nullable = false, length = 20)
  private String nickname;

  // 암호화 저장 고려 길이 100 설정
  @Column(name = "password", nullable = false, length = 100)
  private String password;

  public User(String email, String nickname, String password) {
    this.email = email;
    this.nickname = nickname;
    this.password = password;
  }

  public void updateNickname(String nickname) {
    this.nickname = nickname;
  }
}
