# Portfolio API — Türkçe Dokümantasyon

İç mimarlık portfolyosu için Spring Boot backend.
Ziyaretçilere açık bir portfolyo sitesi ve projeleri, görselleri, iletişim mesajlarını yönetmek için bir admin panel API'si sunar.

---

## Bu Sistem Ne Yapar?

- Yayınlanan iç mimarlık **projelerini** ziyaretçilere sunar (filtreleme, sayfalama, slug ile erişim)
- **Görselleri** yönetir: yükleme, galeriye ekleme, kapak görseli atama
- **Admin panel API'si** sunar: proje oluşturma/düzenleme, yayınlama, öne çıkarma
- **İletişim formu mesajlarını** takip eder (YENİ → OKUNDU → YANITLANDI) ve yeni mesajlarda e-posta bildirimi gönderir
- **Audit log** tutar: tüm admin ve public aksiyonlar izlenebilir, değiştirilemez kayıtlarla saklanır

---

## Mimari Özet

### Katman Yapısı

```
HTTP İsteği
    ↓
Controller      — HTTP bağlama. İş mantığı yok.
    ↓
Service         — İş kuralları, orchestration, transaction sınırı.
    ↓
Repository      — Veri erişimi. Spring Data JPA + özel JPQL sorguları.
    ↓
Entity          — Durum + domain metodları. İş kuralları burada yaşar.
```

**Kural:** Controller'lar repository'ye doğrudan erişmez. Service'ler HTTP response üretmez. Entity'ler repository çağırmaz.

### Domain Bazlı Paket Yapısı

```
com.portfolio
├── config/                  # SecurityConfig, AsyncConfig, OpenApiConfig
├── security/                # JWT auth altyapısı
│   ├── filter/              # JwtAuthFilter (OncePerRequestFilter)
│   ├── handler/             # AuthEntryPoint (401), AccessDeniedHandlerImpl (403)
│   ├── ratelimit/           # RateLimiterService — sliding window, in-memory
│   ├── service/             # UserDetailsServiceImpl
│   └── util/                # JwtUtil — üret / parse / doğrula
├── audit/                   # Değiştirilemez audit log sistemi
│   ├── entity/              # AuditLog — no-setter, factory method
│   ├── enums/               # AuditAction, AuditEntityType
│   ├── repository/          # Yalnızca okuma/kaydetme — silme/güncelleme yok
│   └── service/             # AuditLogService (fluent builder), AsyncAuditPersister
├── common/
│   ├── exception/           # GlobalExceptionHandler, BusinessException, ResourceNotFoundException
│   ├── response/            # ApiResponse<T> — standart response wrapper
│   └── util/                # SlugUtil (Türkçe karakter desteğiyle)
└── domain/
    ├── admin/               # Auth domain — AdminUser entity, login endpoint
    │   ├── controller/      # AuthController (POST /api/v1/auth/login)
    │   ├── service/         # AuthService, DataInitializer
    │   ├── repository/
    │   ├── entity/
    │   └── dto/request+response/
    ├── project/             # Ana domain — projeler, görseller, etiketler, malzemeler
    │   ├── controller/
    │   ├── service/
    │   ├── repository/
    │   ├── entity/
    │   ├── dto/request/
    │   ├── dto/response/
    │   └── enums/
    ├── media/               # Dosya yükleme, storage soyutlaması, dosya sunumu
    │   ├── controller/      # MediaController (yükleme), FileServeController (GET /files)
    │   ├── service/         # MediaService, StorageService (arayüz), LocalStorageService
    │   ├── repository/
    │   ├── entity/
    │   └── enums/
    └── contact/             # İletişim formu mesajları + e-posta bildirimi
        ├── controller/
        ├── service/         # ContactService, EmailNotificationService
        ├── repository/
        ├── entity/
        └── enums/
```

### Storage Mimarisi

```
MediaController.upload()
    → StorageService.upload()          ← arayüz (interface)
        → LocalStorageService          ← Aşama 1'de aktif (app.storage.type=local)
        → S3StorageService             ← Aşama 2   (app.storage.type=s3)
    → MediaFile DB'ye kaydedilir       ← sadece metadata, binary içerik asla DB'de olmaz
    → MediaFileResponse döner
```

---

## Modüller

### `domain/project`

Ana domain. `Project` aggregate root'tur.

| Sınıf | Sorumluluk |
|---|---|
| `Project` | Domain metodlarıyla entity: `publish`, `unpublish`, `addImage`, `removeImageById`, `applyPatch` |
| `ProjectImage` | Project ile MediaFile arasındaki join — sıralama ve tip bilgisiyle |
| `ProjectTag` / `ProjectMaterial` | Değer nesnesi benzeri alt kayıtlar, cascade ALL |
| `ProjectService` | Oluşturma/güncelleme/yayınlama orchestration'ı, slug çözümleme, etiket ve malzeme senkronizasyonu |
| `ProjectRepository` | N+1 önleyen `@EntityGraph` sorguları |
| `ProjectImageRepository` | Görselleri + media'yı tek sorguda getiren `JOIN FETCH` sorgusu |

