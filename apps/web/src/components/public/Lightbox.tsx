"use client";

import { useEffect, useCallback, useRef, useState } from "react";

export interface LightboxImage {
  src: string;
  alt?: string;
  caption?: string;
}

interface Props {
  images: LightboxImage[];
  initialIndex: number;
  onClose: () => void;
}

export default function Lightbox({ images, initialIndex, onClose }: Props) {
  const [index, setIndex] = useState(initialIndex);
  const [fading, setFading] = useState(false);
  const touchX = useRef(0);
  const touchY = useRef(0);
  const total = images.length;
  const current = images[index];

  /* ── Navigasyon ──────────────────────────────────────────── */

  const goTo = useCallback(
    (next: number) => {
      if (fading || total <= 1) return;
      const normalized = ((next % total) + total) % total;
      setFading(true);
      setTimeout(() => {
        setIndex(normalized);
        setFading(false);
      }, 200);
    },
    [fading, total],
  );

  const prev = useCallback(() => goTo(index - 1), [index, goTo]);
  const next = useCallback(() => goTo(index + 1), [index, goTo]);

  /* ── Klavye ──────────────────────────────────────────────── */

  useEffect(() => {
    const handle = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
      if (e.key === "ArrowLeft") prev();
      if (e.key === "ArrowRight") next();
    };
    document.addEventListener("keydown", handle);
    return () => document.removeEventListener("keydown", handle);
  }, [onClose, prev, next]);

  /* ── Scroll kilidi ───────────────────────────────────────── */

  useEffect(() => {
    const orig = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = orig;
    };
  }, []);

  /* ── Touch (swipe) ───────────────────────────────────────── */

  function onTouchStart(e: React.TouchEvent) {
    touchX.current = e.touches[0].clientX;
    touchY.current = e.touches[0].clientY;
  }
  function onTouchEnd(e: React.TouchEvent) {
    const dx = e.changedTouches[0].clientX - touchX.current;
    const dy = e.changedTouches[0].clientY - touchY.current;
    if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > 48) {
      if (dx < 0) { next(); } else { prev(); }
    }
  }

  /* ── Render ──────────────────────────────────────────────── */

  return (
    <div
      className="fixed inset-0 z-50 bg-black/92"
      onTouchStart={onTouchStart}
      onTouchEnd={onTouchEnd}
    >
      <div className="relative h-full flex flex-col select-none">

        {/* ── Üst bar: sayaç + kapat ── */}
        <div className="flex-shrink-0 flex items-center justify-between px-5 pt-4 pb-2">
          <span className="font-sans text-[10px] text-white/35 tracking-[0.28em] uppercase">
            {index + 1} / {total}
          </span>
          <button
            onClick={onClose}
            className="p-2 -mr-2 text-white/50 hover:text-white transition-colors"
            aria-label="Kapat"
          >
            <svg width="18" height="18" viewBox="0 0 18 18" fill="none" aria-hidden="true">
              <path
                d="M3.5 3.5L14.5 14.5M14.5 3.5L3.5 14.5"
                stroke="currentColor"
                strokeWidth="1.4"
                strokeLinecap="round"
              />
            </svg>
          </button>
        </div>

        {/* ── Orta: oklar + görsel ── */}
        <div className="flex-1 flex items-center min-h-0">

          {/* Sol ok */}
          <button
            onClick={prev}
            className={`flex-shrink-0 p-4 sm:p-5 text-white/40 hover:text-white transition-colors ${
              total <= 1 ? "invisible pointer-events-none" : ""
            }`}
            aria-label="Önceki görsel"
          >
            <svg width="22" height="22" viewBox="0 0 22 22" fill="none" aria-hidden="true">
              <path
                d="M14 3L6 11L14 19"
                stroke="currentColor"
                strokeWidth="1.4"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          </button>

          {/* Görsel alanı — arka plana tıklayınca kapat, görsele tıklayınca açık kal */}
          <div
            className="flex-1 h-full flex items-center justify-center min-w-0 cursor-pointer overflow-hidden"
            onClick={onClose}
          >
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img
              src={current.src}
              alt={current.alt ?? ""}
              className="max-w-full object-contain cursor-default"
              style={{
                maxHeight: "calc(100vh - 140px)",
                opacity: fading ? 0 : 1,
                transition: "opacity 0.2s ease",
              }}
              onClick={(e) => e.stopPropagation()}
              draggable={false}
            />
          </div>

          {/* Sağ ok */}
          <button
            onClick={next}
            className={`flex-shrink-0 p-4 sm:p-5 text-white/40 hover:text-white transition-colors ${
              total <= 1 ? "invisible pointer-events-none" : ""
            }`}
            aria-label="Sonraki görsel"
          >
            <svg width="22" height="22" viewBox="0 0 22 22" fill="none" aria-hidden="true">
              <path
                d="M8 3L16 11L8 19"
                stroke="currentColor"
                strokeWidth="1.4"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          </button>
        </div>

        {/* ── Alt bar: caption + ilerleme noktaları ── */}
        <div className="flex-shrink-0 py-3 px-6 flex flex-col items-center gap-2.5">
          {current.caption && (
            <p className="font-sans text-[11px] text-white/40 tracking-wide text-center max-w-md">
              {current.caption}
            </p>
          )}
          {total > 1 && (
            <div className="flex items-center gap-1.5">
              {Array.from({ length: total }).map((_, i) => (
                <button
                  key={i}
                  onClick={() => goTo(i)}
                  className={`rounded-full transition-all duration-200 ${
                    i === index
                      ? "bg-white w-5 h-[3px]"
                      : "bg-white/30 w-[5px] h-[3px] hover:bg-white/60"
                  }`}
                  aria-label={`Görsel ${i + 1}`}
                />
              ))}
            </div>
          )}
        </div>

      </div>
    </div>
  );
}
