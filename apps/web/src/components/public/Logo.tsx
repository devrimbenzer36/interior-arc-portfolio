import type { CSSProperties } from "react";

interface Props {
  variant?: "full" | "mark";
  className?: string;
  style?: CSSProperties;
}

/**
 * ELİF BENZER — Interior Arc
 *
 * mark : EB monogramı + yay motifi (kare — nav mobil, hero arka plan)
 * full : monogram + isim + unvan (geniş ekranlar için nav)
 *
 * full viewBox 0 0 210 50 → h-14 (56px) verince:
 *   "ELİF BENZER"  → ~14.6px  ✓
 *   "INTERIOR ARC" → ~7px     ✓
 */
export default function Logo({ variant = "full", className = "", style }: Props) {

  /* ── Mark — kare monogram ──────────────────────────────────── */
  if (variant === "mark") {
    return (
      <svg
        viewBox="0 0 120 120"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        className={className}
        style={style}
        aria-label="Elif Benzer Interior Arc"
      >
        {/* Dış yay */}
        <path
          d="M16 104 A46 46 0 0 1 104 104"
          stroke="currentColor"
          strokeWidth="1.8"
          fill="none"
        />
        {/* İç yay */}
        <path
          d="M28 104 A34 34 0 0 1 92 104"
          stroke="currentColor"
          strokeWidth="1.1"
          fill="none"
          strokeOpacity="0.45"
        />
        {/* E */}
        <text
          x="22"
          y="88"
          fontFamily="var(--font-serif), Georgia, 'Times New Roman', serif"
          fontSize="52"
          fill="currentColor"
          letterSpacing="-2"
        >
          E
        </text>
        {/* B */}
        <text
          x="62"
          y="88"
          fontFamily="var(--font-serif), Georgia, 'Times New Roman', serif"
          fontSize="52"
          fill="currentColor"
          letterSpacing="-2"
        >
          B
        </text>
      </svg>
    );
  }

  /* ── Full — monogram + isim + unvan ────────────────────────── */
  return (
    <svg
      viewBox="0 0 210 50"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
      style={style}
      aria-label="Elif Benzer Interior Arc"
    >
      {/* Dış yay */}
      <path
        d="M3 48 A32 32 0 0 1 71 48"
        stroke="currentColor"
        strokeWidth="1.4"
        fill="none"
      />
      {/* İç yay */}
      <path
        d="M10 48 A25 25 0 0 1 64 48"
        stroke="currentColor"
        strokeWidth="0.9"
        fill="none"
        strokeOpacity="0.4"
      />

      {/* E */}
      <text
        x="9"
        y="41"
        fontFamily="var(--font-serif), Georgia, 'Times New Roman', serif"
        fontSize="30"
        fill="currentColor"
      >
        E
      </text>
      {/* B */}
      <text
        x="39"
        y="41"
        fontFamily="var(--font-serif), Georgia, 'Times New Roman', serif"
        fontSize="30"
        fill="currentColor"
      >
        B
      </text>

      {/* Dikey ayraç */}
      <line
        x1="79"
        y1="10"
        x2="79"
        y2="40"
        stroke="currentColor"
        strokeWidth="0.7"
        strokeOpacity="0.22"
      />

      {/* İsim */}
      <text
        x="89"
        y="29"
        fontFamily="var(--font-serif), Georgia, 'Times New Roman', serif"
        fontSize="13"
        fill="currentColor"
        letterSpacing="1.8"
      >
        ELİF BENZER
      </text>

      {/* Unvan */}
      <text
        x="90"
        y="43"
        fontFamily="var(--font-sans), 'Helvetica Neue', Arial, sans-serif"
        fontSize="6"
        fill="currentColor"
        letterSpacing="3.8"
        fillOpacity="0.52"
      >
        INTERIOR ARC
      </text>
    </svg>
  );
}
