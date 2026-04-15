package com.team01.deokhugam.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class BaseRemovableEntity extends BaseUpdatableEntity {

  @Column(name = "is_deleted", nullable = false)
  private boolean isDeleted = false;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  // 논리 삭제 용 -> 삭제여부 O / 삭제시간 부여
  // 물리적 삭제는 테스트만
  public void softDelete() {
    if (this.isDeleted) {
      return;
    }
    this.isDeleted = true;
    this.deletedAt = Instant.now();
  }
}
