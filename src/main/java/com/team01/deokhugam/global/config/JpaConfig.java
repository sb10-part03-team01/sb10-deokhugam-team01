package com.team01.deokhugam.global.config;

import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "offsetDateTimeProvider")
public class JpaConfig {

  @Bean
  public DateTimeProvider offsetDateTimeProvider() {
    // Auditing 시 OffsetDateTime.now()를 사용하도록 강제
    return () -> Optional.of(OffsetDateTime.now());
  }
}
