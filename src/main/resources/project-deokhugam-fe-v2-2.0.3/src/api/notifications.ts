import {apiClient} from "./client.ts";
import {useAuthStore} from "@/store/authStore.ts";

export interface Notification {
  id: string;
  userId: string;
  reviewId: string;
  reviewContent: string;
  message: string;
  confirmed: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface NotificationsResponse {
  content: Notification[];
  nextCursor: string;
  nextAfter: string;
  size: number;
  totalElements: number;
  hasNext: boolean;
}

export interface NotificationsParams {
  userId: string;
  direction?: "ASC" | "DESC";
  cursor?: string;
  after?: string;
  limit?: number;
}

export const getNotifications = async (
    params: NotificationsParams
): Promise<NotificationsResponse> => {
  const authState = useAuthStore.getState();
  if (!authState.user?.id) {
    throw new Error("로그인이 필요합니다.");
  }

  const queryParams = new URLSearchParams();

  queryParams.append("userId", params.userId);

  if (params.direction) {
    queryParams.append("direction", params.direction);
  }

  if (params.cursor) {
    queryParams.append("cursor", params.cursor);
  }

  if (params.after) {
    queryParams.append("after", params.after);
  }

  if (params.limit) {
    queryParams.append("limit", params.limit.toString());
  }

  return await apiClient.get<NotificationsResponse>(
      `/api/notifications?${queryParams.toString()}`
  );
};

export const markNotificationAsRead = async (
    notificationId: string
): Promise<void> => {
  const authState = useAuthStore.getState();
  if (!authState.user?.id) {
    throw new Error("로그인이 필요합니다.");
  }
  return await apiClient.patch<void>(`/api/notifications/${notificationId}`, {
    confirmed: true
  });
};

export const markAllNotificationsAsRead = async (): Promise<void> => {
  const authState = useAuthStore.getState();
  if (!authState.user?.id) {
    throw new Error("로그인이 필요합니다.");
  }
  return await apiClient.patch<void>(`/api/notifications/read-all`, {});
};
