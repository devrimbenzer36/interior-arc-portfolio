"use client";

import { useRouter } from "next/navigation";
import Link from "next/link";
import ProjectForm, { type ProjectFormPayload } from "@/components/admin/projects/ProjectForm";
import { adminCreateProject } from "@/lib/api/projects";
import { revalidateProjectCache } from "@/lib/revalidate";

export default function NewProjectPage() {
  const router = useRouter();

  async function handleSubmit(payload: ProjectFormPayload) {
    const project = await adminCreateProject({
      title:       payload.title,
      slug:        payload.slug,
      shortDesc:   payload.shortDesc,
      category:    payload.category,
      coverImageId: payload.newCoverMedia?.id,
    });

    void project;

    // Public sayfaların önbelleğini temizle — yeni proje hemen görünsün
    await revalidateProjectCache();
    router.push("/admin/projects");
  }

  return (
    <div className="space-y-6">
      <Link
        href="/admin/projects"
        className="inline-block font-sans text-sm text-muted hover:text-foreground transition-colors"
      >
        ← Projelere dön
      </Link>

      <h1 className="font-serif text-3xl text-foreground">Yeni Proje</h1>

      <div className="bg-surface border border-border p-6">
        <ProjectForm onSubmit={handleSubmit} submitLabel="Oluştur" />
      </div>
    </div>
  );
}