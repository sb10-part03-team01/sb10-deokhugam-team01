package com.team01.deokhugam.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("deokhugam API 문서")
            .description("deokhugam 프로젝트의 Swagger API 문서입니다.")
            .version("1.2")
        )
        .servers(List.of(
            // Swagger UI의 "Try it out" 요청이 전송될 기본 서버 주소
            new Server().url("/").description("현재 접속 서버")
        ));
  }
}
