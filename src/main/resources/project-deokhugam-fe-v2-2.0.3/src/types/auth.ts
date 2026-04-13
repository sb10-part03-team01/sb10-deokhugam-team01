// 회원가입 요청 타입
export interface SignupRequest {
  email: string;
  nickname: string;
  password: string;
}

// 회원가입 응답 타입
export interface SignupResponse {
  id: string;
  email: string;
  nickname: string;
  createdAt: string;
}

// 로그인 요청 타입
export interface LoginRequest {
  email: string;
  password: string;
}

// 로그인 응답 타입
export interface LoginResponse {
  id: string;
  email: string;
  nickname: string;
  createdAt: string;
}

// 사용자 타입 (공통)
export interface User extends UserNicknameRequest {
  id: string;
  email: string;
  createdAt: string;
}

// 사용자 닉네임 수정
export interface UserNicknameRequest {
  nickname: string;
}
