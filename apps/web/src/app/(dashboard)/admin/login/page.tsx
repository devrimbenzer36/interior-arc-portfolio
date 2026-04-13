"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import axios from "axios";
import { login } from "@/lib/api/auth";
import { tokenStore } from "@/lib/auth/token";

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  // Zaten giriş yapmışsa dashboard'a yönlendir
  useEffect(() => {
    if (tokenStore.isAuthenticated()) {
      router.replace("/admin");
    }
  }, [router]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const response = await login({ email, password });
      tokenStore.set(response.token, response.email);
      router.replace("/admin");
    } catch (err: unknown) {
      // Axios HTTP hatası → backend'in döndürdüğü mesajı al
      // Düz Error → mesajı doğrudan kullan
      const message = axios.isAxiosError(err)
        ? (err.response?.data?.message ?? "Giriş başarısız. Tekrar deneyin.")
        : err instanceof Error
        ? err.message
        : "Giriş başarısız. Tekrar deneyin.";
      setError(message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="min-h-screen flex items-center justify-center bg-background">
      <div className="w-full max-w-sm px-6">
        {/* Başlık */}
        <div className="text-center mb-10">
          <h1 className="font-serif text-4xl text-foreground mb-2">Portfolio</h1>
          <p className="font-sans text-muted text-xs tracking-widest uppercase">
            Admin Paneli
          </p>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-5">
          <div className="space-y-1.5">
            <label
              htmlFor="email"
              className="block font-sans text-xs text-muted tracking-wider uppercase"
            >
              E-posta
            </label>
            <input
              id="email"
              type="email"
              autoComplete="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="
                w-full bg-surface border border-border rounded-none px-4 py-3
                font-sans text-sm text-foreground
                placeholder:text-muted/50
                focus:outline-none focus:border-accent
                transition-colors
              "
              placeholder="admin@portfolio.com"
            />
          </div>

          <div className="space-y-1.5">
            <label
              htmlFor="password"
              className="block font-sans text-xs text-muted tracking-wider uppercase"
            >
              Şifre
            </label>
            <input
              id="password"
              type="password"
              autoComplete="current-password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="
                w-full bg-surface border border-border rounded-none px-4 py-3
                font-sans text-sm text-foreground
                placeholder:text-muted/50
                focus:outline-none focus:border-accent
                transition-colors
              "
              placeholder="••••••••"
            />
          </div>

          {error && (
            <p className="font-sans text-xs text-red-600 py-2">{error}</p>
          )}

          <button
            type="submit"
            disabled={loading}
            className="
              w-full bg-accent text-white font-sans text-xs tracking-widest uppercase
              px-4 py-3.5 mt-2
              hover:bg-accent-hover focus:outline-none
              disabled:opacity-50 disabled:cursor-not-allowed
              transition-colors
            "
          >
            {loading ? "Giriş yapılıyor…" : "Giriş Yap"}
          </button>
        </form>

        {/* Alt dekoratif çizgi */}
        <div className="w-8 h-px bg-accent mx-auto mt-12" />
      </div>
    </main>
  );
}
