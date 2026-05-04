package com.team01.deokhugam.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestMdcLoggingFilter extends OncePerRequestFilter {
  // MDC 내부 로그용 key
  public static final String MDC_REQUEST_ID = "requestId";
  public static final String MDC_CLIENT_IP = "clientIp";

  // HTTP 응답 해더
  public static final String HEADER_REQUEST_ID = "X-Request-Id";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    // 요청별 추적 ID 생성 - 계층별 로그 여러개더라도 같은 id 공유
    String requestId = UUID.randomUUID().toString();

    // 프록시 고려, 클라이언트 IP 추출
    String clientIp = resolveClientIp(request);

    try {
      // 현재 요청 처리중인 스레드의 MDC에 저장
      // 이후 같은 요청 흐름에서 찍히는 로그는 requestId, clientIp를 같이 출력
      MDC.put(MDC_REQUEST_ID, requestId);
      MDC.put(MDC_CLIENT_IP, clientIp);

      // 클라이언트가 추적할 수 있도록 응답헤더에도 requestId
      response.setHeader(HEADER_REQUEST_ID, requestId);

      filterChain.doFilter(request, response);
    } finally {
      // 스레드 재사용 시 이전 요청 값 비우기
      MDC.remove(MDC_REQUEST_ID);
      MDC.remove(MDC_CLIENT_IP);
    }
  }

  private String resolveClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (hasText(xForwardedFor)) {
      // 프록시를 여러 번 거쳤다면 첫 번째 IP를 실제 클라이언트 IP로 본다.
      // X-Forwarded-For : 203.0.113.10, 10.0.0.3, 10.0.0.4
      return sanitize(xForwardedFor.split(",")[0].trim());
    }

    String xRealIp = request.getHeader("X-Real-IP");
    if (hasText(xRealIp)) {
      return sanitize(xRealIp.trim());
    }
    // 프록시 헤더 없으면, 서블릿 요청의 remote address를 사용
    // ex) 127.0.0.1 or 0:0:0:0:0:0:0:1
    return sanitize(request.getRemoteAddr());
  }

  private String sanitize(String value) {
    if (value == null) {
      return null;
    }

    // 로그 인젝션 방지를 위해 개행/탭 같은 제어 문자를 제거
    return value.replaceAll("[\\n\\r\\t]", "_");
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}
