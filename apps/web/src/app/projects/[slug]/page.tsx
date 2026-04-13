import { notFound } from "next/navigation";
import Link from "next/link";
import Nav from "@/components/public/Nav";
import { getProjectBySlug, getPublishedProjects } from "@/lib/api/projects";

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

  return (
    <>
      <Nav />

      <main className="pt-16">

        {/* ── Hero — tam genişlik, oranı korunur ───────────────────── */}
        {project.coverImageUrl ? (
          <div className="w-full bg-surface-muted flex items-center justify-center">
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img
              src={project.coverImageUrl}
              alt={project.title}
              className="w-full max-h-[90vh] object-contain"
            />
          </div>
        ) : (
          <div className="w-full h-64 bg-surface-muted flex items-center justify-center">
            <span className="font-serif text-8xl text-border select-none">
              {project.title.charAt(0).toUpperCase()}
            </span>
          </div>
        )}

        {/* ── Proje başlık + meta ──────────────────────────────────── */}
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
            <span className="font-sans text-xs text-muted tracking-wide uppercase">
              Interior Design
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

        {/* ── Galeri ──────────────────────────────────────────────── */}
        {galleryImages.length > 0 && (
          <section className="max-w-6xl mx-auto px-6 pb-24">
            <div className="w-8 h-px bg-accent mb-12" />

            {/* Masonry-style: CSS columns */}
            <div className="columns-1 sm:columns-2 lg:columns-3 gap-4 space-y-4">
              {galleryImages.map((img) => (
                <div
                  key={img.id}
                  className="break-inside-avoid bg-surface-muted overflow-hidden"
                >
                  {/* eslint-disable-next-line @next/next/no-img-element */}
                  <img
                    src={img.url}
                    alt={img.altText ?? project.title}
                    className="w-full h-auto block"
                  />
                  {img.caption && (
                    <p className="font-sans text-xs text-muted px-3 py-2">
                      {img.caption}
                    </p>
                  )}
                </div>
              ))}
            </div>
          </section>
        )}

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
