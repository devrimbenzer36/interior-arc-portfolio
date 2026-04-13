interface Props {
  title: string;
  description: string;
  confirmLabel?: string;
  loading?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export default function ConfirmDialog({
  title,
  description,
  confirmLabel = "Sil",
  loading = false,
  onConfirm,
  onCancel,
}: Props) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black/30"
        onClick={onCancel}
        aria-hidden="true"
      />

      {/* Dialog */}
      <div className="relative bg-surface border border-border p-6 w-full max-w-sm mx-4 shadow-lg">
        <h2 className="font-serif text-xl text-foreground mb-2">{title}</h2>
        <p className="font-sans text-sm text-muted mb-6">{description}</p>

        <div className="flex gap-3 justify-end">
          <button
            onClick={onCancel}
            disabled={loading}
            className="px-4 py-2 font-sans text-sm text-muted border border-border hover:text-foreground disabled:opacity-50 transition-colors"
          >
            İptal
          </button>
          <button
            onClick={onConfirm}
            disabled={loading}
            className="px-4 py-2 font-sans text-sm text-white bg-red-600 hover:bg-red-700 disabled:opacity-50 transition-colors"
          >
            {loading ? "İşleniyor…" : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
