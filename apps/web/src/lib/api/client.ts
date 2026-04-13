import axios from "axios";
import { tokenStore } from "@/lib/auth/token";

const BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export const apiClient = axios.create({
  baseURL: BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// ── Request interceptor — JWT token varsa Authorization header'ına ekle ──
apiClient.interceptors.request.use((config) => {
  // localStorage yalnızca client-side'da mevcut (SSR'da atla)
  if (typeof window !== "undefined") {
    const token = tokenStore.get();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

// ── Response interceptor — 401 gelirse tüm auth verisini temizle ve yönlendir ──
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (
      typeof window !== "undefined" &&
      error.response?.status === 401 &&
      !window.location.pathname.startsWith("/admin/login")
    ) {
      tokenStore.clear(); // token + email ikisini de siler
      window.location.href = "/admin/login";
    }
    return Promise.reject(error);
  }
);
