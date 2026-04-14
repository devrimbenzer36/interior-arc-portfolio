import { notFound } from "next/navigation";
import Link from "next/link";
import Nav from "@/components/public/Nav";
import ClickableCover from "@/components/public/ClickableCover";
import GalleryGrid from "@/components/public/GalleryGrid";
import { getProjectBySlug, getPublishedProjects } from "@/lib/api/projects";
import type { LightboxImage } from "@/components/public/Lightbox";

// On-demand revalidation başarısız olursa en fazla 5 dakika eski veri gösterilir
export const revalidate = 300;

export async function generateStaticParams() {
  const projects = await getPublishedProjects().catch(() => []);
  return projects.map((p) => ({ slug: p.slug }));
}

export async function generateMetadata({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug } = await params;
  const project = await getProjectBySlug(slug).catch(() => null);
  if (!project) return {};
  return {
    title: project.title,
    description: project.shortDesc ?? undefined,
    openGraph: project.coverImageUrl
      ? { images: [{ url: project.coverImageUrl }] }
      : undefined,
  };
}

export default async function ProjectDetailPage({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug } = await params;
  const project = await getProjectBySlug(slug).catch(() => null);

  if (!project) notFound();

  const date = new Date(project.createdAt).toLocaleDateString("tr-TR", {
    year: "numeric",
    month: "long",
  });

  const galleryImages = project.images ?? [];

  /* ── Lightbox'ta gezilecek tüm görseller:
        index 0 = kapak, index 1+ = galeri
  ──────────────────────────────────────────── */
  const allImages: LightboxImage[] = [
    ...(project.coverImageUrl
      ? [{ src: project.coverImageUrl, alt: project.title }]
      : []),
    ...galleryImages.map((img) => ({
      src: img.url,
      alt: img.altText ?? project.title,
      caption: img.caption ?? undefined,
    })),
  ];
  const galleryOffset = project.coverImageUrl ? 1 : 0;

  return (
    <>
      <Nav />

      {/* pt-16 sm:pt-20 → nav yüksekliğiyle eşleşir */}
      <main className="pt-16 sm:pt-20">

        {/* ── Kapak görseli — tıklanabilir, Lightbox açar ── */}
        {project.coverImageUrl ? (
          <ClickableCover
            coverUrl={project.coverImageUrl}
            coverAlt={project.title}
            allImages={allImages}
          />
        ) : (
          <div className="w-full h-64 bg-surface-muted flex items-center justify-center">
            <span className="font-serif text-8xl text-border select-none">
              {project.title.charAt(0).toUpperCase()}
            </span>
          </div>
        )}

        {/* ── Proje başlık + meta + açıklama ── */}
        <div className="max-w-3xl mx-auto px-6 py-16">
          <Link
            href="/projects"
            className="inline-flex items-center gap-2 font-sans text-xs text-muted hover:text-foreground tracking-wide transition-colors mb-12"
          >
            ← Projelere Dön
          </Link>

          <h1 className="font-serif text-5xl lg:text-6xl text-foreground leading-tight mb-6">
            {project.title}
          </h1>

          <div className="flex flex-wrap items-center gap-6 mb-12">
            <span className="font-sans text-xs text-muted tracking-wide">{date}</span>
            <div className="w-px h-4 bg-border" />
            <span className="font-sans text-xs text-muted tracking-wide">
              INTERIOR DESIGN
            </span>
            {project.location && (
              <>
                <div className="w-px h-4 bg-border" />
                <span className="font-sans text-xs text-muted tracking-wide">
                  {project.location}
                </span>
              </>
            )}
          </div>

          <div className="w-12 h-px bg-accent mb-12" />

          {project.shortDesc ? (
            <p className="font-sans text-base text-foreground leading-relaxed whitespace-pre-wrap">
              {project.shortDesc}
            </p>
          ) : (
            <p className="font-sans text-sm text-muted italic">
              Bu proje için henüz açıklama eklenmemiş.
            </p>
          )}
        </div>

        {/* ── Galeri — tıklanabilir, Lightbox + ← → ile gezilebilir ── */}
        {galleryImages.length > 0 && (
          <GalleryGrid
            images={galleryImages}
            projectTitle={project.title}
            allImages={allImages}
            galleryOffset={galleryOffset}
          />
        )}

        {/* ── Alt navigasyon ── */}
        <div className="border-t border-border">
          <div className="max-w-6xl mx-auto px-6 py-8 flex justify-between items-center">
            <Link
              href="/projects"
              className="font-sans text-xs text-muted hover:text-foreground tracking-wide transition-colors"
            >
              ← Tüm Projeler
            </Link>
            <div className="w-8 h-px bg-accent" />
          </div>
        </div>

      </main>
    </>
  );
}
