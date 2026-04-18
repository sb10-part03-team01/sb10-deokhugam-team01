package com.team01.deokhugam.user.controller;

import com.team01.deokhugam.user.dto.UserDto;
import com.team01.deokhugam.user.dto.UserLoginRequest;
import com.team01.deokhugam.user.dto.UserRegisterRequest;
import com.team01.deokhugam.user.dto.UserUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag(name = "사용자 관리", description = "사용자 관련 API")
public interface UserApi {

  /// POST - /api/users - 회원가입
  @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "201", description = "회원가입 성공",
          content = @Content(schema = @Schema(implementation = UserDto.class))
      ),
      @ApiResponse(
          responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)",
          content = @Content(examples = @ExampleObject(value = "이메일은 필수입니다."))
      ),
      @ApiResponse(
          responseCode = "409", description = "이메일 중복",
          content = @Content(examples = @ExampleObject(value = "이미 등록된 이메일이 존재합니다."))
      ),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<UserDto> register(
      @Parameter(
          description = "회원가입 정보",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
      ) @Valid UserRegisterRequest request
  );

  /// POST - /api/users/login - 로그인
  @Operation(summary = "로그인", description = "사용자 로그인을 처리합니다.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200", description = "로그인 성공",
          content = @Content(schema = @Schema(implementation = UserDto.class))
      ),
      @ApiResponse(
          responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)",
          content = @Content(examples = @ExampleObject(value = "이메일은 필수입니다."))
      ),
      @ApiResponse(
          responseCode = "401", description = "로그인 실패 (이메일 또는 비밀번호 불일치)",
          content = @Content(examples = @ExampleObject(value = "인증 실패"))
      ),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<UserDto> login(
      @Parameter(
          description = "로그인 정보",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
      ) @Valid UserLoginRequest request
  );

  /// GET - /api/users/{userId} - 사용자 정보 조회
  @Operation(summary = "사용자 정보 조회", description = "사용자 ID로 상세 정보를 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200", description = "사용자 정보 조회 성공",
          content = @Content(schema = @Schema(implementation = UserDto.class))
      ),
      @ApiResponse(
          responseCode = "404", description = "사용자 정보 없음",
          content = @Content(examples = @ExampleObject(value = "해당 유저가 존재하지 않습니다."))
      ),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<UserDto> getUser(
      @Parameter(description = "조회할 사용자 ID") UUID userId
  );

  /// DELETE - /api/users/{userId} - 사용자 논리 삭제
  @Operation(summary = "사용자 논리 삭제", description = "사용자를 논리적으로 삭제합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "사용자 삭제 성공"),
      @ApiResponse(
          responseCode = "403", description = "사용자 삭제 권한 없음",
          content = @Content(examples = @ExampleObject(value = "권한 없음"))
      ),
      @ApiResponse(
          responseCode = "404", description = "사용자 정보 없음",
          content = @Content(examples = @ExampleObject(value = "해당 유저가 존재하지 않습니다."))
      ),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<Void> deleteUser(
      @Parameter(description = "삭제할 사용자 ID") UUID userId,
      @Parameter(
          name = "Deokhugam-Request-User-ID",
          description = "요청자 ID",
          required = true
      ) UUID requestUserId
  );

  /// PATCH - /api/users/{userId} - 사용자 정보 수정
  @Operation(summary = "사용자 정보 수정", description = "사용자의 닉네임을 수정합니다.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200", description = "사용자 정보 수정 성공",
          content = @Content(schema = @Schema(implementation = UserDto.class))
      ),
      @ApiResponse(
          responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)",
          content = @Content(examples = @ExampleObject(value = "닉네임은 2~20자 사이여야 합니다."))
      ),
      @ApiResponse(
          responseCode = "403", description = "사용자 정보 수정 권한 없음",
          content = @Content(examples = @ExampleObject(value = "권한 없음"))
      ),
      @ApiResponse(
          responseCode = "404", description = "사용자 정보 없음",
          content = @Content(examples = @ExampleObject(value = "해당 유저가 존재하지 않습니다."))
      ),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<UserDto> updateUser(
      @Parameter(description = "수정할 사용자 ID") UUID userId,
      @Parameter(
          name = "Deokhugam-Request-User-ID",
          description = "요청자 ID",
          required = true
      ) UUID requestUserId,
      @Parameter(
          description = "수정할 사용자 정보",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
      ) @Valid UserUpdateRequest request
  );

  /// GET - /api/users/power - 파워 유저 목록 조회

  /// DELETE - /api/users/{userId}/hard - 사용자 물리 삭제
  @Operation(summary = "사용자 물리 삭제", description = "사용자를 물리적으로 삭제합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "사용자 삭제 성공"),
      @ApiResponse(
          responseCode = "403", description = "사용자 삭제 권한 없음",
          content = @Content(examples = @ExampleObject(value = "권한 없음"))
      ),
      @ApiResponse(
          responseCode = "404", description = "사용자 정보 없음",
          content = @Content(examples = @ExampleObject(value = "해당 유저가 존재하지 않습니다."))
      ),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<Void> permanentDeleteUser(
      @Parameter(description = "물리 삭제할 사용자 ID") UUID userId,
      @Parameter(
          name = "Deokhugam-Request-User-ID",
          description = "요청자 ID",
          required = true
      ) UUID requestUserId
  );
}
