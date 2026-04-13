"use client";

import { useState } from "react";
import Link from "next/link";
import Badge from "@/components/admin/ui/Badge";
import ConfirmDialog from "@/components/admin/ui/ConfirmDialog";
import type { Project } from "@/types/api";

interface Props {
  projects: Project[];
  onDelete: (id: number) => Promise<void>;
}

export default function ProjectsTable({ projects, onDelete }: Props) {
  const [target, setTarget] = useState<Project | null>(null);
  const [deleting, setDeleting] = useState(false);

  async function handleConfirm() {
    if (!target) return;
    setDeleting(true);
    try {
      await onDelete(target.id);
    } finally {
      setDeleting(false);
      setTarget(null);
    }
  }

  if (projects.length === 0) {
    return (
      <p className="font-sans text-sm text-muted py-10 text-center">
        Henüz proje yok.
      </p>
    );
  }

  return (
    <>
      <table className="w-full">
        <thead>
          <tr className="border-b border-border">
            {["Başlık", "Durum", "Tarih", ""].map((h) => (
              <th
                key={h}
                className="text-left font-sans text-xs text-muted tracking-wider uppercase pb-3 pr-4 last:pr-0"
              >
                {h}
              </th>
            ))}
          </tr>
        </thead>

        <tbody>
          {projects.map((p) => (
            <tr
              key={p.id}
              className="border-b border-border/50 hover:bg-surface-muted/50 transition-colors"
            >
              {/* Başlık + slug */}
              <td className="py-3.5 pr-4">
                <p className="font-sans text-sm text-foreground">{p.title}</p>
                <p className="font-sans text-xs text-muted mt-0.5">{p.slug}</p>
              </td>

              {/* Durum */}
              <td className="py-3.5 pr-4">
                <Badge status={p.status} />
              </td>

              {/* Tarih */}
              <td className="py-3.5 pr-4">
                <p className="font-sans text-xs text-muted">
                  {new Date(p.createdAt).toLocaleDateString("tr-TR")}
                </p>
              </td>

              {/* Eylemler */}
              <td className="py-3.5 text-right">
                <div className="flex items-center justify-end gap-4">
                  <Link
                    href={`/admin/projects/${p.id}/edit`}
                    className="font-sans text-xs text-accent hover:text-accent-hover transition-colors"
                  >
                    Düzenle
                  </Link>
                  <button
                    onClick={() => setTarget(p)}
                    className="font-sans text-xs text-muted hover:text-red-600 transition-colors"
                  >
                    Sil
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {target && (
        <ConfirmDialog
          title="Projeyi sil"
          description={`"${target.title}" kalıcı olarak silinecek. Bu işlem geri alınamaz.`}
          loading={deleting}
          onConfirm={handleConfirm}
          onCancel={() => setTarget(null)}
        />
      )}
    </>
  );
}
