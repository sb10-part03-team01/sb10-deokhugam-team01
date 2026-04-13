import {apiClient} from "./client.ts";

export interface PowerUser {
  userId: string;
  nickname: string;
  period: "DAILY" | "WEEKLY" | "MONTHLY" | "ALL_TIME";
  createdAt: string;
  rank: number;
  score: number;
  reviewScoreSum: number;
  likeCount: number;
  commentCount: number;
}

export interface PowerUsersResponse {
  content: PowerUser[];
  nextCursor: string;
  nextAfter: string;
  size: number;
  totalElements: number;
  hasNext: boolean;
}

export interface PowerUsersParams {
  period?: "DAILY" | "WEEKLY" | "MONTHLY" | "ALL_TIME";
  direction?: "ASC" | "DESC";
  cursor?: string;
  after?: string;
  limit?: number;
}

export const getPowerUsers = async (
    params: PowerUsersParams = {}
): Promise<PowerUsersResponse> => {
  const searchParams = new URLSearchParams();

  if (params.period) searchParams.append("period", params.period);
  if (params.direction) searchParams.append("direction", params.direction);
  if (params.cursor) searchParams.append("cursor", params.cursor);
  if (params.after) searchParams.append("after", params.after);
  if (params.limit) searchParams.append("limit", params.limit.toString());

  const response = await apiClient.get<PowerUsersResponse>(
      `/api/users/power?${searchParams.toString()}`
  );
  return response;
};
