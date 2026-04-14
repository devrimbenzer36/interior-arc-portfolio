import Link from "next/link";
import Logo from "@/components/public/Logo";

/**
 * Sabit üst navigasyon.
 *
 * Yükseklik : h-16 (mobil) / h-20 (sm+)
 * Logo      : mark (< sm) — sadece EB monogramı, 40×40 px, net okunur
 *             full (sm+)  — monogram + isim + unvan, h-14 (56px), okunur
 */
export default function Nav() {
  return (
    <header className="fixed top-0 left-0 right-0 z-40 bg-background/92 backdrop-blur-sm border-b border-border">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 h-16 sm:h-20 flex items-center justify-between">

        {/* Logo */}
        <Link href="/" className="text-foreground flex-shrink-0" aria-label="Ana sayfa">
          {/* Mobil: sadece monogram */}
          <Logo variant="mark" className="h-10 w-10 sm:hidden" />
          {/* Tablet+: tam logo — viewBox 210×50, h-14 → metin ~15px */}
          <Logo variant="full" className="hidden sm:block h-14 w-auto" />
        </Link>

        {/* Navigasyon linkleri */}
        <nav className="flex items-center gap-5 sm:gap-8">
          <Link
            href="/projects"
            className="font-sans text-xs sm:text-sm text-muted hover:text-foreground transition-colors tracking-wide"
          >
            Projeler
          </Link>
          <Link
            href="/#iletisim"
            className="font-sans text-xs sm:text-sm text-muted hover:text-foreground transition-colors tracking-wide"
          >
            İletişim
          </Link>
        </nav>

      </div>
    </header>
  );
}
