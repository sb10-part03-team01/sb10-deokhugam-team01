package com.team01.deokhugam.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    String email,

    // [2, 20] characters
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
    String nickname,

    // matches ^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,20}$

    /*
    ?= ...  이 위치 다음에 ... 패턴이 오는지
    .*      임의의 문자 반복 (문자열 내의 어느 위치든 상관 없음)
      .     아무 문자(줄바꿈 제외) 1개
      *     0개 이상 반복

    ^                         문자열 시작
    (?=.*[A-Za-z])            문자열 전체 중 어디든 영문자 최소 1개
    (?=.*\d)                  숫자 최소 1개
    (?=.*[@$!%*#?&])          특수문자 최소 1개
    [A-Za-z\d@$!%*#?&]{8,20}  허용 문자로만 8~20자 구성
    $                         문자열 끝
     */
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
        message = "비밀번호는 8~20자이며, 영문자, 숫자, 특수문자(@$!%*#?&)를 각각 1개 이상 포함해야 합니다."
    )
    String password
) {

}
