import { revalidatePath } from "next/cache";
import { NextResponse } from "next/server";

/**
 * Admin panel proje mutasyonlarından (create/update/publish/delete) sonra
 * client-side'dan çağrılır. Next.js önbelleğini temizler.
 *
 * POST /api/revalidate
 *
 * Güvenlik: sadece cache temizleme — veri okuma/yazma yok.
 * Kötüye kullanım riski: cache churn (kabul edilebilir, portfolio trafiği düşük).
 */
export async function POST() {
  // Ana sayfa (öne çıkan projeler)
  revalidatePath("/", "page");

  // Projeler listesi + tüm proje detay sayfaları
  revalidatePath("/projects", "layout");

  return NextResponse.json({ revalidated: true });
}
