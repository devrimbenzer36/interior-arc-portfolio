import type { ProjectStatus, MessageStatus } from "@/types/api";

type AnyStatus = ProjectStatus | MessageStatus;

const STYLES: Record<AnyStatus, string> = {
  // Proje
  PUBLISHED: "bg-emerald-50 text-emerald-700 border-emerald-200",
  DRAFT:     "bg-amber-50   text-amber-700   border-amber-200",
  ARCHIVED:  "bg-gray-50    text-gray-400    border-gray-100",
  // Mesaj
  NEW:       "bg-amber-50   text-amber-700   border-amber-200",
  READ:      "bg-gray-50    text-gray-500    border-gray-200",
  REPLIED:   "bg-emerald-50 text-emerald-700 border-emerald-200",
};

const LABELS: Record<AnyStatus, string> = {
  PUBLISHED: "Yayında",
  DRAFT:     "Taslak",
  ARCHIVED:  "Arşiv",
  NEW:       "Yeni",
  READ:      "Okundu",
  REPLIED:   "Yanıtlandı",
};

export default function Badge({ status }: { status: AnyStatus }) {
  return (
    <span
      className={`inline-flex items-center border px-2 py-0.5 font-sans text-[11px] tracking-wide ${STYLES[status] ?? "bg-gray-50 text-gray-500 border-gray-200"}`}
    >
      {LABELS[status] ?? status}
    </span>
  );
}