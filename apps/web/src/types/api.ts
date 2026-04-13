// ─────────────────────────────────────────────────────────────
// Wrapper — tüm backend cevapları bu şablona sarılı gelir
// ─────────────────────────────────────────────────────────────

export interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  message: string | null;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;   // current page (0-indexed)
  size: number;
  first: boolean;
  last: boolean;
}

// ─────────────────────────────────────────────────────────────
// Auth
// ─────────────────────────────────────────────────────────────

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  tokenType: string;    // "Bearer"
  expiresIn: number;    // seconds
  email: string;
  role: string;
}

// ─────────────────────────────────────────────────────────────
// Projects
// ─────────────────────────────────────────────────────────────

export type ProjectStatus = "DRAFT" | "PUBLISHED" | "ARCHIVED";

export type ProjectCategory =
  | "RESIDENTIAL"
  | "COMMERCIAL"
  | "HOSPITALITY"
  | "OFFICE"
  | "RETAIL"
  | "OTHER";

/** ProjectSummaryResponse — liste endpoint'lerinde döner */
export interface Project {
  id: number;
  title: string;
  slug: string;
  shortDesc: string | null;
  category: ProjectCategory;
  spaceType: string | null;
  location: string | null;
  projectDate: string | null;
  status: ProjectStatus;
  featured: boolean;
  coverImageUrl: string | null;
  createdAt: string;
}

export interface ProjectImageData {
  id: number;
  url: string;
  type: string;
  displayOrder: number;
  altText: string | null;
  caption: string | null;
  width: number | null;
  height: number | null;
}

/** ProjectDetailResponse — tek proje endpoint'lerinde döner */
export interface ProjectDetail extends Project {
  detailedStory: string | null;
  style: string | null;
  squareMeters: number | null;
  budgetRange: string | null;
  viewCount: number;
  images: ProjectImageData[];
  tags: string[];
  materials: string[];
  updatedAt: string;
}

export interface CreateProjectRequest {
  title: string;
  slug?: string;
  shortDesc?: string;
  category: ProjectCategory;
  coverImageId?: number;
}

export interface UpdateProjectRequest {
  title?: string;
  slug?: string;
  shortDesc?: string;
  category?: ProjectCategory;
}

// ─────────────────────────────────────────────────────────────
// Media
// ─────────────────────────────────────────────────────────────

export interface MediaFileResponse {
  id: number;
  originalName: string;
  url: string;
  mimeType: string;
  size: number;
  width: number | null;
  height: number | null;
  createdAt: string;
}

// ─────────────────────────────────────────────────────────────
// Contact Messages
// ─────────────────────────────────────────────────────────────

/** ContactStatus — backend enum: NEW | READ | REPLIED */
export type MessageStatus = "NEW" | "READ" | "REPLIED";

export interface ContactMessage {
  id: number;
  fullName: string;
  email: string;
  phone: string | null;
  subject: string | null;
  message: string;
  status: MessageStatus;
  createdAt: string;
}

export interface SendContactMessageRequest {
  fullName: string;
  email: string;
  phone?: string;
  subject?: string;
  message: string;
}