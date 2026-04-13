import axios, {AxiosInstance, AxiosRequestConfig, AxiosResponse} from "axios";
import {useAuthStore} from "@/store/authStore.ts";

// API 기본 설정 - 모든 환경에서 Next.js 프록시 사용
const API_BASE_URL = "";

class ApiClient {
  private axiosInstance: AxiosInstance;

  constructor(baseURL: string) {
    this.axiosInstance = axios.create({
      baseURL,
      headers: {
        "Content-Type": "application/json"
      },
      timeout: 10000
    });

    this.axiosInstance.interceptors.request.use(
        config => {
          console.log(`API 요청: ${config.method?.toUpperCase()} ${config.url}`);

          if (typeof window !== "undefined") {
            const authState = useAuthStore.getState();
            if (authState.user?.id) {
              config.headers["Deokhugam-Request-User-ID"] = authState.user.id;
            }
          }

          return config;
        },
        error => {
          console.error("API 요청 에러:", error);
          return Promise.reject(error);
        }
    );

    // 응답 인터셉터
    this.axiosInstance.interceptors.response.use(
        (response: AxiosResponse) => {
          console.log(`API 응답: ${response.status} ${response.config.url}`);
          return response;
        },
        error => {
          console.error("API 응답 에러:", error);
          if (error.response) {
            const errorMessage =
                error.response.data?.message ||
                `HTTP error! status: ${error.response.status}`;
            throw new Error(errorMessage);
          } else if (error.request) {
            throw new Error("네트워크 에러: 서버에 연결할 수 없습니다.");
          } else {
            throw new Error("요청 설정 에러: " + error.message);
          }
        }
    );
  }

  // GET 요청
  async get<T>(
      endpoint: string,
      config?: AxiosRequestConfig & { skipInterceptor?: boolean }
  ): Promise<T> {
    if (config?.skipInterceptor) {
      // 인터셉터 우회: AxiosError를 그대로 던짐으로써 Response로 받은 Error Status 값에 따라 원하는 UI를 출력하기 위해 추가함_병진
      const response = await axios.get<T>(
          this.axiosInstance.defaults.baseURL + endpoint,
          config
      );
      return response.data;
    }
    const response = await this.axiosInstance.get<T>(endpoint, config);
    return response.data;
  }

  // POST 요청
  async post<T, D = unknown>(
      endpoint: string,
      data: D,
      config?: AxiosRequestConfig & { skipInterceptor?: boolean }
  ): Promise<T> {
    if (config?.skipInterceptor) {
      // 인터셉터 우회: AxiosError를 그대로 던짐으로써 Response로 받은 Error Status 값에 따라 원하는 UI를 출력하기 위해 추가함_혜림
      const response = await axios.post<T>(
          this.axiosInstance.defaults.baseURL + endpoint,
          data,
          config
      );

      return response.data;
    }
    const response = await this.axiosInstance.post<T>(endpoint, data, config);
    return response.data;
  }

  // PATCH 요청
  async patch<T, D = unknown>(
      endpoint: string,
      data: D,
      config?: AxiosRequestConfig & { skipInterceptor?: boolean }
  ): Promise<T> {
    if (config?.skipInterceptor) {
      // 인터셉터 우회: AxiosError를 그대로 던짐으로써 Response로 받은 Error Status 값에 따라 원하는 UI를 출력하기 위해 추가함_혜림
      const response = await axios.patch<T>(
          this.axiosInstance.defaults.baseURL + endpoint,
          data,
          config
      );

      return response.data;
    }

    const response = await this.axiosInstance.patch<T>(endpoint, data, config);
    return response.data;
  }

  async delete<T>(endpoint: string): Promise<T> {
    const response = await this.axiosInstance.delete<T>(endpoint);
    return response.data;
  }
}

export const apiClient = new ApiClient(API_BASE_URL);

export const API_ENDPOINTS = {
  USERS: {
    SIGNUP: "/api/users",
    LOGIN: "/api/users/login",
    PROFILE: (userId: string) => `/api/users/${userId}`
  },
  BOOKS: {
    LIST: "/api/books",
    DETAIL: "/api/books/{id}",
    CREATE: "/api/books",
    DELETE: "/api/books/{id}"
  },
  REVIEWS: {
    LIST: "/api/reviews",
    DETAIL: "/api/reviews/{id}",
    CREATE: "/api/reviews",
    DELETE: "/api/reviews/{id}"
  }
} as const;
