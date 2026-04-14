"use client";

import { useState } from "react";
import Lightbox, { type LightboxImage } from "./Lightbox";
import type { ProjectImageData } from "@/types/api";

interface Props {
  images: ProjectImageData[];
  projectTitle: string;
  /**
   * Cover dahil tüm görseller.
   * Lightbox'ta galeri görselinden sola kaydırınca kapak görseline ulaşılabilir.
   */
  allImages: LightboxImage[];
  /**
   * allImages dizisinde galeri görsellerinin başladığı index.
   * Cover varsa 1, yoksa 0.
   */
  galleryOffset: number;
}

/**
 * Masonry galeri grid'i.
 * Her görsel tıklandığında full-screen Lightbox açılır.
 * Lightbox içinde ← → ile kapak dahil tüm görseller gezilebilir.
 */
export default function GalleryGrid({
  images,
  projectTitle,
  allImages,
  galleryOffset,
}: Props) {
  const [lightboxIndex, setLightboxIndex] = useState<number | null>(null);

  return (
    <section className="max-w-6xl mx-auto px-6 pb-24">
      <div className="w-8 h-px bg-accent mb-12" />

      {/* CSS columns masonry */}
      <div className="columns-1 sm:columns-2 lg:columns-3 gap-4 space-y-4">
        {images.map((img, i) => (
          <div
            key={img.id}
            className="break-inside-avoid bg-surface-muted overflow-hidden cursor-zoom-in group relative"
            onClick={() => setLightboxIndex(galleryOffset + i)}
            role="button"
            aria-label={`${img.altText ?? projectTitle} — büyütmek için tıklayın`}
          >
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img
              src={img.url}
              alt={img.altText ?? projectTitle}
              className="w-full h-auto block"
              loading="lazy"
            />

            {/* Hover: karartma + büyüt ikonu */}
            <div className="absolute inset-0 bg-black/0 group-hover:bg-black/18 transition-colors duration-200 flex items-center justify-center pointer-events-none">
              <div className="opacity-0 group-hover:opacity-100 transition-opacity duration-200 bg-black/40 rounded-full p-2.5">
                <svg
                  width="16"
                  height="16"
                  viewBox="0 0 16 16"
                  fill="none"
                  className="text-white"
                  aria-hidden="true"
                >
                  <circle cx="7" cy="7" r="4.5" stroke="currentColor" strokeWidth="1.3" />
                  <path
                    d="M10.5 10.5L14 14"
                    stroke="currentColor"
                    strokeWidth="1.3"
                    strokeLinecap="round"
                  />
                  <path
                    d="M7 5V9M5 7H9"
                    stroke="currentColor"
                    strokeWidth="1.3"
                    strokeLinecap="round"
                  />
                </svg>
              </div>
            </div>

            {img.caption && (
              <p className="font-sans text-xs text-muted px-3 py-2">{img.caption}</p>
            )}
          </div>
        ))}
      </div>

      {/* Lightbox */}
      {lightboxIndex !== null && (
        <Lightbox
          images={allImages}
          initialIndex={lightboxIndex}
          onClose={() => setLightboxIndex(null)}
        />
      )}
    </section>
  );
}
