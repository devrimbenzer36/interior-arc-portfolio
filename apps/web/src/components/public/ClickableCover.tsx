"use client";

import { useState } from "react";
import Lightbox, { type LightboxImage } from "./Lightbox";

interface Props {
  coverUrl: string;
  coverAlt: string;
  /**
   * Lightbox'ta gezilecek tüm görseller dizisi.
   * Cover (index 0) + galeri görselleri birlikte geçilebilir.
   */
  allImages: LightboxImage[];
}

/**
 * Kapak görselini gösterir; tıklandığında tam ekran Lightbox açar.
 * Kullanıcı Lightbox içinden tüm proje görsellerini gezip geçebilir.
 */
export default function ClickableCover({ coverUrl, coverAlt, allImages }: Props) {
  const [open, setOpen] = useState(false);

  return (
    <>
      <div
        className="w-full bg-surface-muted flex items-center justify-center cursor-zoom-in relative group"
        onClick={() => setOpen(true)}
        role="button"
        aria-label="Büyütmek için tıklayın"
      >
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img
          src={coverUrl}
          alt={coverAlt}
          className="w-full max-h-[90vh] object-contain"
        />

        {/* Hover: büyüt ipucu */}
        <div className="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-200 pointer-events-none">
          <div className="bg-black/35 rounded-full p-3">
            <svg
              width="22"
              height="22"
              viewBox="0 0 22 22"
              fill="none"
              className="text-white"
              aria-hidden="true"
            >
              <circle cx="9.5" cy="9.5" r="6" stroke="currentColor" strokeWidth="1.4" />
              <path
                d="M14 14L19 19"
                stroke="currentColor"
                strokeWidth="1.4"
                strokeLinecap="round"
              />
              <path
                d="M9.5 7V12M7 9.5H12"
                stroke="currentColor"
                strokeWidth="1.4"
                strokeLinecap="round"
              />
            </svg>
          </div>
        </div>
      </div>

      {open && (
        <Lightbox
          images={allImages}
          initialIndex={0}
          onClose={() => setOpen(false)}
        />
      )}
    </>
  );
}
