"use client";

import { useRef, useState } from "react";
import { uploadMedia } from "@/lib/api/media";
import { adminAddProjectImage, adminRemoveProjectImage } from "@/lib/api/projects";
import type { ProjectImageData } from "@/types/api";

interface Props {
  projectId: number;
  images: ProjectImageData[];
  onUpdate: () => void;
}

export default function ProjectGallery({ projectId, images, onUpdate }: Props) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [uploading, setUploading] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const [removingId, setRemovingId] = useState<number | null>(null);
  const [isDragging, setIsDragging] = useState(false);

  function handleDragOver(e: React.DragEvent) {
    e.preventDefault();
    setIsDragging(true);
  }
  function handleDragLeave(e: React.DragEvent) {
    // Sadece container'dan çıkınca false yap, child'a geçince değil
    if (!e.currentTarget.contains(e.relatedTarget as Node)) {
      setIsDragging(false);
    }
  }
  function handleDrop(e: React.DragEvent) {
    e.preventDefault();
    setIsDragging(false);
    handleFiles(e.dataTransfer.files);
  }

  async function handleFiles(files: FileList | null) {
    if (!files || files.length === 0) return;
    setUploading(true);
    setUploadError(null);
    try {
      for (const file of Array.from(files)) {
        const media = await uploadMedia(file);
        await adminAddProjectImage(projectId, media.id);
      }
      onUpdate();
    } catch {
      setUploadError("Yükleme başarısız. Tekrar deneyin.");
    } finally {
      setUploading(false);
      if (inputRef.current) inputRef.current.value = "";
    }
  }

  async function handleRemove(imageId: number) {
    setRemovingId(imageId);
    try {
      await adminRemoveProjectImage(projectId, imageId);
      onUpdate();
    } finally {
      setRemovingId(null);
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <p className="font-sans text-xs text-muted tracking-wider uppercase">
            Proje Görselleri
          </p>
          <p className="font-sans text-xs text-muted mt-0.5">
            {images.length} görsel · Tüm render ve fotoğraflar
          </p>
        </div>
        <button
          type="button"
          onClick={() => inputRef.current?.click()}
          disabled={uploading}
          className="font-sans text-xs tracking-widest uppercase bg-accent text-white px-4 py-2 hover:bg-accent-hover disabled:opacity-50 transition-colors"
        >
          {uploading ? "Yükleniyor…" : "+ Görsel Ekle"}
        </button>
      </div>

      <input
        ref={inputRef}
        type="file"
        accept="image/jpeg,image/png,image/webp"
        multiple
        className="hidden"
        onChange={(e) => handleFiles(e.target.files)}
      />

      {uploadError && (
        <p className="font-sans text-xs text-red-600">{uploadError}</p>
      )}

      {images.length === 0 ? (
        <div
          onClick={() => inputRef.current?.click()}
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onDrop={handleDrop}
          className={`border-2 border-dashed transition-colors cursor-pointer py-12 flex flex-col items-center gap-2 ${
            isDragging ? "border-accent bg-accent/5" : "border-border hover:border-accent/50"
          }`}
        >
          <p className="font-sans text-sm text-muted">Henüz görsel yok</p>
          <p className="font-sans text-xs text-muted">Tıklayın veya sürükleyerek ekleyin</p>
        </div>
      ) : (
        <div
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onDrop={handleDrop}
          className={`grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3 p-2 border-2 border-dashed transition-colors ${
            isDragging ? "border-accent bg-accent/5" : "border-transparent"
          }`}
        >
          {images.map((img) => (
            <div key={img.id} className="relative group h-40 bg-surface-muted overflow-hidden border border-border flex items-center justify-center">
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img
                src={img.url}
                alt=""
                className="max-w-full max-h-full object-contain"
              />
              <div className="absolute inset-0 bg-black/0 group-hover:bg-black/40 transition-colors flex items-center justify-center">
                <button
                  type="button"
                  onClick={() => handleRemove(img.id)}
                  disabled={removingId === img.id}
                  className="opacity-0 group-hover:opacity-100 transition-opacity font-sans text-xs text-white border border-white/70 px-3 py-1.5 hover:bg-white/20 disabled:opacity-50"
                >
                  {removingId === img.id ? "…" : "Kaldır"}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
