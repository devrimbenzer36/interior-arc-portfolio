"use client";

import { useState, useEffect } from "react";
import axios from "axios";
import ImageUploader, { type UploadedMedia } from "@/components/admin/ui/ImageUploader";
import type { ProjectCategory, CreateProjectRequest, UpdateProjectRequest } from "@/types/api";

// Türkçe karakter desteğiyle URL-safe slug üret
function toSlug(value: string): string {
  return value
    .toLowerCase()
    .replace(/[çÇ]/g, "c").replace(/[şŞ]/g, "s").replace(/[ğĞ]/g, "g")
    .replace(/[üÜ]/g, "u").replace(/[ıİ]/g, "i").replace(/[öÖ]/g, "o")
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
}

const CATEGORIES: { value: ProjectCategory; label: string }[] = [
  { value: "RESIDENTIAL",  label: "Konut" },
  { value: "COMMERCIAL",   label: "Ticari" },
  { value: "HOSPITALITY",  label: "Otel / Restoran / Kafe" },
  { value: "OFFICE",       label: "Ofis" },
  { value: "RETAIL",       label: "Mağaza / Showroom" },
  { value: "OTHER",        label: "Diğer" },
];

interface FormValues {
  title:    string;
  slug:     string;
  shortDesc: string;
  category: ProjectCategory | "";
}

/** Formun çağırana ilettiği payload — create ve update için ortak */
export interface ProjectFormPayload {
  title:        string;
  slug:         string;
  shortDesc?:   string;
  category:     ProjectCategory;
  /** Yeni görsel yüklenirse dolar; mevcut görsel korunacaksa undefined */
  newCoverMedia?: UploadedMedia;
}

interface Props {
  initialValues?: {
    title?:        string;
    slug?:         string;
    shortDesc?:    string | null;
    category?:     ProjectCategory;
    coverImageUrl?: string | null;
  };
  onSubmit: (payload: ProjectFormPayload) => Promise<void>;
  submitLabel: string;
}

