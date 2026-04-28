package com.team01.deokhugam.global.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Configuration
public class ExternalApiConfig {
  @Value("${deokhugam.naver.client.id}")
  private String naverClientId;

  @Value("${deokhugam.naver.client.secret}")
  private String naverClientSecret;



  @Bean
  public RestClient naverRestClient(RestClient.Builder builder){
    if (!StringUtils.hasText(naverClientId) || !StringUtils.hasText(naverClientSecret)) {
      throw new IllegalStateException("네이버 API 인증 정보(자격 증명)를 설정해야 합니다");
    }
    // 타임 아웃 설정 - 대기 시간 지나면 바로 연결 끊어버림
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout((int) Duration.ofSeconds(3).toMillis()); // 3초 (연결대기 시간)
    factory.setReadTimeout((int) Duration.ofSeconds(5).toMillis()); // 5초 (데이터 읽기 대기 시간)

    return builder
        .requestFactory(factory) // 타임아웃 설정
        .baseUrl("https://openapi.naver.com/v1/search")
        .defaultHeader("X-Naver-Client-Id", naverClientId)
        .defaultHeader("X-Naver-Client-Secret", naverClientSecret)
        .build();
  }

  @Bean
  public RestClient ocrRestClient(RestClient.Builder builder){

    // 타임 아웃 설정 - 대기 시간 지나면 바로 연결 끊어버림
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout((int) Duration.ofSeconds(10).toMillis()); // 3초 (연결대기 시간)
    factory.setReadTimeout((int) Duration.ofSeconds(30).toMillis()); // 5초 (데이터 읽기 대기 시간)

    return builder
        .requestFactory(factory) // 타임아웃 설정
        .baseUrl("https://api.ocr.space")
        .build();
  }

  @Bean
  public RestClient defaultRestClient(RestClient.Builder builder){
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout((int) Duration.ofSeconds(3).toMillis());
    factory.setReadTimeout((int) Duration.ofSeconds(5).toMillis());
    return builder
        .requestFactory(factory)
        .build();
  }
}