**Cascade kuralları:**
- `images`, `tags`, `materials` → `CASCADE ALL + orphanRemoval` — proje silinince hepsi silinir
- `coverImage` → sadece FK, cascade yok — medya dosyası bağımsız bir varlıktır

**Güncelleme semantiği (PATCH):** `null` alan = mevcut değer korunur. Boş liste `[]` = temizle. Değer verilmişse güncelle.

**Validation sınırları:**
- `tags`: en fazla 15 etiket, her biri en fazla 50 karakter
- `materials`: en fazla 20 malzeme, her biri en fazla 100 karakter
- `detailedStory`: en fazla 50.000 karakter

### `domain/media`

Dosya yaşam döngüsünü yönetir. Binary dosya diskte (veya S3'te) durur. DB yalnızca metadata saklar.

| Sınıf | Sorumluluk |
|---|---|
| `MediaService` | Doğrular, StorageService ile yükler, MediaFile kaydeder |
| `LocalStorageService` | `uploads/` dizinine yazar, `FileServeController` üzerinden sunar |
| `MediaFile` | Metadata: originalName, storedName (UUID), url, mimeType, boyut, genişlik, yükseklik |
| `FileServeController` | `GET /files/{filename}` — path traversal korumasıyla dosya sunar |

**Dosya yükleme güvenliği:**
- MIME tipi izin listesine göre kontrol edilir (jpeg, png, webp, gif)
- Kaydedilen dosya adı UUID'dir — orijinal ad asla dosya sistemine yazılmaz
- Path traversal: `resolveSecurePath()` normalize eder ve yolun upload dizini içinde kalmasını garantiler
- Görsel URL'leri `APP_STORAGE_BASE_URL` env değişkeninden üretilir — production'da Railway URL'i set edilmeli

### `domain/contact`

Durumsuz iletişim formu. Ziyaretçi gönderir, admin okur ve durumu günceller.

| Sınıf | Sorumluluk |
|---|---|
| `ContactService` | `sendMessage()` IP ile kaydeder; admin metodları listeler/filtreler/günceller/siler |
| `ContactMessage` | Durum makineli entity: `YENİ → OKUNDU → YANITLANDI` |
| `EmailNotificationService` | Yeni mesajda async HTML e-posta gönderir; spam tespiti e-posta başlığını işaretler |

**Rate limiting:** Aynı IP'den günde en fazla 3 mesaj (`RateLimiterService`).

**Spam tespiti:** %80+ büyük harf, 6+ tekrar karakter veya URL içeriği → e-posta konusuna `⚠ [ŞÜPHELİ]` eklenir. Mesaj DB'ye kaydedilir, silinmez.

### `audit`

Değiştirilemez audit trail. Tüm admin ve public aksiyonlar loglanır.

| Sınıf | Sorumluluk |
|---|---|
| `AuditLogService` | Fluent builder: `record(action).entity(...).ip(...).meta(...).save()` |
| `AsyncAuditPersister` | Spring proxy için ayrı `@Component` — `@Async("auditExecutor")` doğru çalışır |
| `AuditLog` | Setter yok, immutable. Factory method: `AuditLog.of(...)`. `createdAt` factory'de set edilir |
| `AuditAction` | Enum: LOGIN_SUCCESS/FAILURE/BLOCKED, PROJECT_*, CONTACT_*, MEDIA_* |

**Önemli:** Repository'de `delete` ve `update` metodu kasıtlı olarak yoktur — audit kayıtları değiştirilemez.

### `common`

| Sınıf | Sorumluluk |
|---|---|
| `ApiResponse<T>` | Standart JSON wrapper: `{ success, data, message, errors }` |
| `GlobalExceptionHandler` | `BusinessException` (4xx), `ResourceNotFoundException` (404), `DataIntegrityViolationException` (409), `MethodArgumentNotValidException` (400) yakalar |
| `BusinessException` | HTTP statüsü taşır — service katmanı kural ihlallerinde bunu fırlatır |
| `SlugUtil` | Türkçe destekli slug üretici: `"Boğaziçi Dairesi"` → `"bogazici-dairesi"` |

---

## API Özeti

Tüm yanıtlar sarılır: `{ "success": true, "data": {...} }`

### Auth Endpoint'leri

| Metod | Yol | Açıklama |
|---|---|---|
| POST | `/api/v1/auth/login` | Admin girişi — JWT token döner |

**Rate limiting:** 5 başarısız denemeden sonra IP 15 dakika engellenir.

### Genel Endpoint'ler (auth gerektirmez)

| Metod | Yol | Açıklama |
|---|---|---|
| GET | `/api/v1/projects` | Yayınlanan projeler, sayfalı (boyut=12, tarihe göre) |
| GET | `/api/v1/projects/featured` | Öne çıkan yayınlanan projeler |
| GET | `/api/v1/projects/category/{category}` | Kategoriye göre, sayfalı |
| GET | `/api/v1/projects/{slug}` | Slug ile proje detayı |
| GET | `/files/{filename}` | Yüklenen dosyayı sun (local storage) |
| POST | `/api/v1/contact` | İletişim formu gönder (günde 3 / IP) |

### Admin Endpoint'leri (JWT korumalı — ADMIN rolü gerekli)

Token: `Authorization: Bearer <token>`

| Metod | Yol | Açıklama |
|---|---|---|
| GET | `/api/v1/admin/projects` | Tüm projeler (her statüs), sayfalı |
| GET | `/api/v1/admin/projects/{id}` | ID ile proje detayı |
| POST | `/api/v1/admin/projects` | Proje oluştur |
| PATCH | `/api/v1/admin/projects/{id}` | Kısmi güncelleme — null alanlar görmezden gelinir |
| DELETE | `/api/v1/admin/projects/{id}` | Proje sil |
| PATCH | `/api/v1/admin/projects/{id}/publish` | Yayınla |
| PATCH | `/api/v1/admin/projects/{id}/unpublish` | Yayından kaldır |
| PATCH | `/api/v1/admin/projects/{id}/featured?value=true` | Öne çıkar |
| PATCH | `/api/v1/admin/projects/{id}/cover-image/{mediaFileId}` | Kapak görseli ata |
| POST | `/api/v1/admin/projects/{id}/images/{mediaFileId}` | Galeriye görsel ekle |
| DELETE | `/api/v1/admin/projects/{id}/images/{imageId}` | Galeriden görsel kaldır |
| PUT | `/api/v1/admin/projects/{id}/images/reorder` | Galeri sırasını güncelle |
| POST | `/api/v1/admin/media/upload` | Dosya yükle |
| GET | `/api/v1/admin/contact` | Tüm iletişim mesajları, sayfalı |
| GET | `/api/v1/admin/contact/status/{status}` | Duruma göre filtrele |
| GET | `/api/v1/admin/contact/count/new` | YENİ mesaj sayısı |
| PATCH | `/api/v1/admin/contact/{id}/status` | Mesaj durumunu güncelle |
| DELETE | `/api/v1/admin/contact/{id}` | Mesaj sil |

---

## Veritabanı

### Tablolar

| Tablo | Açıklama |
|---|---|
| `projects` | Proje verileri, statü, öne çıkarma bayrağı, görüntülenme sayısı |
| `project_images` | Galeri görselleri — hem projects hem media_files'a FK |
| `project_tags` | Proje etiketleri, proje silinince cascade siliner |
| `project_materials` | Kullanılan malzemeler, proje silinince cascade siliner |
| `media_files` | Yalnızca upload metadata — binary disk/S3'te |
| `contact_messages` | İletişim formu gönderileri, durum takibiyle |
| `admin_users` | Admin hesapları — JWT auth aktif |
| `audit_logs` | Değiştirilemez audit trail — repository'de delete/update metodu yok |
| `comments` | Ziyaretçi yorumları — tablo hazır, entity bekliyor |

### Migration Sırası

| Versiyon | Dosya | İçerik |
|---|---|---|
| V1 | `V1__init_schema.sql` | Tüm tablolar |
| V2 | `V2__seed_admin_user.sql` | Placeholder — DataInitializer programatik olarak ekler |
| V3 | `V3__add_audit_logs.sql` | `audit_logs` tablosu + indeksler |

Migration'lar **Flyway** tarafından yönetilir. Hibernate `ddl-auto: validate` ile çalışır.

---

## Önemli Teknik Kararlar

### `StorageService` neden arayüz?
Storage backend'lerini iş mantığına dokunmadan değiştirmek için. `app.storage.type=s3` yapılınca `LocalStorageService` devre dışı kalır, `S3StorageService` devreye girer. Hiçbir çağıran değişmez.

### `MediaFile` neden ayrı entity?
Binary'yi DB'de saklamak ölçekte performans ve yedekleme felaketidir. `MediaFile` yalnızca metadata tutar. Tek bir `MediaFile` birden fazla proje tarafından referans alınabilir.

### Güncelleme neden PATCH, PUT değil?
`null` = mevcut değer korunsun. Boş `[]` = liste temizlensin. Değer verilmişse güncelle. `Project.applyPatch()` entity içinde zorunlu kılar.

### `@Async` neden `AsyncAuditPersister` ayrı bean'i?
`AuditLogService.record()` içinden `this.persist()` çağrısı Spring proxy'yi atlar — `@Async` çalışmaz. Ayrı `@Component` (`AsyncAuditPersister`) Spring proxy üzerinden inject edilir, `@Async` garanti çalışır.

### `@EntityGraph` neden `FetchType.EAGER` değil?
`EAGER` ihtiyaç olmasa bile her sorguda join çalıştırır. `@EntityGraph` sorgu bazlıdır: liste endpoint'leri yalnızca `coverImage` yükler, detay endpoint'leri tüm ilişkileri yükler. N+1 yok, fazla veri çekimi yok.

---

## Yerel Geliştirme

### Gereksinimler

- Java 17 (JDK)
- Maven 3.x
- Docker Desktop

### 1. PostgreSQL Başlat

```bash
docker-compose up -d
```

`portfolio_db`'yi `localhost:5432`'de başlatır. Flyway migration'ları ilk açılışta otomatik çalışır.

### 2. Backend'i Çalıştır

```bash
set JAVA_HOME=C:\Program Files\Java\jdk-17
cd apps/api
mvn spring-boot:run
```

Ya da IntelliJ'den `PortfolioApplication.java`'yı çalıştır.

**IntelliJ run config — Environment variables:**
```
APP_JWT_SECRET=<min 32 karakter>
APP_ADMIN_EMAIL=admin@portfolio.com
APP_ADMIN_PASSWORD=<şifre>
MAIL_USERNAME=interiorarcstudio@gmail.com
MAIL_APP_PASSWORD=<16 haneli Gmail Uygulama Şifresi>
APP_STORAGE_BASE_URL=http://localhost:8080/files
```

Gmail Uygulama Şifresi: Google Hesabı → Güvenlik → 2 Adımlı Doğrulama → Uygulama Şifresi

**VM parametresi:**
```
-Dspring.profiles.active=dev
```

**Production env değişkenleri (Railway):**
```
DATABASE_URL=<Railway postgres URL>
DATABASE_USERNAME=<db kullanıcı>
DATABASE_PASSWORD=<db şifre>
APP_JWT_SECRET=<min 32 karakter>
APP_ADMIN_EMAIL=<admin email>
APP_ADMIN_PASSWORD=<güçlü şifre>
MAIL_USERNAME=interiorarcstudio@gmail.com
MAIL_APP_PASSWORD=<16 haneli Gmail Uygulama Şifresi>
APP_STORAGE_BASE_URL=https://<railway-app>.railway.app/files
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
```

### 3. Doğrula

- API: `GET http://localhost:8080/api/v1/projects`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### Ortam Profilleri

| Profil | Datasource | SQL Log | Swagger |
|---|---|---|---|
| `dev` | `localhost:5432/portfolio_db` | AÇIK | AÇIK |
| `prod` | `$DATABASE_URL` env değişkeni | KAPALI | KAPALI |

### Java Versiyonu Notu

Birden fazla JDK varsa Maven mutlaka JDK 17 kullanmalı. OpenJDK 25 Lombok `TypeTag::UNKNOWN` hatasına yol açar.

---

## Geliştirme Yol Haritası

### ✓ Tamamlandı

- [x] JWT kimlik doğrulama — tüm `/admin/**` endpoint'leri
- [x] React/Next.js frontend — genel portfolyo sitesi + admin paneli
- [x] Rate limiting: iletişim formu günde 3/IP, giriş 5 başarısız → 15 dak blok
- [x] Audit log sistemi — async, değiştirilemez, `audit_logs` tablosu (V3 migration)
- [x] İletişim formu e-posta bildirimi — async, HTML şablonu, spam tespiti
- [x] CORS: `CORS_ALLOWED_ORIGINS` env değişkeni ile yapılandırılır
- [x] Storage base URL: `APP_STORAGE_BASE_URL` env değişkeni (production'da Railway URL)
- [x] Input validation: etiket/malzeme item-level `@Size`, `detailedStory` 50k karakter sınırı
- [x] Güvenlik başlıkları: Spring Security default'ları (X-Frame-Options, X-Content-Type-Options)
- [x] ISR: `revalidate = 300` + admin mutasyonlarında `POST /api/revalidate` ile anında güncelleme
- [x] Üretimde Swagger kapalı

### Bekleyen

- [ ] `S3StorageService` — `app.storage.type=s3` ile geçiş
- [ ] `MediaController` audit log'a bağlansın (MEDIA_UPLOADED / MEDIA_DELETED enum'da var, controller'da yok)
- [ ] Yorum sistemi — `comments` tablosu schema'da var, entity bekliyor
- [ ] Üretim için yapılandırılmış JSON loglama (Logstash encoder)
