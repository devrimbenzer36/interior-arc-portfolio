# Portfolio API — Türkçe Dokümantasyon

İç mimar portfolyo platformu için Spring Boot backend.
Ziyaretçilere açık bir portfolyo sitesi ve projeleri, görselleri ve iletişim mesajlarını yönetmek için bir admin panel API'si sunar.

---

## Bu Sistem Ne Yapar?

- Yayınlanan iç mimarlık **projelerini** ziyaretçilere sunar (filtreleme, sayfalama, slug ile erişim)
- **Görselleri** yönetir: yükleme, galeriye ekleme, kapak görseli atama
- **Admin panel API'si** sunar: proje oluşturma/düzenleme, yayınlama, öne çıkarma
- **İletişim formu mesajlarını** takip eder (YENİ → OKUNDU → YANITLANDI)

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
├── config/                  # SecurityConfig, OpenApiConfig
├── security/                # JWT auth altyapısı
│   ├── filter/              # JwtAuthFilter (OncePerRequestFilter)
│   ├── handler/             # AuthEntryPoint (401), AccessDeniedHandlerImpl (403)
│   ├── service/             # UserDetailsServiceImpl
│   └── util/                # JwtUtil — üret / parse / doğrula
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
    └── contact/             # İletişim formu mesajları
        ├── controller/
        ├── service/
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

### `domain/contact`

Durumsuz iletişim formu. Ziyaretçi gönderir, admin okur ve durumu günceller.

| Sınıf | Sorumluluk |
|---|---|
| `ContactService` | `sendMessage()` IP ile kaydeder, admin metodları listeler/filtreler/günceller/siler |
| `ContactMessage` | Durum makineli entity: `YENİ → OKUNDU → YANITLANDI` |

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

**Login isteği:**
```json
{ "email": "admin@portfolio.com", "password": "admin123" }
```

**Login yanıtı:**
```json
{
  "success": true,
  "data": {
    "token": "<jwt>",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "email": "admin@portfolio.com",
    "role": "ADMIN"
  }
}
```

### Genel Endpoint'ler (auth gerektirmez)

| Metod | Yol | Açıklama |
|---|---|---|
| GET | `/api/v1/projects` | Yayınlanan projeler, sayfalı (boyut=12, tarihe göre) |
| GET | `/api/v1/projects/featured` | Öne çıkan yayınlanan projeler |
| GET | `/api/v1/projects/category/{category}` | Kategoriye göre, sayfalı |
| GET | `/api/v1/projects/{slug}` | Slug ile proje detayı, görüntülenme sayısını artırır |
| GET | `/files/{filename}` | Yüklenen dosyayı sun (sadece local storage) |
| POST | `/api/v1/contact` | İletişim formu gönder |

### Admin Endpoint'leri (JWT korumalı — ADMIN rolü gerekli)

Token gönderimi: `Authorization: Bearer <token>`

| Metod | Yol | Açıklama |
|---|---|---|
| GET | `/api/v1/admin/projects` | Tüm projeler (her statüs), sayfalı |
| GET | `/api/v1/admin/projects/{id}` | ID ile proje detayı |
| POST | `/api/v1/admin/projects` | Proje oluştur (opsiyonel coverImageId, imageIds ile) |
| PATCH | `/api/v1/admin/projects/{id}` | Kısmi güncelleme — null alanlar görmezden gelinir |
| DELETE | `/api/v1/admin/projects/{id}` | Proje sil |
| PATCH | `/api/v1/admin/projects/{id}/publish` | Yayınla |
| PATCH | `/api/v1/admin/projects/{id}/unpublish` | Yayından kaldır |
| PATCH | `/api/v1/admin/projects/{id}/featured?value=true` | Öne çıkar |
| PATCH | `/api/v1/admin/projects/{id}/cover-image/{mediaFileId}` | Kapak görseli ata |
| POST | `/api/v1/admin/projects/{id}/images/{mediaFileId}` | Galeriye görsel ekle |
| DELETE | `/api/v1/admin/projects/{id}/images/{imageId}` | Galeriden görsel kaldır |
| PUT | `/api/v1/admin/projects/{id}/images/reorder` | Galeri sırasını güncelle |
| POST | `/api/v1/admin/media/upload` | Dosya yükle, MediaFileResponse döner |
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
| `comments` | Ziyaretçi yorumları — tablo hazır, entity Aşama 3'te |

### İlişkiler

