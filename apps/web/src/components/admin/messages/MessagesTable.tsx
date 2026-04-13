"use client";

import Badge from "@/components/admin/ui/Badge";
import type { ContactMessage } from "@/types/api";

interface Props {
  messages: ContactMessage[];
  onSelect: (message: ContactMessage) => void;
}

/** Mesaj gövdesinin ilk N karakterini + "…" döner */
function preview(text: string, limit = 90): string {
  const first = text.split("\n")[0];
  return first.length > limit ? first.slice(0, limit) + "…" : first;
}

export default function MessagesTable({ messages, onSelect }: Props) {
  if (messages.length === 0) {
    return (
      <div className="py-16 text-center">
        <div className="w-8 h-px bg-border mx-auto mb-5" />
        <p className="font-sans text-sm text-muted">Henüz mesaj yok.</p>
      </div>
    );
  }

  return (
    <div className="divide-y divide-border">
      {messages.map((msg) => {
        const isNew = msg.status === "NEW";
        const date = new Date(msg.createdAt).toLocaleDateString("tr-TR", {
          day: "numeric",
          month: "short",
          year: "numeric",
        });

        return (
          <button
            key={msg.id}
            onClick={() => onSelect(msg)}
            className={`
              w-full text-left px-4 py-4 transition-colors
              hover:bg-surface-muted
              ${isNew ? "bg-amber-50/40" : ""}
            `}
          >
            <div className="flex items-start justify-between gap-4">

              {/* Sol: gönderen + önizleme */}
              <div className="flex-1 min-w-0 space-y-1">
                <div className="flex items-center gap-2.5">
                  <p className={`font-sans text-sm text-foreground truncate ${isNew ? "font-semibold" : ""}`}>
                    {msg.fullName}
                  </p>
                  <Badge status={msg.status} />
                </div>
                <p className="font-sans text-xs text-muted truncate">
                  {msg.email}
                </p>
                <p className="font-sans text-xs text-muted/80 truncate">
                  {preview(msg.message)}
                </p>
              </div>

              {/* Sağ: tarih */}
              <p className="font-sans text-xs text-muted shrink-0 pt-0.5">
                {date}
              </p>

            </div>
          </button>
        );
      })}
    </div>
  );
}