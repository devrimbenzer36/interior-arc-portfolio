"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { tokenStore } from "@/lib/auth/token";

const NAV = [
  { label: "Genel Bakış", href: "/admin" },
  { label: "Projeler", href: "/admin/projects" },
  { label: "Mesajlar", href: "/admin/messages" },
];

export default function Sidebar() {
  const pathname = usePathname();
  const router = useRouter();

  function handleLogout() {
    tokenStore.clear();
    router.replace("/admin/login");
  }

  return (
    <aside className="w-56 shrink-0 bg-surface border-r border-border min-h-screen flex flex-col">
      {/* Logo */}
      <div className="px-6 py-7 border-b border-border">
        <span className="font-serif text-xl text-foreground">Portfolio</span>
        <p className="font-sans text-[10px] text-muted tracking-widest uppercase mt-0.5">
          Admin
        </p>
      </div>

      {/* Nav */}
      <nav className="flex-1 py-6 px-3 space-y-0.5">
        {NAV.map(({ label, href }) => {
          const active =
            href === "/admin"
              ? pathname === "/admin"
              : pathname.startsWith(href);
          return (
            <Link
              key={href}
              href={href}
              className={`
                block px-3 py-2.5 font-sans text-sm transition-colors
                ${
                  active
                    ? "text-accent bg-surface-muted"
                    : "text-muted hover:text-foreground hover:bg-surface-muted"
                }
              `}
            >
              {label}
            </Link>
          );
        })}
      </nav>

      {/* Çıkış */}
      <div className="px-3 py-5 border-t border-border">
        <button
          onClick={handleLogout}
          className="w-full text-left px-3 py-2.5 font-sans text-sm text-muted hover:text-foreground transition-colors"
        >
          Çıkış Yap
        </button>
      </div>
    </aside>
  );
}
