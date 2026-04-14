-- ============================================================
-- AUDIT LOGS
-- Değiştirilemez olay kaydı. Uygulama katmanında sadece INSERT
-- ve SELECT yapılır — UPDATE / DELETE yoktur.
--
-- Güvenlik notu (production):
--   Portfolio DB kullanıcısına audit_logs tablosu için
--   DELETE ve UPDATE izinleri VERİLMEMELİDİR:
--     REVOKE UPDATE, DELETE ON audit_logs FROM portfolio_user;
-- ============================================================
CREATE TABLE audit_logs
(
    id          BIGSERIAL PRIMARY KEY,

    -- Aksiyonu kim yaptı? NULL → anonim (ziyaretçi contact formu gibi)
    actor_email VARCHAR(255),

    -- Hangi olay? (Enum string — AuditAction.java ile eşleşir)
    action      VARCHAR(100) NOT NULL,

    -- Hangi domain nesnesi üzerinde? (opsiyonel)
    entity_type VARCHAR(100),
    entity_id   BIGINT,

    -- Ağ bilgisi
    ip_address  VARCHAR(45),   -- IPv6 için 45 karakter yeterli
    user_agent  VARCHAR(500),

    -- Serbest JSON — aksiyon bazlı ekstra bilgi
    -- Örn: {"title":"Salon Projesi","category":"RESIDENTIAL"}
    -- ÖNEMLİ: Hiçbir zaman şifre veya hassas kimlik bilgisi loglanmamalı
    metadata    TEXT,

    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ── İndeksler ────────────────────────────────────────────────────────
-- Hangi admin ne yaptı? (en sık sorgulanan)
CREATE INDEX idx_audit_logs_actor_email ON audit_logs (actor_email);

-- Belirli aksiyonu filtrele (LOGIN_FAILURE toplu sorgulama gibi)
CREATE INDEX idx_audit_logs_action ON audit_logs (action);

-- Zaman bazlı sıralama ve aralık sorguları
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at DESC);

-- Belirli bir entity'nin geçmişi
CREATE INDEX idx_audit_logs_entity ON audit_logs (entity_type, entity_id)
    WHERE entity_type IS NOT NULL;
