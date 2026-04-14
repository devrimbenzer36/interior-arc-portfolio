"use client";

import { useEffect, useState } from "react";
import { tokenStore } from "@/lib/auth/token";
import { adminGetProjects } from "@/lib/api/projects";
import { adminGetMessages } from "@/lib/api/contact";

interface Stat {
  label: string;
  value: number | string;
}

export default function DashboardOverview() {
  const [stats, setStats] = useState<Stat[]>([]);
  const [loading, setLoading] = useState(true);
  const [apiError, setApiError] = useState(false);
  const email = tokenStore.getEmail();

  useEffect(() => {
    async function load() {
      try {
        const [projects, messages] = await Promise.all([
          adminGetProjects(0, 1),
          adminGetMessages(0, 1),
        ]);

        setStats([
          { label: "Toplam Proje", value: projects.totalElements },
          { label: "Yeni Mesaj", value: messages.totalElements },
        ]);
      } catch (err) {
        // 401 → client.ts interceptor zaten /admin/login'e yönlendirir
        // Diğer hatalar → backend kapalı veya ulaşılamaz
        if (process.env.NODE_ENV === "development") {
          console.error("[DashboardOverview] API hatası:", err);
        }
        setApiError(true);
      } finally {
        setLoading(false);
      }
    }

    load();
  }, []);

  return (
    <div className="space-y-8">
      {/* Başlık */}
      <div>
        <h1 className="font-serif text-3xl text-foreground">Genel Bakış</h1>
        {email && (
          <p className="font-sans text-sm text-muted mt-1">{email}</p>
        )}
      </div>

      {/* İstatistik kartları */}
      {apiError ? (
        <p className="font-sans text-sm text-muted">
          API&apos;ye ulaşılamıyor. Backend çalışıyor mu?
        </p>
      ) : (
        <div className="grid grid-cols-2 gap-4 max-w-sm">
          {loading
            ? [1, 2].map((i) => (
                <div
                  key={i}
                  className="bg-surface border border-border p-5 animate-pulse"
                >
                  <div className="h-7 w-10 bg-border rounded mb-2" />
                  <div className="h-3 w-20 bg-border rounded" />
                </div>
              ))
            : stats.map(({ label, value }) => (
                <div
                  key={label}
                  className="bg-surface border border-border p-5"
                >
                  <p className="font-serif text-3xl text-foreground">{value}</p>
                  <p className="font-sans text-xs text-muted tracking-wider uppercase mt-1">
                    {label}
                  </p>
                </div>
              ))}
        </div>
      )}
    </div>
  );
}
