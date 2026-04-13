import {apiClient} from "./client.ts";
import {useAuthStore} from "@/store/authStore.ts";
import type {
  CommentsResponse,
  CommentsParams,
  Comment
} from "@/types/reviews.ts";

export const getComments = async (
    params: CommentsParams
): Promise<CommentsResponse> => {
  const {reviewId, direction = "DESC", cursor, after, limit = 50} = params;

  const queryParams = new URLSearchParams();

  queryParams.append("reviewId", reviewId);
  queryParams.append("direction", direction);
  queryParams.append("limit", limit.toString());

  if (cursor) {
    queryParams.append("cursor", cursor);
  }

  if (after) {
    queryParams.append("after", after);
  }

  return await apiClient.get<CommentsResponse>(
      `/api/comments?${queryParams.toString()}`
  );
};

export const createComment = async (data: {
  reviewId: string;
  content: string;
}): Promise<Comment> => {
  const authState = useAuthStore.getState();
  if (!authState.user?.id) {
    throw new Error("로그인이 필요합니다.");
  }

  const requestData = {
    reviewId: data.reviewId,
    userId: authState.user.id,
    content: data.content
  };

  return await apiClient.post<Comment>("/api/comments", requestData);
};

export const updateComment = async (data: {
  commentId: string;
  content: string;
}): Promise<Comment> => {
  const authState = useAuthStore.getState();
  if (!authState.user?.id) {
    throw new Error("로그인이 필요합니다.");
  }

  const requestData = {
    content: data.content
  };

  return await apiClient.patch<Comment>(
      `/api/comments/${data.commentId}`,
      requestData,
      {
        headers: {
          "Deokhugam-Request-User-ID": authState.user.id
        }
      }
  );
};

export const deleteComment = async (commentId: string): Promise<void> => {
  const authState = useAuthStore.getState();
  if (!authState.user?.id) {
    throw new Error("로그인이 필요합니다.");
  }

  return await apiClient.delete<void>(`/api/comments/${commentId}`);
};
