import { apiClient } from "./client";
import type {
  ApiResponse,
  PageResponse,
  Project,
  ProjectDetail,
  CreateProjectRequest,
  UpdateProjectRequest,
} from "@/types/api";

export async function getPublishedProjects(): Promise<Project[]> {
  const { data } = await apiClient.get<ApiResponse<PageResponse<Project>>>(
    "/api/v1/projects"
  );
  return data.data?.content ?? [];
}

export async function getProjectBySlug(slug: string): Promise<ProjectDetail> {
  const { data } = await apiClient.get<ApiResponse<ProjectDetail>>(
    `/api/v1/projects/${slug}`
  );
  if (!data.data) throw new Error("Project not found");
  return data.data;
}

// ── Admin endpoints ───────────────────────────────────────────

export async function adminGetProjects(
  page = 0,
  size = 20
): Promise<PageResponse<Project>> {
  const { data } = await apiClient.get<ApiResponse<PageResponse<Project>>>(
    "/api/v1/admin/projects",
    { params: { page, size } }
  );
  if (!data.data) throw new Error("Failed to load projects");
  return data.data;
}

export async function adminCreateProject(
  payload: CreateProjectRequest
): Promise<ProjectDetail> {
  const { data } = await apiClient.post<ApiResponse<ProjectDetail>>(
    "/api/v1/admin/projects",
    payload
  );
  if (!data.data) throw new Error(data.message ?? "Create failed");
  return data.data;
}

export async function adminUpdateProject(
  id: number,
  payload: UpdateProjectRequest
): Promise<ProjectDetail> {
  const { data } = await apiClient.patch<ApiResponse<ProjectDetail>>(
    `/api/v1/admin/projects/${id}`,
    payload
  );
  if (!data.data) throw new Error(data.message ?? "Update failed");
  return data.data;
}

export async function adminGetProjectById(id: number): Promise<ProjectDetail> {
  const { data } = await apiClient.get<ApiResponse<ProjectDetail>>(
    `/api/v1/admin/projects/${id}`
  );
  if (!data.data) throw new Error("Project not found");
  return data.data;
}

export async function adminSetCoverImage(
  projectId: number,
  mediaFileId: number
): Promise<void> {
  await apiClient.patch(
    `/api/v1/admin/projects/${projectId}/cover-image/${mediaFileId}`
  );
}

export async function adminDeleteProject(id: number): Promise<void> {
  await apiClient.delete(`/api/v1/admin/projects/${id}`);
}