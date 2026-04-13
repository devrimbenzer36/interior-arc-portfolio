"use client";

import { useEffect } from "react";
import Badge from "@/components/admin/ui/Badge";
import type { ContactMessage } from "@/types/api";

interface Props {
  message: ContactMessage;
  onClose: () => void;
  /** NEW mesaj açılınca sayfa bu callback ile state'i günceller */
  onRead: (id: number) => void;
}

export default function MessageModal({ message, onClose, onRead }: Props) {
  // Açılır açılmaz NEW ise okundu olarak işaretle
  useEffect(() => {
    if (message.status === "NEW") {
      onRead(message.id);
    }
    // ESC ile kapat
    function handleKey(e: KeyboardEvent) {
      if (e.key === "Escape") onClose();
    }
    document.addEventListener("keydown", handleKey);
    return () => document.removeEventListener("keydown", handleKey);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [message.id]);

  const date = new Date(message.createdAt).toLocaleDateString("tr-TR", {
    year: "numeric",
    month: "long",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black/30"
        onClick={onClose}
        aria-hidden="true"
      />

      {/* Dialog */}
      <div className="relative bg-surface border border-border w-full max-w-lg max-h-[80vh] flex flex-col shadow-xl">

        {/* Başlık */}
        <div className="flex items-start justify-between px-6 py-5 border-b border-border">
          <div className="space-y-1 pr-4">
            <div className="flex items-center gap-3">
              <p className="font-sans text-sm font-medium text-foreground">
                {message.fullName}
              </p>
              <Badge status={message.status} />
            </div>
            <p className="font-sans text-xs text-muted">{message.email}</p>
            {message.phone && (
              <p className="font-sans text-xs text-muted">{message.phone}</p>
            )}
            <p className="font-sans text-xs text-muted">{date}</p>
          </div>

          <button
            onClick={onClose}
            aria-label="Kapat"
            className="text-muted hover:text-foreground transition-colors mt-0.5 shrink-0"
          >
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M2 2l12 12M14 2L2 14" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
            </svg>
          </button>
        </div>

        {/* Mesaj gövdesi */}
        <div className="flex-1 overflow-y-auto px-6 py-5">
          <p className="font-sans text-sm text-foreground leading-relaxed whitespace-pre-wrap">
            {message.message}
          </p>
        </div>

        {/* Alt bar */}
        <div className="px-6 py-4 border-t border-border">
          <button
            onClick={onClose}
            className="font-sans text-xs text-muted hover:text-foreground transition-colors"
          >
            Kapat
          </button>
        </div>

      </div>
    </div>
  );
}