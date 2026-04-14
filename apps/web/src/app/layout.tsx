import type { Metadata } from "next";
import { Inter, Cormorant_Garamond } from "next/font/google";
import "./globals.css";


/*
 * Inter: gövde metni, UI, admin panel
 * Cormorant Garamond: başlıklar, hero alanları — editorial, lüks his
 *
 * variable ile CSS custom property'ye bağlanır → globals.css'deki @theme ile eşleşir
 */
const inter = Inter({
  variable: "--font-inter",
  subsets: ["latin"],
  display: "swap",
});

const cormorant = Cormorant_Garamond({
  variable: "--font-cormorant",
  subsets: ["latin"],
  weight: ["300", "400", "500", "600"],
  display: "swap",
});

export const metadata: Metadata = {
  title: {
    default: "Elif Benzer — Interior Arc",
    template: "%s | Interior Arc",
  },
  description:
    "İç mekân tasarımında özgün hikayeler. Elif Benzer imzalı konut, ticari ve ofis tasarım projeleri.",
  openGraph: {
    type: "website",
    locale: "tr_TR",
    siteName: "Interior Arc",
    title: "Elif Benzer — Interior Arc",
    description:
      "İç mekân tasarımında özgün hikayeler. Elif Benzer imzalı konut, ticari ve ofis tasarım projeleri.",
  },
  robots: {
    index: true,
    follow: true,
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="tr" className={`${inter.variable} ${cormorant.variable}`}>
      <body>
        {children}
      </body>
    </html>
  );
}
