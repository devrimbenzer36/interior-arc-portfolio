# Portfolyo Sitesi — Kullanım Kılavuzu

Bu belge, portfolyo sitesinin admin panelinde **neler yapabileceğini** anlatır.
Teknik detay içermez — her yeni özellik eklendiğinde bu belge de güncellenir.

---

## Panele Nasıl Giriş Yapılır?

Tarayıcında `/admin/login` adresini aç. E-posta ve şifreni gir.

> Giriş bilgilerin: `admin@portfolio.com` / `admin123`
> *(İleride değiştirilmesi önerilir.)*

Oturum açık kaldığı sürece tekrar giriş yapman gerekmez. Token süresi dolunca otomatik olarak giriş ekranına yönlendirilirsin.

---

## Şu An Neler Yapılabiliyor?

### Proje Yönetimi

Portfolyondaki her iç mimarlık projesi ayrı bir "proje" kaydıdır.
Her projeye şunları ekleyebilirsin:

- **Başlık** ve **kısa açıklama** (liste sayfasında görünen)
- **Kategori** (konut, ticari, ofis, otel/kafe, mağaza/showroom, diğer)
- **Kapak görseli** (liste kartında ve detay sayfasında görünen)

#### Projeyle yapabileceklerin:

| İşlem | Açıklama |
|---|---|
| **Taslak oluştur** | Proje ilk oluşturulduğunda taslak olarak kaydedilir, sitede görünmez |
| **Düzenle** | Başlık, açıklama, kategori ve kapak görselini değiştirebilirsin |
| **Sil** | Proje kalıcı olarak silinir (geri alınamaz) |

---

### Görsel Yükleme

Proje oluştururken veya düzenlerken kapak görseli ekleyebilirsin:

1. "Görsel seçin veya sürükleyin" alanına tıkla ya da dosyayı sürükle-bırak
2. Yükleme tamamlanınca görsel otomatik bağlanır

> **Desteklenen formatlar:** JPEG, PNG, WebP, GIF
> **Maksimum dosya boyutu:** 5 MB

---

### İletişim Mesajları

Sitedeki iletişim formunu dolduran ziyaretçilerin mesajlarını buradan görebilirsin.

#### Her mesajda şunlar bulunur:
- Ad soyad, e-posta, telefon (varsa)
- Mesaj içeriği
- Gönderilme tarihi

#### Mesaj durumları:

| Durum | Anlamı |
|---|---|
| **Yeni** | Henüz bakılmamış mesaj |
| **Okundu** | Mesajı açtın, otomatik olarak okundu işaretlenir |
| **Yanıtlandı** | Yanıt verdin, durumu kendin güncelleyebilirsin |

- Kaç yeni mesaj geldiğini panelde görebilirsin
- Mesaja tıklayınca içerik açılır ve otomatik "Okundu" olur

---

## Yakında Eklenecekler

### Aşama 3 — Genişletilmiş Proje Detayları

- **Uzun hikaye** alanı — proje detay sayfasında görünen tam açıklama
- **Mekan tipi, stil, konum, tarih, metrekare, bütçe aralığı**
- **Etiketler** ve **malzemeler**
- **Galeri yönetimi** — birden fazla görsel, sürükle-bırak sıralama, önce/sonra karşılaştırması

### Aşama 3 — İçerik Yönetimi

- **Yorum sistemi** — ziyaretçiler projelere yorum bırakabilecek, onaylama senin elinde olacak
- **Yayınla / Yayından kaldır** — panelden tek tıkla kontrol

### Aşama 4 — Bulut Depolama

- Görseller şu an sunucuda saklanıyor
- İleride Amazon S3 gibi bir bulut sistemine taşınacak
- Senin için hiçbir şey değişmez — arka planda otomatik gerçekleşir

---

## Teknik Destek

Bir şey çalışmıyorsa veya bir özellik eksik görünüyorsa geliştiricinizle iletişime geçin.

---

*Bu belge her yeni özellik eklendiğinde güncellenir.*
