import Nav from "@/components/public/Nav";
import ProjectCard from "@/components/public/ProjectCard";
import { getPublishedProjects } from "@/lib/api/projects";

export const revalidate = 300;

export const metadata = {
  title: "Projeler",
  description: "Tüm interior design projelerimiz.",
};

export default async function ProjectsPage() {
  const projects = await getPublishedProjects().catch(() => []);

  return (
    <>
      <Nav />

      <main className="max-w-6xl mx-auto px-6 pt-32 pb-24">
        {/* Sayfa başlığı */}
        <div className="mb-16">
          <p className="font-sans text-xs text-muted tracking-[0.2em] uppercase mb-3">
            Çalışmalar
          </p>
          <h1 className="font-serif text-5xl text-foreground">Projeler</h1>
        </div>

        {projects.length === 0 ? (
          <div className="py-32 text-center">
            <div className="w-8 h-px bg-border mx-auto mb-6" />
            <p className="font-sans text-sm text-muted">
              Henüz yayınlanmış proje yok.
            </p>
          </div>
        ) : (
          <>
            {/* Proje sayısı */}
            <p className="font-sans text-xs text-muted mb-10">
              {projects.length} proje
            </p>

            {/* Grid — 2 sütun, geniş görseller için ideal */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-16">
              {projects.map((project, i) => (
                <ProjectCard
                  key={project.id}
                  project={project}
                  // İlk kart dikey (tall) göstererek grid'e ritim katar
                  aspect={i === 0 ? "tall" : "normal"}
                />
              ))}
            </div>
          </>
        )}
      </main>
    </>
  );
}
