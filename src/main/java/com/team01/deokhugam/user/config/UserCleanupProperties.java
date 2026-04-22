package com.team01.deokhugam.user.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

// 설정값을 외부 설정 파일에서 읽어와 자바 객체로 바인딩
// 설정값은 런타임에 바뀌면 안됨 -> 불변성 보장을 위한 record 사용
// record -> 테스트에서 값 바꾸기 객체 주입으로 용이
@Validated
@ConfigurationProperties(prefix = "deokhugam.user.cleanup")
public record UserCleanupProperties(
    // 논리 삭제된 사용자를 물리 삭제하기까지의 보존 시간
    @Min(1) long retentionMinutes // 음수 방지 검증 (프로필 설정 실수 방지)
) {

}
