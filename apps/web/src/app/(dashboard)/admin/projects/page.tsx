"use client";

import { useEffect, useState, useCallback } from "react";
import Link from "next/link";
import { adminGetProjects, adminDeleteProject, adminPublishProject, adminUnpublishProject } from "@/lib/api/projects";
import ProjectsTable from "@/components/admin/projects/ProjectsTable";
import type { Project, PageResponse } from "@/types/api";

const PAGE_SIZE = 15;

export default function ProjectsPage() {
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<Project> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const load = useCallback(async (p: number) => {
    setLoading(true);
    setError(false);
    try {
      setData(await adminGetProjects(p, PAGE_SIZE));
    } catch {
      setError(true);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(page); }, [page, load]);

  async function handleDelete(id: number) {
    await adminDeleteProject(id);
    // Silinen son öğeyse bir önceki sayfaya düş
    const remaining = (data?.content.length ?? 1) - 1;
    const targetPage = remaining === 0 && page > 0 ? page - 1 : page;
    await load(targetPage);
    setPage(targetPage);
  }

  return (
    <div className="space-y-6">
      {/* Başlık */}
      <div className="flex items-center justify-between">
        <h1 className="font-serif text-3xl text-foreground">Projeler</h1>
        <Link
          href="/admin/projects/new"
          className="bg-accent text-white font-sans text-xs tracking-widest uppercase px-4 py-2.5 hover:bg-accent-hover transition-colors"
        >
          + Yeni Proje
        </Link>
      </div>

      {/* Tablo kartı */}
      <div className="bg-surface border border-border p-6">
        {loading ? (
          <div className="space-y-3">
            {Array.from({ length: 5 }).map((_, i) => (
              <div key={i} className="h-12 bg-surface-muted animate-pulse" />
            ))}
          </div>
        ) : error ? (
          <div className="py-8 text-center space-y-3">
            <p className="font-sans text-sm text-muted">Projeler yüklenemedi.</p>
            <button
              onClick={() => load(page)}
              className="font-sans text-xs text-accent hover:text-accent-hover transition-colors"
            >
              Tekrar dene
            </button>
          </div>
        ) : (
          <ProjectsTable
            projects={data?.content ?? []}
            onDelete={handleDelete}
            onPublish={async (id) => { await adminPublishProject(id); await load(page); }}
            onUnpublish={async (id) => { await adminUnpublishProject(id); await load(page); }}
          />
        )}
      </div>

      {/* Pagination — sadece birden fazla sayfa varsa göster */}
      {data && data.totalPages > 1 && (
        <div className="flex items-center justify-between">
          <p className="font-sans text-xs text-muted">
            Toplam {data.totalElements} proje
          </p>
          <div className="flex items-center gap-2">
            <button
              disabled={data.first}
              onClick={() => setPage((p) => p - 1)}
              className="px-3 py-1.5 font-sans text-xs border border-border text-muted hover:text-foreground disabled:opacity-40 transition-colors"
            >
              ← Önceki
            </button>
            <span className="font-sans text-xs text-muted px-2">
              {data.number + 1} / {data.totalPages}
            </span>
            <button
              disabled={data.last}
              onClick={() => setPage((p) => p + 1)}
              className="px-3 py-1.5 font-sans text-xs border border-border text-muted hover:text-foreground disabled:opacity-40 transition-colors"
            >
              Sonraki →
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
