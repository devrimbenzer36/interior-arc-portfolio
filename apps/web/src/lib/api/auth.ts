import { apiClient } from "./client";
import type { ApiResponse, LoginRequest, LoginResponse } from "@/types/api";

export async function login(payload: LoginRequest): Promise<LoginResponse> {
  const { data } = await apiClient.post<ApiResponse<LoginResponse>>(
    "/api/v1/auth/login",
    payload
  );

  if (!data.success || !data.data) {
    throw new Error(data.message ?? "Login failed");
  }

  return data.data;
}
