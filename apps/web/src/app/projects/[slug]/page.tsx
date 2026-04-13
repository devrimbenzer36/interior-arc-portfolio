import { notFound } from "next/navigation";
import Link from "next/link";
import Nav from "@/components/public/Nav";
import { getProjectBySlug, getPublishedProjects } from "@/lib/api/projects";

// Static params — build time'da bilinen slug'ları üretir (opsiyonel, SEO için)
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

  return (
    <>
      <Nav />

      <main className="pt-16">
        {/* ── Hero görsel ─────────────────────────────────────────── */}
        {project.coverImageUrl ? (
          <div className="w-full aspect-[16/9] max-h-[80vh] overflow-hidden bg-surface-muted">
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img
              src={project.coverImageUrl}
              alt={project.title}
              className="w-full h-full object-cover"
            />
          </div>
        ) : (
          /* Görsel yoksa büyük placeholder */
          <div className="w-full aspect-[16/9] max-h-[60vh] bg-surface-muted flex items-center justify-center">
            <span className="font-serif text-8xl text-border select-none">
              {project.title.charAt(0).toUpperCase()}
            </span>
          </div>
        )}

        {/* ── İçerik ─────────────────────────────────────────────── */}
        <div className="max-w-3xl mx-auto px-6 py-16">
          {/* Geri link */}
          <Link
            href="/projects"
            className="inline-flex items-center gap-2 font-sans text-xs text-muted hover:text-foreground tracking-wide transition-colors mb-12"
          >
            ← Projelere Dön
          </Link>

          {/* Başlık */}
          <h1 className="font-serif text-5xl lg:text-6xl text-foreground leading-tight mb-6">
            {project.title}
          </h1>

          {/* Meta bilgi */}
          <div className="flex items-center gap-6 mb-12">
            <span className="font-sans text-xs text-muted tracking-wide">{date}</span>
            <div className="w-px h-4 bg-border" />
            <span className="font-sans text-xs text-muted tracking-wide uppercase">
              Interior Design
            </span>
          </div>

          {/* Dekoratif çizgi */}
          <div className="w-12 h-px bg-accent mb-12" />

          {/* Açıklama */}
          {project.shortDesc ? (
            <div className="prose prose-sm max-w-none">
              <p className="font-sans text-base text-foreground leading-relaxed whitespace-pre-wrap">
                {project.shortDesc}
              </p>
            </div>
          ) : (
            <p className="font-sans text-sm text-muted italic">
              Bu proje için henüz açıklama eklenmemiş.
            </p>
          )}
        </div>

        {/* ── Alt navigasyon ──────────────────────────────────────── */}
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
