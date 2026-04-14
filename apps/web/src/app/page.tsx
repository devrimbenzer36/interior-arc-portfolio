import Link from "next/link";
import Nav from "@/components/public/Nav";
import ProjectCard from "@/components/public/ProjectCard";
import ContactForm from "@/components/public/ContactForm";
import Logo from "@/components/public/Logo";
import { getPublishedProjects } from "@/lib/api/projects";

export const metadata = {
  title: "Portfolio — Interior Design",
  description: "Mekânları özgün hikayelerle şekillendiriyoruz.",
};

export default async function HomePage() {
  // API kapalıysa sayfa çökmez, boş dizi döner
  const allProjects = await getPublishedProjects().catch(() => []);
  const featured = allProjects.slice(0, 3);

  return (
    <>
      <Nav />

      {/* ── Hero ──────────────────────────────────────────────────── */}
      <section className="relative h-screen flex flex-col items-center justify-center text-center px-6 overflow-hidden">

        {/* ── Arka plan katmanları ── */}

        {/* 1. Büyük monogram — mimari yay, belirgin ama narin */}
        <div className="absolute inset-0 flex items-center justify-center pointer-events-none select-none">
          <Logo variant="mark" className="w-[110vmin] text-foreground" style={{ opacity: 0.055 }} />
        </div>

        {/* 2. Yatay dekoratif çizgiler — mimari his */}
        <div className="absolute inset-0 pointer-events-none select-none flex flex-col justify-between py-24 px-8 opacity-20">
          <div className="w-full h-px bg-foreground/30" />
          <div className="w-full h-px bg-foreground/30" />
          <div className="w-full h-px bg-foreground/30" />
        </div>

        {/* 3. Köşe aksan çizgileri */}
        <div className="absolute top-24 left-8 pointer-events-none select-none opacity-25">
          <div className="w-12 h-px bg-accent mb-2" />
          <div className="w-6 h-px bg-accent" />
        </div>
        <div className="absolute bottom-16 right-8 pointer-events-none select-none opacity-25">
          <div className="w-6 h-px bg-accent mb-2" />
          <div className="w-12 h-px bg-accent" />
        </div>

        {/* ── İçerik ── */}
        <div className="relative z-10">
          <p className="font-sans text-xs text-muted tracking-[0.3em] mb-8">
            INTERIOR DESIGN
          </p>

          <h1 className="font-serif text-6xl sm:text-7xl lg:text-[6.5rem] text-foreground leading-[1.05] max-w-4xl">
            Mekânlar
            <br />
            <em className="not-italic text-accent">Hikâye</em> Anlatır
          </h1>

          <div className="w-12 h-px bg-accent mx-auto my-10" />

          <p className="font-sans text-sm text-muted max-w-sm leading-relaxed tracking-wide">
            Her projeyi özgün bir anlatıya dönüştürüyor,
            fonksiyon ile estetiği bir araya getiriyoruz.
          </p>

          <Link
            href="/projects"
            className="mt-10 inline-flex items-center gap-2 font-sans text-xs tracking-[0.2em] uppercase text-foreground border border-foreground px-6 py-3 hover:bg-foreground hover:text-background transition-colors"
          >
            Projeleri Keşfet
            <span aria-hidden>→</span>
          </Link>
        </div>

        {/* Alt orta: marka adı küçük */}
        <div className="absolute bottom-8 left-1/2 -translate-x-1/2 pointer-events-none select-none">
          <p className="font-sans text-[9px] text-muted/40 tracking-[0.4em]">
            ELİF BENZER — INTERIOR ARC
          </p>
        </div>
      </section>

      {/* ── Öne çıkan projeler ───────────────────────────────────── */}
      {featured.length > 0 && (
        <section className="max-w-6xl mx-auto px-6 py-24">
          {/* Bölüm başlığı */}
          <div className="flex items-end justify-between mb-12">
            <div>
              <p className="font-sans text-xs text-muted tracking-[0.2em] uppercase mb-2">
                Seçili Çalışmalar
              </p>
              <h2 className="font-serif text-4xl text-foreground">
                Öne Çıkan Projeler
              </h2>
            </div>
            <Link
              href="/projects"
              className="hidden sm:inline-flex font-sans text-xs text-accent hover:text-accent-hover tracking-wide transition-colors gap-1"
            >
              Tümünü Gör →
            </Link>
          </div>

          {/* Kart grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
            {featured.map((project) => (
              <ProjectCard key={project.id} project={project} />
            ))}
          </div>

          {/* Mobil "tümünü gör" */}
          <div className="mt-10 text-center sm:hidden">
            <Link
              href="/projects"
              className="font-sans text-xs text-accent tracking-wide"
            >
              Tüm Projeleri Gör →
            </Link>
          </div>
        </section>
      )}

      {/* ── Hakkımızda / Yaklaşım ─────────────────────────────────── */}
      <section className="bg-surface-muted py-24 px-6">
        <div className="max-w-2xl mx-auto text-center">
          <p className="font-sans text-xs text-muted tracking-[0.2em] uppercase mb-4">
            Yaklaşımımız
          </p>
          <h2 className="font-serif text-4xl text-foreground mb-8 leading-snug">
            Her alan, bir yaşam biçiminin yansımasıdır.
          </h2>
          <p className="font-sans text-sm text-muted leading-relaxed max-w-lg mx-auto">
            İç mekân tasarımında sadece görselliği değil, o mekânın içinde nasıl
            hissedileceğini şekillendiriyoruz. Doğal malzemeler, dengeli renkler
            ve dikkatle seçilmiş detaylarla her projeyi özgün bir deneyime
            dönüştürüyoruz.
          </p>
        </div>
      </section>

      {/* ── İletişim ──────────────────────────────────────────────── */}
      <section id="iletisim" className="max-w-6xl mx-auto px-6 py-24">
        <div className="grid grid-cols-1 lg:grid-cols-5 gap-16 lg:gap-24">

          {/* Sol: başlık + açıklama (2/5) */}
          <div className="lg:col-span-2">
            <p className="font-sans text-xs text-muted tracking-[0.2em] uppercase mb-4">
              İletişim
            </p>
            <h2 className="font-serif text-4xl text-foreground mb-6 leading-snug">
              Bir proje mi planlıyorsunuz?
            </h2>
            <div className="w-8 h-px bg-accent mb-6" />
            <p className="font-sans text-sm text-muted leading-relaxed">
              Fikirlerinizi dinlemekten ve projenizi birlikte
              şekillendirmekten mutluluk duyarız. Formu doldurun,
              en kısa sürede size ulaşalım.
            </p>
          </div>

          {/* Sağ: form (3/5) */}
          <div className="lg:col-span-3">
            <ContactForm />
          </div>

        </div>

        <div className="w-8 h-px bg-accent mt-24" />
      </section>
    </>
  );
}
