"use client";

import { useRef, useState, useCallback, useEffect } from "react";
import axios from "axios";
import { uploadMedia } from "@/lib/api/media";

type UploadState = "idle" | "uploading" | "error";

export interface UploadedMedia {
  id: number;
  url: string;
}

interface Props {
  /**
   * Gösterilecek mevcut görsel URL'i (sunucudan gelen — ID bilinmeyebilir).
   * Yeni bir yükleme yapılınca bu değer onChange ile güncellenir.
   */
  displayUrl: string | null;
  /** Yükleme tamamlanınca veya görsel kaldırılınca çağrılır. */
  onChange: (media: UploadedMedia | null) => void;
}

const ACCEPTED = ["image/jpeg", "image/png", "image/webp", "image/gif"];
const MAX_BYTES = 5 * 1024 * 1024; // 5 MB

export default function ImageUploader({ displayUrl, onChange }: Props) {
  const inputRef = useRef<HTMLInputElement>(null);

  const [uploadState, setUploadState] = useState<UploadState>("idle");
  const [uploadError, setUploadError] = useState<string | null>(null);
  const [localPreview, setLocalPreview] = useState<string | null>(null);
  const [pendingFile, setPendingFile] = useState<File | null>(null);
  const [isDragging, setIsDragging] = useState(false);

  // Blob URL'leri temizle — bellek sızıntısını önle
  useEffect(() => {
    return () => {
      if (localPreview) URL.revokeObjectURL(localPreview);
    };
  }, [localPreview]);

  // Gösterilecek görsel: yükleme/hata varsa yerel preview, yoksa sunucu URL'i
  const shownSrc = localPreview ?? displayUrl;

  // ── Dosya doğrulama ──────────────────────────────────────────
  function validate(file: File): string | null {
    if (!ACCEPTED.includes(file.type))
      return "Sadece JPEG, PNG, WebP veya GIF yüklenebilir.";
    if (file.size > MAX_BYTES)
      return "Dosya boyutu 5 MB'ı geçemez.";
    return null;
  }

  // ── Yükleme ──────────────────────────────────────────────────
  const handleFile = useCallback(async (file: File) => {
    const validationError = validate(file);
    if (validationError) {
      setUploadError(validationError);
      setUploadState("error");
      return;
    }

    // Yerel önizlemeyi hemen göster
    if (localPreview) URL.revokeObjectURL(localPreview);
    const objectUrl = URL.createObjectURL(file);
    setLocalPreview(objectUrl);
    setPendingFile(file);
    setUploadState("uploading");
    setUploadError(null);

    try {
      const result = await uploadMedia(file);
      onChange({ id: result.id, url: result.url });
      URL.revokeObjectURL(objectUrl);
      setLocalPreview(null);
      setPendingFile(null);
      setUploadState("idle");
    } catch (err: unknown) {
      const msg = axios.isAxiosError(err)
        ? (err.response?.data?.message ?? "Yükleme başarısız. Tekrar deneyin.")
        : "Yükleme başarısız. Tekrar deneyin.";
      setUploadError(msg);
      setUploadState("error");
    }
  }, [localPreview, onChange]);

  // ── Retry ────────────────────────────────────────────────────
  async function handleRetry() {
    if (pendingFile) await handleFile(pendingFile);
  }

  // ── Kaldır ──────────────────────────────────────────────────
  function handleRemove() {
    if (localPreview) URL.revokeObjectURL(localPreview);
    setLocalPreview(null);
    setPendingFile(null);
    setUploadState("idle");
    setUploadError(null);
    onChange(null);
    if (inputRef.current) inputRef.current.value = "";
  }

  // ── Drag & Drop ──────────────────────────────────────────────
  function handleDragOver(e: React.DragEvent) {
    e.preventDefault();
    setIsDragging(true);
  }
  function handleDragLeave() {
    setIsDragging(false);
  }
  function handleDrop(e: React.DragEvent) {
    e.preventDefault();
    setIsDragging(false);
    const file = e.dataTransfer.files[0];
    if (file) handleFile(file);
  }

  // ── Render ───────────────────────────────────────────────────

  // Görsel var (yerel veya sunucu)
  if (shownSrc) {
    return (
      <div className="space-y-2">
        {/* Önizleme */}
        <div className="relative aspect-video w-full max-w-sm overflow-hidden bg-surface-muted border border-border">
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img
            src={shownSrc}
            alt="Kapak görseli"
            className="w-full h-full object-cover"
          />

          {/* Yükleniyor overlay */}
          {uploadState === "uploading" && (
            <div className="absolute inset-0 bg-black/40 flex items-center justify-center">
              <div className="text-center space-y-2">
                <div className="w-6 h-6 border-2 border-white border-t-transparent rounded-full animate-spin mx-auto" />
                <p className="font-sans text-xs text-white">Yükleniyor…</p>
              </div>
            </div>
          )}
        </div>

        {/* Hata mesajı */}
        {uploadState === "error" && uploadError && (
          <div className="flex items-center gap-3">
            <p className="font-sans text-xs text-red-600 flex-1">{uploadError}</p>
            <button
              type="button"
              onClick={handleRetry}
              className="font-sans text-xs text-accent hover:text-accent-hover transition-colors shrink-0"
            >
              Tekrar Dene
            </button>
          </div>
        )}

        {/* Aksiyon butonları */}
        {uploadState !== "uploading" && (
          <div className="flex gap-4">
            <button
              type="button"
              onClick={() => inputRef.current?.click()}
              className="font-sans text-xs text-accent hover:text-accent-hover transition-colors"
            >
              Değiştir
            </button>
            <button
              type="button"
              onClick={handleRemove}
              className="font-sans text-xs text-muted hover:text-red-600 transition-colors"
            >
              Kaldır
            </button>
          </div>
        )}

        <input
          ref={inputRef}
          type="file"
          accept={ACCEPTED.join(",")}
          className="hidden"
          onChange={(e) => {
            const file = e.target.files?.[0];
            if (file) handleFile(file);
          }}
        />
      </div>
    );
  }

  // Boş durum — drag & drop alanı
  return (
    <div className="space-y-2">
      <button
        type="button"
        onClick={() => inputRef.current?.click()}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        className={`
          w-full max-w-sm aspect-video border-2 border-dashed
          flex flex-col items-center justify-center gap-2
          transition-colors cursor-pointer
          ${isDragging
            ? "border-accent bg-accent/5"
            : "border-border hover:border-accent/50 hover:bg-surface-muted/50"
          }
        `}
      >
        {/* Upload ikonu */}
        <svg
          width="24" height="24" viewBox="0 0 24 24" fill="none"
          className={`transition-colors ${isDragging ? "text-accent" : "text-muted"}`}
        >
          <path
            d="M12 16V8M12 8l-3 3M12 8l3 3M20 16.5A4.5 4.5 0 0016.5 12H15a7 7 0 10-9.9 6.4"
            stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"
          />
        </svg>

        <div className="text-center">
          <p className="font-sans text-sm text-foreground">
            {isDragging ? "Bırakın" : "Görsel seçin veya sürükleyin"}
          </p>
          <p className="font-sans text-xs text-muted mt-0.5">
            JPEG, PNG, WebP · maks. 5 MB
          </p>
        </div>
      </button>

      {/* Validasyon hatası (dosya seçilmeden önce) */}
      {uploadState === "error" && uploadError && (
        <p className="font-sans text-xs text-red-600">{uploadError}</p>
      )}

      <input
        ref={inputRef}
        type="file"
        accept={ACCEPTED.join(",")}
        className="hidden"
        onChange={(e) => {
          const file = e.target.files?.[0];
          if (file) handleFile(file);
        }}
      />
    </div>
  );
}