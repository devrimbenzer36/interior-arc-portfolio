import { apiClient } from "./client";
import type { ApiResponse, MediaFileResponse } from "@/types/api";

/**
 * Dosyayı backend'e yükler, MediaFileResponse döner.
 *
 * Content-Type header'ı SET ETME — axios FormData'yı görünce
 * multipart/form-data + boundary'yi otomatik ekler.
 */
export async function uploadMedia(file: File): Promise<MediaFileResponse> {
  const form = new FormData();
  form.append("file", file);

  const { data } = await apiClient.post<ApiResponse<MediaFileResponse>>(
    "/api/v1/admin/media/upload",
    form
  );

  if (!data.data) throw new Error(data.message ?? "Upload failed");
  return data.data;
}

export async function deleteMedia(id: number): Promise<void> {
  await apiClient.delete(`/api/v1/admin/media/${id}`);
}
