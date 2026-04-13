"use client";

import { useEffect, useState, useCallback } from "react";
import { adminGetMessages, adminMarkMessageRead } from "@/lib/api/contact";
import MessagesTable from "@/components/admin/messages/MessagesTable";
import MessageModal from "@/components/admin/messages/MessageModal";
import type { ContactMessage, PageResponse } from "@/types/api";

const PAGE_SIZE = 20;

export default function MessagesPage() {
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<ContactMessage> | null>(null);
  const [messages, setMessages] = useState<ContactMessage[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [selected, setSelected] = useState<ContactMessage | null>(null);

  const load = useCallback(async (p: number) => {
    setLoading(true);
    setError(false);
    try {
      const result = await adminGetMessages(p, PAGE_SIZE);
      setData(result);
      setMessages(result.content);
    } catch {
      setError(true);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(page); }, [page, load]);

  // Modal açılır — MessageModal bu callback'i kendi useEffect'inden çağırır
  function handleSelect(msg: ContactMessage) {
    setSelected(msg);
  }

  // NEW mesaj açılınca optimistic update + API çağrısı
  function handleRead(id: number) {
    // Listede anında güncelle
    setMessages((prev) =>
      prev.map((m) => (m.id === id ? { ...m, status: "READ" as const } : m))
    );
    // Açık modal da güncellensin
    setSelected((prev) =>
      prev?.id === id ? { ...prev, status: "READ" as const } : prev
    );
    // Arka planda API'ye bildir (hata sessizce geçer)
    adminMarkMessageRead(id).catch(() => {});
  }

  const unreadCount = messages.filter((m) => m.status === "NEW").length;

  return (
    <>
      <div className="space-y-6">
        {/* Başlık */}
        <div className="flex items-baseline gap-3">
          <h1 className="font-serif text-3xl text-foreground">Mesajlar</h1>
          {unreadCount > 0 && (
            <span className="font-sans text-xs text-amber-700 bg-amber-50 border border-amber-200 px-2 py-0.5">
              {unreadCount} yeni
            </span>
          )}
        </div>

        {/* İçerik kartı */}
        <div className="bg-surface border border-border">
          {loading ? (
            <div className="divide-y divide-border">
              {Array.from({ length: 6 }).map((_, i) => (
                <div key={i} className="px-4 py-4 space-y-2">
                  <div className="flex gap-3">
                    <div className="h-4 w-32 bg-surface-muted animate-pulse" />
                    <div className="h-4 w-12 bg-surface-muted animate-pulse" />
                  </div>
                  <div className="h-3 w-48 bg-surface-muted animate-pulse" />
                  <div className="h-3 w-72 bg-surface-muted animate-pulse" />
                </div>
              ))}
            </div>
          ) : error ? (
            <div className="py-12 text-center space-y-3">
              <p className="font-sans text-sm text-muted">
                Mesajlar yüklenemedi.
              </p>
              <button
                onClick={() => load(page)}
                className="font-sans text-xs text-accent hover:text-accent-hover transition-colors"
              >
                Tekrar dene
              </button>
            </div>
          ) : (
            <MessagesTable messages={messages} onSelect={handleSelect} />
          )}
        </div>

        {/* Pagination */}
        {data && data.totalPages > 1 && (
          <div className="flex items-center justify-between">
            <p className="font-sans text-xs text-muted">
              Toplam {data.totalElements} mesaj
            </p>
            <div className="flex items-center gap-2">
              <button
                disabled={data.first}
                onClick={() => setPage((p) => p - 1)}
                className="px-3 py-1.5 font-sans text-xs border border-border text-muted hover:text-foreground disabled:opacity-40 transition-colors"
              >
                ← Önceki
              </button>
              <span className="font-sans text-xs text-muted px-2">
                {data.number + 1} / {data.totalPages}
              </span>
              <button
                disabled={data.last}
                onClick={() => setPage((p) => p + 1)}
                className="px-3 py-1.5 font-sans text-xs border border-border text-muted hover:text-foreground disabled:opacity-40 transition-colors"
              >
                Sonraki →
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Modal */}
      {selected && (
        <MessageModal
          message={selected}
          onClose={() => setSelected(null)}
          onRead={handleRead}
        />
      )}
    </>
  );
}
