# Portfolyo Sitesi — Kullanım Kılavuzu

Bu belge, **Elif Benzer — Interior Arc** portfolyo sitesinin admin panelinde neler yapabileceğini anlatır.
Teknik detay içermez.

---

## Panele Nasıl Giriş Yapılır?

Tarayıcında `/admin/login` adresine git. E-posta ve şifreni gir.

Oturum açık kaldığı sürece tekrar giriş yapman gerekmez. 24 saat sonra otomatik olarak çıkış yapılır ve giriş ekranına yönlendirilirsin.

---

## Proje Yönetimi

Portfolyondaki her iç mimarlık projesi ayrı bir kayıttır. Her projeye şunları ekleyebilirsin:

| Alan | Zorunlu | Açıklama |
|---|---|---|
| Başlık | Evet | Proje adı |
| Kısa açıklama | Hayır | Liste sayfasında kartın altında görünen metin |
| Uzun hikaye | Hayır | Proje detay sayfasında görünen tam açıklama |
| Kategori | Evet | Konut, ticari, ofis, otel/kafe, mağaza, diğer |
| Stil | Hayır | Minimalist, modern, klasik vb. |
| Mekan tipi | Hayır | Salon, mutfak, yatak odası vb. |
| Konum | Hayır | Şehir veya mahalle adı |
| Proje tarihi | Hayır | Tamamlanma tarihi |
| Metrekare | Hayır | Alan büyüklüğü |
| Bütçe aralığı | Hayır | Bilgilendirme amaçlı |
| Etiketler | Hayır | En fazla 15 etiket, her biri en fazla 50 karakter |
| Malzemeler | Hayır | En fazla 20 malzeme, her biri en fazla 100 karakter |
| Kapak görseli | Hayır | Liste kartında ve detay sayfasında görünen ana görsel |
| Galeri görselleri | Hayır | Detay sayfasında görüntülenebilir galeri |

### Proje Durumları

| Durum | Ne anlama gelir |
|---|---|
| **Taslak** | Sadece sende görünür, ziyaretçiler göremez |
| **Yayında** | Herkes görebilir |

### Yapabileceklerin

| İşlem | Açıklama |
|---|---|
| **Oluştur** | Proje taslak olarak oluşturulur |
| **Düzenle** | Tüm alanları sonradan değiştirebilirsin |
| **Yayınla / Yayından kaldır** | Tek tıkla kontrol |
| **Öne çıkar** | Ana sayfadaki "Öne Çıkan Projeler" bölümüne ekler |
| **Sil** | Proje kalıcı olarak silinir — geri alınamaz |

---

## Görsel Yönetimi

### Görsel Yükleme

1. "Görsel seçin veya sürükleyin" alanına tıkla ya da dosyayı sürükle-bırak
2. Yükleme tamamlanınca görsel otomatik bağlanır

> **Desteklenen formatlar:** JPEG, PNG, WebP, GIF
> **Maksimum boyut:** 20 MB

### Kapak Görseli

Projenin liste kartında ve detay sayfasının üstünde görünen ana görseldir.
Proje oluştururken veya sonradan düzenlerken atanabilir.

### Galeri

Proje detay sayfasında büyütülebilir galeri olarak görünür.
- Görsellerin sırası sürükle-bırak ile değiştirilebilir
- Ziyaretçiler her görsele tıklayarak tam ekran açabilir ve ok tuşları/kaydırma ile geçiş yapabilir

---

## İletişim Mesajları

Sitedeki iletişim formunu dolduran ziyaretçilerin mesajları buradan görüntülenir.
**Aynı zamanda `elifdavulcu8@gmail.com` ve `devrimbenzer@gmail.com` adreslerine otomatik e-posta gönderilir.**

### Her mesajda şunlar bulunur

- Ad soyad, e-posta, telefon (varsa), konu (varsa)
- Mesaj içeriği
- Gönderilme tarihi ve saati

### Mesaj Durumları

| Durum | Anlamı |
|---|---|
| **Yeni** | Henüz bakılmamış |
| **Okundu** | Mesajı açtın — otomatik işaretlenir |
| **Yanıtlandı** | Yanıt verdin — durumu kendin güncelleyebilirsin |

Panelin üstünde kaç yeni mesaj beklediğini görebilirsin.

### E-posta Bildirimi

Ziyaretçi form gönderdiğinde otomatik olarak bir bildirim e-postası gelir:
- **Kimden:** Interior Arc Studio (`interiorarcstudio@gmail.com`)
- **Kime:** Her iki adrese aynı anda
- **İçerik:** Ad soyad, e-posta (tıklanabilir), telefon, konu, mesaj, gönderim saati

Mesaj spam veya bot kaynaklı görünüyorsa konu satırı **⚠ [ŞÜPHELİ]** ile başlar — silmeden önce kontrol et.

### Mesaj Gönderim Limiti

Kötüye kullanımı önlemek için aynı IP adresinden günde en fazla **3 mesaj** gönderilebilir.
Gerçek bir ziyaretçi için bu limit yeterlidir.

---

## Güvenlik Notları

- Admin paneline günde 5 başarısız giriş denemesinden sonra o IP adresi 15 dakika boyunca engellenir
- Tüm işlemler kayıt altına alınır (kim, ne zaman, hangi IP'den)
- Şifreni kimseyle paylaşma; gerekirse geliştiriciyle iletişime geçerek değiştirt

---

## Teknik Destek

Bir şey çalışmıyorsa veya bir özellik eksik görünüyorsa geliştiriciyle iletişime geç.

---

*Son güncelleme: Nisan 2026*
