package com.team01.deokhugam.user.dto;

import com.team01.deokhugam.user.entity.User;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UserDto(
    UUID id,
    String email,
    String nickname,
    OffsetDateTime createdAt
) {

  public static UserDto from(User user) {
    return new UserDto(
        user.getId(),
        user.getEmail(),
        user.getNickname(),
        user.getCreatedAt()
    );
  }
}
