import {apiClient, API_ENDPOINTS} from "./client.ts";
import type {
  SignupRequest,
  SignupResponse,
  LoginRequest,
  LoginResponse,
  User,
  UserNicknameRequest
} from "@/types/auth.ts";

// 인증 API 함수들
export const authApi = {
  // 회원가입
  async signup(userData: SignupRequest): Promise<SignupResponse> {
    try {
      const response = await apiClient.post<SignupResponse>(
          API_ENDPOINTS.USERS.SIGNUP,
          userData
      );
      return response;
    } catch (error) {
      console.error("회원가입 API 에러:", error);

      if (error instanceof Error) {
        if (error.message.includes("이미 존재하는 이메일")) {
          throw new Error("이미 존재하는 이메일입니다.");
        } else if (
            error.message.includes("status: 400") ||
            error.message.includes("400")
        ) {
          throw new Error("입력값을 확인해주세요.");
        } else if (
            error.message.includes("status: 500") ||
            error.message.includes("500")
        ) {
          throw new Error(
              "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
          );
        }
      }

      throw new Error("회원가입에 실패했습니다.");
    }
  },

  // 로그인
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    try {
      const response = await apiClient.post<LoginResponse>(
          API_ENDPOINTS.USERS.LOGIN,
          credentials
      );
      return response;
    } catch (error) {
      console.error("로그인 API 에러:", error);

      if (error instanceof Error) {
        if (
            error.message.includes("status: 401") ||
            error.message.includes("401") ||
            error.message.includes("이메일 또는 비밀번호가 올바르지 않습니다")
        ) {
          throw new Error("이메일 또는 비밀번호가 불일치합니다.");
        } else if (
            error.message.includes("status: 400") ||
            error.message.includes("400")
        ) {
          throw new Error("이메일 또는 비밀번호를 확인해주세요.");
        } else if (
            error.message.includes("status: 500") ||
            error.message.includes("500")
        ) {
          throw new Error(
              "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
          );
        }
      }

      throw new Error("로그인에 실패했습니다.");
    }
  },

  // 사용자 프로필 조회
  async getUserProfile(userId: string): Promise<User> {
    try {
      const response = await apiClient.get<User>(
          API_ENDPOINTS.USERS.PROFILE(userId)
      );
      return response;
    } catch (error) {
      console.error("사용자 프로필 조회 API 에러:", error);
      throw new Error("사용자 정보를 가져오는데 실패했습니다.");
    }
  }
};

// 프로필 수정
export const patchUserProfile = async (userId: string, data: string) => {
  try {
    const response = await apiClient.patch<UserNicknameRequest>(
        API_ENDPOINTS.USERS.PROFILE(userId),
        {nickname: data}
    );
    return response;
  } catch (error) {
    console.error("사용자 프로필 수정 API 에러:", error);
    throw new Error("사용자 정보를 수정하는데 실패했습니다.");
  }
};

// 회원 탈퇴
export const deleteUser = async (userId: string) => {
  try {
    await apiClient.delete(API_ENDPOINTS.USERS.PROFILE(userId));
  } catch (error) {
    console.error("사용자 탈퇴 API 에러:", error);
    throw new Error("사용자의 탈퇴를 실패했습니다.");
  }
};