export default function ProjectForm({ initialValues, onSubmit, submitLabel }: Props) {
  const [values, setValues] = useState<FormValues>({
    title:     initialValues?.title     ?? "",
    slug:      initialValues?.slug      ?? "",
    shortDesc: initialValues?.shortDesc ?? "",
    category:  initialValues?.category  ?? "",
  });

  // Yeni yüklenen medya (id + url). null = kaldırıldı, undefined = değişmedi
  const [newCoverMedia, setNewCoverMedia] = useState<UploadedMedia | null | undefined>(
    undefined
  );

  // Gösterilecek URL: yeni yüklenmediyse initialValues'tan al
  const displayUrl =
    newCoverMedia === undefined
      ? (initialValues?.coverImageUrl ?? null)
      : newCoverMedia?.url ?? null;

  const [slugLocked, setSlugLocked] = useState(!!initialValues?.slug);
  const [errors, setErrors] = useState<Partial<Record<keyof FormValues, string>>>({});
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  // Title değişince slug'ı otomatik güncelle (kilitlemediyse)
  useEffect(() => {
    if (!slugLocked) {
      setValues((v) => ({ ...v, slug: toSlug(v.title) }));
    }
  }, [values.title, slugLocked]);

  function set<K extends keyof FormValues>(key: K, value: FormValues[K]) {
    setValues((v) => ({ ...v, [key]: value }));
    if (errors[key]) setErrors((e) => ({ ...e, [key]: undefined }));
  }

  function validate(): boolean {
    const errs: Partial<Record<keyof FormValues, string>> = {};
    if (!values.title.trim())
      errs.title = "Başlık zorunlu.";
    if (!values.slug.trim())
      errs.slug = "Slug zorunlu.";
    else if (!/^[a-z0-9-]+$/.test(values.slug))
      errs.slug = "Sadece küçük harf, rakam ve tire kullanılabilir.";
    if (!values.category)
      errs.category = "Kategori zorunlu.";
    setErrors(errs);
    return Object.keys(errs).length === 0;
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!validate()) return;
    setSubmitting(true);
    setSubmitError(null);

    try {
      await onSubmit({
        title:    values.title.trim(),
        slug:     values.slug.trim(),
        shortDesc: values.shortDesc.trim() || undefined,
        category:  values.category as ProjectCategory,
        newCoverMedia: newCoverMedia === undefined ? undefined : (newCoverMedia ?? undefined),
      });
    } catch (err: unknown) {
      const msg = axios.isAxiosError(err)
        ? (err.response?.data?.message ?? "İşlem başarısız. Tekrar deneyin.")
        : err instanceof Error
        ? err.message
        : "İşlem başarısız.";
      setSubmitError(msg);
    } finally {
      setSubmitting(false);
    }
  }

  const inputCls =
    "w-full bg-surface border border-border px-4 py-2.5 font-sans text-sm text-foreground focus:outline-none focus:border-accent transition-colors";

  return (
    <form onSubmit={handleSubmit} className="space-y-6 max-w-lg">

      {/* Başlık */}
      <div className="space-y-1.5">
        <label className="block font-sans text-xs text-muted tracking-wider uppercase">
          Başlık <span className="text-red-500">*</span>
        </label>
        <input
          type="text"
          value={values.title}
          onChange={(e) => set("title", e.target.value)}
          className={inputCls}
          placeholder="Örn: Modern Mutfak Yenileme"
        />
        {errors.title && <p className="font-sans text-xs text-red-600">{errors.title}</p>}
      </div>

      {/* Slug */}
      <div className="space-y-1.5">
        <label className="block font-sans text-xs text-muted tracking-wider uppercase">
          Slug <span className="text-red-500">*</span>
        </label>
        <input
          type="text"
          value={values.slug}
          onChange={(e) => { setSlugLocked(true); set("slug", e.target.value); }}
          className={inputCls}
          placeholder="modern-mutfak-yenileme"
        />
        {errors.slug
          ? <p className="font-sans text-xs text-red-600">{errors.slug}</p>
          : <p className="font-sans text-xs text-muted">URL'de kullanılır. Otomatik türetilir, düzenleyebilirsiniz.</p>
        }
      </div>

      {/* Kategori */}
      <div className="space-y-1.5">
        <label className="block font-sans text-xs text-muted tracking-wider uppercase">
          Kategori <span className="text-red-500">*</span>
        </label>
        <select
          value={values.category}
          onChange={(e) => set("category", e.target.value as ProjectCategory)}
          className={inputCls}
        >
          <option value="">Seçin…</option>
          {CATEGORIES.map((c) => (
            <option key={c.value} value={c.value}>{c.label}</option>
          ))}
        </select>
        {errors.category && <p className="font-sans text-xs text-red-600">{errors.category}</p>}
      </div>

      {/* Kapak Görseli */}
      <div className="space-y-1.5">
        <label className="block font-sans text-xs text-muted tracking-wider uppercase">
          Kapak Görseli
        </label>
        <ImageUploader
          displayUrl={displayUrl}
          onChange={(media) => setNewCoverMedia(media)}
        />
      </div>

      {/* Açıklama */}
      <div className="space-y-1.5">
        <label className="block font-sans text-xs text-muted tracking-wider uppercase">
          Kısa Açıklama
        </label>
        <textarea
          value={values.shortDesc}
          onChange={(e) => set("shortDesc", e.target.value)}
          rows={4}
          className={`${inputCls} resize-none`}
          placeholder="Proje hakkında kısa açıklama…"
          maxLength={500}
        />
      </div>

      {submitError && (
        <p className="font-sans text-xs text-red-600">{submitError}</p>
      )}

      <button
        type="submit"
        disabled={submitting}
        className="bg-accent text-white font-sans text-xs tracking-widest uppercase px-6 py-3 hover:bg-accent-hover disabled:opacity-50 transition-colors"
      >
        {submitting ? "Kaydediliyor…" : submitLabel}
      </button>
    </form>
  );
}