package com.team01.deokhugam.global.filter;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class RequestMdcLoggingFilterTest {
  private final RequestMdcLoggingFilter filter = new RequestMdcLoggingFilter();

  @Test
  @DisplayName("필터 실행 중에는 MDC에 requestId, clientIp가 저장되고 응답 헤더에도 requestId가 추가된다")
  void should_put_mdc_values_and_add_request_id_header() throws ServletException, IOException {
    // given
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    request.addHeader("X-Forwarded-For", "203.0.113.10, 10.0.0.3");
    request.setRemoteAddr("127.0.0.1");

    FilterChain chain =
        (req, res) -> {
          // 필터 체인 내부에서는 MDC 값이 살아 있어야 한다.
          String requestId = MDC.get(RequestMdcLoggingFilter.MDC_REQUEST_ID);
          String clientIp = MDC.get(RequestMdcLoggingFilter.MDC_CLIENT_IP);

          assertThat(requestId).isNotBlank();
          assertThat(clientIp).isEqualTo("203.0.113.10");

          // 응답 헤더에도 같은 requestId가 들어가 있어야 한다.
          assertThat(
                  ((MockHttpServletResponse) res)
                      .getHeader(RequestMdcLoggingFilter.HEADER_REQUEST_ID))
              .isEqualTo(requestId);
        };

    // when - 필터 한번 실행
    filter.doFilter(request, response, chain);

    // then
    // 요청 처리가 끝난 뒤에는 MDC 값이 제거되어야 한다.
    assertThat(MDC.get(RequestMdcLoggingFilter.MDC_REQUEST_ID)).isNull();
    assertThat(MDC.get(RequestMdcLoggingFilter.MDC_CLIENT_IP)).isNull();

    // 응답 헤더는 최종 응답에도 남아 있어야 한다.
    assertThat(response.getHeader(RequestMdcLoggingFilter.HEADER_REQUEST_ID)).isNotBlank();
  }

  @Test
  @DisplayName("X-Forwarded-For가 없고 X-Real-IP가 있으면 X-Real-IP를 clientIp로 사용한다")
  void should_use_x_real_ip_when_x_forwarded_for_is_missing() throws ServletException, IOException {
    // given
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    request.addHeader("X-Real-IP", "192.168.0.10");
    request.setRemoteAddr("127.0.0.1");

    FilterChain chain =
        (req, res) ->
            assertThat(MDC.get(RequestMdcLoggingFilter.MDC_CLIENT_IP)).isEqualTo("192.168.0.10");

    // when
    filter.doFilter(request, response, chain);

    // then
    assertThat(MDC.get(RequestMdcLoggingFilter.MDC_CLIENT_IP)).isNull();
  }
}
