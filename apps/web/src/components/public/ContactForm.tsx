"use client";

import { useState } from "react";
import axios from "axios";
import { sendContactMessage } from "@/lib/api/contact";

// ── Küçük yardımcı bileşenler (yalnızca bu dosyada kullanılır) ──

const inputCls =
  "w-full bg-transparent border-b border-border py-3 font-sans text-sm text-foreground " +
  "placeholder:text-muted/40 focus:outline-none focus:border-accent transition-colors";

function Field({
  label,
  required,
  hint,
  error,
  children,
}: {
  label: string;
  required?: boolean;
  hint?: string;
  error?: string;
  children: React.ReactNode;
}) {
  return (
    <div className="space-y-1">
      <div className="flex items-baseline justify-between">
        <label className="block font-sans text-xs text-muted tracking-wider uppercase">
          {label}
          {required && <span className="text-accent ml-0.5">*</span>}
        </label>
        {hint && (
          <span className="font-sans text-[10px] text-muted italic">{hint}</span>
        )}
      </div>
      {children}
      {error && (
        <p className="font-sans text-xs text-red-500 pt-0.5">{error}</p>
      )}
    </div>
  );
}

// ── Tip tanımları ──

interface FormValues {
  name: string;
  email: string;
  phone: string;
  message: string;
}

type FormErrors = Partial<Record<keyof FormValues, string>>;
type Status = "idle" | "submitting" | "success" | "error";

// ── Ana bileşen ──

export default function ContactForm() {
  const [values, setValues] = useState<FormValues>({
    name: "",
    email: "",
    phone: "",
    message: "",
  });
  const [errors, setErrors] = useState<FormErrors>({});
  const [status, setStatus] = useState<Status>("idle");
  const [apiError, setApiError] = useState<string | null>(null);

  function set<K extends keyof FormValues>(key: K, value: string) {
    setValues((v) => ({ ...v, [key]: value }));
    if (errors[key]) setErrors((e) => ({ ...e, [key]: undefined }));
  }

  function validate(): boolean {
    const errs: FormErrors = {};

    if (!values.name.trim())
      errs.name = "Ad Soyad zorunlu.";

    if (!values.email.trim())
      errs.email = "E-posta zorunlu.";
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(values.email))
      errs.email = "Geçerli bir e-posta adresi girin.";

    if (!values.message.trim())
      errs.message = "Mesaj zorunlu.";
    else if (values.message.trim().length < 10)
      errs.message = "Mesaj en az 10 karakter olmalıdır.";

    setErrors(errs);
    return Object.keys(errs).length === 0;
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!validate()) return;

    setStatus("submitting");
    setApiError(null);

    try {
      await sendContactMessage({
        fullName: values.name.trim(),
        email:    values.email.trim(),
        phone:    values.phone.trim() || undefined,
        message:  values.message.trim(),
      });
      setStatus("success");
    } catch (err: unknown) {
      const msg = axios.isAxiosError(err)
        ? (err.response?.data?.message ?? "Mesajınız gönderilemedi. Lütfen tekrar deneyin.")
        : "Mesajınız gönderilemedi. Lütfen tekrar deneyin.";
      setApiError(msg);
      setStatus("error");
    }
  }

  // ── Başarı durumu ──
  if (status === "success") {
    return (
      <div className="py-8">
        <div className="w-10 h-px bg-accent mb-8" />
        <h3 className="font-serif text-3xl text-foreground mb-4">
          Teşekkürler
        </h3>
        <p className="font-sans text-sm text-muted leading-relaxed max-w-xs">
          Mesajınız alındı. En kısa sürede size geri dönüş yapacağız.
        </p>
      </div>
    );
  }

  // ── Form ──
  return (
    <form onSubmit={handleSubmit} noValidate className="space-y-8">

      {/* Ad — E-posta yan yana */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-8">
        <Field label="Ad Soyad" required error={errors.name}>
          <input
            type="text"
            value={values.name}
            onChange={(e) => set("name", e.target.value)}
            placeholder="Adınız Soyadınız"
            autoComplete="name"
            className={inputCls}
          />
        </Field>

        <Field label="E-posta" required error={errors.email}>
          <input
            type="email"
            value={values.email}
            onChange={(e) => set("email", e.target.value)}
            placeholder="ornek@email.com"
            autoComplete="email"
            className={inputCls}
          />
        </Field>
      </div>

      {/* Telefon */}
      <Field label="Telefon" hint="İsteğe bağlı">
        <input
          type="tel"
          value={values.phone}
          onChange={(e) => set("phone", e.target.value)}
          placeholder="+90 5xx xxx xx xx"
          autoComplete="tel"
          className={inputCls}
        />
      </Field>

      {/* Mesaj */}
      <Field label="Mesaj" required error={errors.message}>
        <textarea
          value={values.message}
          onChange={(e) => set("message", e.target.value)}
          rows={5}
          placeholder="Projeniz hakkında bilgi verin…"
          className={`${inputCls} resize-none`}
        />
      </Field>

      {/* API hatası */}
      {status === "error" && apiError && (
        <p className="font-sans text-xs text-red-500">{apiError}</p>
      )}

      {/* Gönder */}
      <button
        type="submit"
        disabled={status === "submitting"}
        className="
          font-sans text-xs tracking-[0.2em] uppercase
          px-8 py-3.5 bg-foreground text-background
          hover:bg-accent disabled:opacity-50
          transition-colors
        "
      >
        {status === "submitting" ? "Gönderiliyor…" : "Gönder"}
      </button>

    </form>
  );
}