```
projects
  ├── cover_image_id → media_files   (FK, ON DELETE SET NULL)
  ├── project_images → media_files   (FK, ON DELETE RESTRICT)
  │         └── project_id → projects (ON DELETE CASCADE)
  ├── project_tags   → project_id   (ON DELETE CASCADE)
  └── project_materials → project_id (ON DELETE CASCADE)
```

Migration'lar **Flyway** tarafından yönetilir. Hibernate `ddl-auto: validate` ile çalışır — schema'nın entity'lerle uyumunu doğrular, asla değiştirmez.

---

## Önemli Teknik Kararlar

### `StorageService` neden arayüz (interface)?

Storage backend'lerini iş mantığına dokunmadan değiştirmek için. `LocalStorageService`, `app.storage.type=local` olduğunda aktiftir. `s3` yapılınca `@ConditionalOnProperty` ile devre dışı kalır, `S3StorageService` devreye girer. `MediaService` ve her çağıran tamamen habersizdir.

### `MediaFile` neden ayrı entity?

Binary'yi DB'de saklamak ölçekte performans ve yedekleme felaketi olur. `MediaFile` yalnızca metadata tutar: dosyanın nerede olduğu, nasıl erişileceği, boyutları. Gerçek byte'lar dosya sisteminde (veya S3'te) durur. Ayrıca tek bir `MediaFile` birden fazla proje tarafından referans alınabilir.

### Güncelleme neden PATCH, PUT değil?

Portfolyo admini nadiren 12 alanın tamamını aynı anda günceller. PUT tüm nesnenin gönderilmesini zorunlu kılar. PATCH ile: sadece değişeni gönder. `null` = mevcut değer korunsun. Boş `[]` = liste temizlensin. Bu `Project.applyPatch()` içinde zorunlu kılınır — entity kendisini korur.

### `@EntityGraph` neden `FetchType.EAGER` değil?

`EAGER` ihtiyaç olmasa bile her sorguda join çalıştırır. `@EntityGraph` sorgu bazlıdır: liste endpoint'leri yalnızca `coverImage` yükler. Detay endpoint'leri `coverImage + tags + materials` yükler. Galeri görselleri ayrı optimize edilmiş `JOIN FETCH` sorgusuyla gelir. Sonuç: N+1 yok, fazla veri çekimi yok.

### Entity'lerde domain metodlar neden?

`project.setStatus("PUBLISHED")` bir setter'dır — hiçbir fikri yoktur. `project.publish()` bir domain metodudur — yarın "en az bir görseli olmalı" kuralını hiçbir service değişikliği olmadan uygulayabilir. İş kuralları korudukları entity'ye aittir.

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
# Windows — birden fazla JDK varsa JAVA_HOME'u açıkça belirt
set JAVA_HOME=C:\Program Files\Java\jdk-17
cd apps/api
mvn spring-boot:run
```

Ya da IntelliJ'den `PortfolioApplication.java`'yı `dev` profiliyle çalıştır.

**IntelliJ run config'de gerekli VM parametresi:**
```
-Dspring.profiles.active=dev
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

Birden fazla JDK varsa Maven mutlaka JDK 17 kullanmalı. OpenJDK 25 Lombok `TypeTag::UNKNOWN` hatasına yol açar. `JAVA_HOME`'u ayarlayın ya da sistem ortam değişkenlerine kalıcı olarak ekleyin.

---

## Geliştirme Yol Haritası

### Aşama 2 — Kimlik Doğrulama & Frontend ✓ Tamamlandı

- [x] Tüm `/admin/**` endpoint'leri için JWT kimlik doğrulama
- [x] `admin_users` tablo entegrasyonu
- [x] Üretimde Swagger kapalı
- [x] React/Next.js frontend — genel portfolyo sitesi + admin paneli (tam işlevsel)
- [ ] `S3StorageService` implementasyonu — `app.storage.type=s3` ile geçiş
- [ ] İletişim formunda rate limiting (Bucket4j)

### Aşama 3 — İçerik Özellikleri

- [ ] Yorum sistemi — `comments` tablosu schema'da var, entity bekliyor
- [ ] Yükleme sırasında MIME type magic bytes doğrulaması

### Aşama 4 — Gözlemlenebilirlik

- [ ] Micrometer + Prometheus metrikleri
- [ ] Üretim için yapılandırılmış JSON loglama
- [ ] İstek izleme başlıkları
