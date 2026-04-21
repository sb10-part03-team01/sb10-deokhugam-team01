package com.team01.deokhugam.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class NaverApiConfig {
  @Value("${deokhugam.naver.client.id}")
  private String clientId;

  @Value("${deokhugam.naver.client.secret}")
  private String clientSecret;

  @Bean
  public RestClient naverRestClient(RestClient.Builder builder){
    return builder
        .baseUrl("https://openapi.naver.com/v1/search")
        .defaultHeader("X-Naver-Client-Id", clientId)
        .defaultHeader("X-Naver-Client-Secret", clientSecret)
        .build();
  }
}
