import Link from "next/link";

export default function Nav() {
  return (
    <header className="fixed top-0 left-0 right-0 z-40 bg-background/90 backdrop-blur-sm border-b border-border">
      <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
        <Link href="/" className="font-serif text-xl text-foreground tracking-wide">
          Portfolio
        </Link>

        <nav className="flex items-center gap-8">
          <Link
            href="/projects"
            className="font-sans text-sm text-muted hover:text-foreground transition-colors tracking-wide"
          >
            Projeler
          </Link>
          <Link
            href="/#iletisim"
            className="font-sans text-sm text-muted hover:text-foreground transition-colors tracking-wide"
          >
            İletişim
          </Link>
        </nav>
      </div>
    </header>
  );
}
