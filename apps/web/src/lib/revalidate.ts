/**
 * Admin proje mutasyonlarından sonra public sayfaların önbelleğini temizler.
 * Hata olursa sessizce geçer — revalidate = 300 fallback zaten var.
 */
export async function revalidateProjectCache(): Promise<void> {
  try {
    await fetch("/api/revalidate", { method: "POST" });
  } catch {
    // Kritik değil — sayfalar max 5 dakika sonra zaten güncellenir
  }
}
