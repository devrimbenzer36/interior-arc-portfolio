-- ============================================================
-- Portfolio Platform - Initial Schema
-- V1__init_schema.sql
-- ============================================================

-- ============================================================
-- MEDIA FILES
-- Storage abstraction: LOCAL veya S3, her ikisi de bu tabloyu kullanır.
-- url alanı storage_type'a göre relative path veya S3 key içerir.
-- ============================================================
CREATE TABLE media_files
(
    id            BIGSERIAL PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL,
    stored_name   VARCHAR(255) NOT NULL UNIQUE,
    url           TEXT         NOT NULL,
    mime_type     VARCHAR(100) NOT NULL,
    size          BIGINT       NOT NULL,
    width         INTEGER,
    height        INTEGER,
    storage_type  VARCHAR(20)  NOT NULL DEFAULT 'LOCAL',
    created_at    TIMESTAMP             DEFAULT NOW()
);

-- ============================================================
-- PROJECTS
-- Aggregate root. Tüm proje bilgileri burada.
-- cover_image_id: galeri dışında ayrıca tutulan kapak görseli.
-- slug: SEO-friendly URL (/projects/bogazici-dairesi)
-- ============================================================
CREATE TABLE projects
(
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    slug            VARCHAR(255) NOT NULL UNIQUE,
    short_desc      VARCHAR(500),
    detailed_story  TEXT,
    category        VARCHAR(50),
    style           VARCHAR(100),
    space_type      VARCHAR(100),
    location        VARCHAR(255),
    project_date    DATE,
    square_meters   DECIMAL(10, 2),
    budget_range    VARCHAR(100),
    status          VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    featured        BOOLEAN               DEFAULT FALSE,
    view_count      INTEGER               DEFAULT 0,
    cover_image_id  BIGINT REFERENCES media_files (id) ON DELETE SET NULL,
    created_at      TIMESTAMP             DEFAULT NOW(),
    updated_at      TIMESTAMP             DEFAULT NOW()
);

CREATE INDEX idx_projects_slug ON projects (slug);
CREATE INDEX idx_projects_status ON projects (status);
CREATE INDEX idx_projects_featured ON projects (featured);
CREATE INDEX idx_projects_category ON projects (category);

-- ============================================================
-- PROJECT IMAGES
-- Bir projenin galeri görselleri. display_order ile sıralama.
-- type: GALLERY | BEFORE_AFTER
-- cover ayrıca projects.cover_image_id üzerinden tutulur.
-- ============================================================
CREATE TABLE project_images
(
    id            BIGSERIAL PRIMARY KEY,
    project_id    BIGINT      NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    media_file_id BIGINT      NOT NULL REFERENCES media_files (id) ON DELETE RESTRICT,
    type          VARCHAR(30) NOT NULL DEFAULT 'GALLERY',
    display_order INTEGER              DEFAULT 0,
    alt_text      VARCHAR(255),
    caption       TEXT,
    created_at    TIMESTAMP            DEFAULT NOW()
);

CREATE INDEX idx_project_images_project_id ON project_images (project_id);

-- ============================================================
-- PROJECT TAGS
-- Öne çıkan etiketler. Ör: "Ödüllü", "Sürdürülebilir", "2024 Trendi"
-- ============================================================
CREATE TABLE project_tags
(
    id         BIGSERIAL PRIMARY KEY,
    project_id BIGINT       NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    name       VARCHAR(100) NOT NULL
);

CREATE INDEX idx_project_tags_project_id ON project_tags (project_id);

-- ============================================================
-- PROJECT MATERIALS
-- Kullanılan malzemeler. Ör: "Mermer", "Ahşap", "Cam"
-- ============================================================
CREATE TABLE project_materials
(
    id         BIGSERIAL PRIMARY KEY,
    project_id BIGINT       NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    name       VARCHAR(100) NOT NULL
);

CREATE INDEX idx_project_materials_project_id ON project_materials (project_id);

-- ============================================================
-- CONTACT MESSAGES
-- İletişim formu. Admin panelinden okunur ve durum güncellenir.
-- ip_address: rate limiting ve spam tespiti için tutulur.
-- ============================================================
CREATE TABLE contact_messages
(
    id         BIGSERIAL PRIMARY KEY,
    full_name  VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    phone      VARCHAR(50),
    subject    VARCHAR(255),
    message    TEXT         NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'NEW',
    ip_address VARCHAR(50),
    created_at TIMESTAMP             DEFAULT NOW()
);

CREATE INDEX idx_contact_messages_status ON contact_messages (status);
CREATE INDEX idx_contact_messages_created_at ON contact_messages (created_at DESC);

-- ============================================================
-- COMMENTS (Phase 3 - tablo hazır, entity Phase 3'te)
-- Moderasyon: PENDING → APPROVED | REJECTED
-- ============================================================
CREATE TABLE comments
(
    id          BIGSERIAL PRIMARY KEY,
    project_id  BIGINT       NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    author_name VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    body        TEXT         NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP             DEFAULT NOW()
);

CREATE INDEX idx_comments_project_id ON comments (project_id);
CREATE INDEX idx_comments_status ON comments (status);

-- ============================================================
-- ADMIN USERS (Phase 2 - tablo hazır, auth Phase 2'de)
-- ============================================================
CREATE TABLE admin_users
(
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255),
    role          VARCHAR(50)  NOT NULL DEFAULT 'ADMIN',
    active        BOOLEAN               DEFAULT TRUE,
    created_at    TIMESTAMP             DEFAULT NOW()
);