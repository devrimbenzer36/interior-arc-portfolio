"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import ProjectForm, { type ProjectFormPayload } from "@/components/admin/projects/ProjectForm";
import { adminGetProjectById, adminUpdateProject, adminSetCoverImage } from "@/lib/api/projects";
import type { ProjectDetail } from "@/types/api";

export default function EditProjectPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const projectId = Number(id);

  const [project, setProject] = useState<ProjectDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    if (!projectId) { setNotFound(true); setLoading(false); return; }

    adminGetProjectById(projectId)
      .then(setProject)
      .catch(() => setNotFound(true))
      .finally(() => setLoading(false));
  }, [projectId]);

  async function handleSubmit(payload: ProjectFormPayload) {
    await adminUpdateProject(projectId, {
      title:    payload.title,
      slug:     payload.slug,
      shortDesc: payload.shortDesc,
      category: payload.category,
    });

    // Yeni kapak görseli yüklendiyse ayrı endpoint ile bağla
    if (payload.newCoverMedia) {
      await adminSetCoverImage(projectId, payload.newCoverMedia.id);
    }

    router.push("/admin/projects");
  }

  if (loading) {
    return (
      <div className="space-y-4 max-w-lg">
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="h-10 bg-surface-muted animate-pulse" />
        ))}
      </div>
    );
  }

  if (notFound || !project) {
    return (
      <div className="space-y-4">
        <p className="font-sans text-sm text-muted">Proje bulunamadı.</p>
        <Link
          href="/admin/projects"
          className="font-sans text-sm text-accent hover:text-accent-hover transition-colors"
        >
          ← Projelere dön
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <Link
        href="/admin/projects"
        className="inline-block font-sans text-sm text-muted hover:text-foreground transition-colors"
      >
        ← Projelere dön
      </Link>

      <h1 className="font-serif text-3xl text-foreground">Proje Düzenle</h1>

      <div className="bg-surface border border-border p-6">
        <ProjectForm
          initialValues={project}
          onSubmit={handleSubmit}
          submitLabel="Kaydet"
        />
      </div>
    </div>
  );
}