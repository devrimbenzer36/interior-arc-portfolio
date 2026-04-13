import Link from "next/link";
import type { Project } from "@/types/api";

interface Props {
  project: Project;
  /** "normal" → 4/3 oran | "tall" → 3/4 oran (dikey fotoğraflar için) */
  aspect?: "normal" | "tall";
}

export default function ProjectCard({ project, aspect = "normal" }: Props) {
  const aspectClass = aspect === "tall" ? "aspect-[3/4]" : "aspect-[4/3]";

  return (
    <Link href={`/projects/${project.slug}`} className="group block">
      {/* Görsel alanı */}
      <div className={`${aspectClass} bg-surface-muted overflow-hidden relative`}>
        {project.coverImageUrl ? (
          /* eslint-disable-next-line @next/next/no-img-element */
          <img
            src={project.coverImageUrl}
            alt={project.title}
            className="w-full h-full object-contain group-hover:scale-105 transition-transform duration-700 ease-out"
          />
        ) : (
          /* Görsel yoksa initial placeholder */
          <div className="w-full h-full flex items-center justify-center">
            <span className="font-serif text-5xl text-border select-none">
              {project.title.charAt(0).toUpperCase()}
            </span>
          </div>
        )}
      </div>

      {/* Başlık */}
      <div className="mt-4 space-y-1">
        <h3 className="font-serif text-lg text-foreground group-hover:text-accent transition-colors leading-snug">
          {project.title}
        </h3>
        {project.shortDesc && (
          <p className="font-sans text-xs text-muted line-clamp-2">
            {project.shortDesc}
          </p>
        )}
      </div>
    </Link>
  );
}
