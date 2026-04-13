import { apiClient } from "./client";
import type {
  ApiResponse,
  PageResponse,
  ContactMessage,
  SendContactMessageRequest,
} from "@/types/api";

export async function sendContactMessage(
  payload: SendContactMessageRequest
): Promise<void> {
  await apiClient.post("/api/v1/contact", payload);
}

// ── Admin endpoints ───────────────────────────────────────────

export async function adminGetMessages(
  page = 0,
  size = 20
): Promise<PageResponse<ContactMessage>> {
  const { data } = await apiClient.get<ApiResponse<PageResponse<ContactMessage>>>(
    "/api/v1/admin/contact",
    { params: { page, size } }
  );
  if (!data.data) throw new Error("Failed to load messages");
  return data.data;
}

export async function adminMarkMessageRead(id: number): Promise<void> {
  await apiClient.patch(`/api/v1/admin/contact/${id}/status`, null, {
    params: { status: "READ" },
  });
}