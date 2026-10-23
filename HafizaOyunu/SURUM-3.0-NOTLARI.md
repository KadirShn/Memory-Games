# Hafıza Ustası 3.0 — sürüm notları

3.0 ile uygulama basit bir hafıza oyunundan, ilerleme sistemi olan tam bir mobil oyuna dönüştü. Yeni kütüphane bağımlılığı eklenmedi; uygulama hâlâ tamamen çevrimdışı, reklamsız ve internet izni yok.

## Oynanış

- **6 oyun modu:** Macera (5 dünya, 30 seviye, yıldız sistemi), Klasik (4×4 / 4×5 / 6×6, zamana karşı), Günlük meydan okuma (tarihten tohumlanan, herkes için aynı tahta + günlük seri), Hayatta kalma (her turda büyüyen sonsuz tahtalar, canlar), Zen (süresiz) ve Düello (tek cihazda 2 oyuncu, eşleştiren tekrar oynar).
- **Sürpriz mekanikler:** Gözü kapalı (önizlemesiz), Karıştırma (her 3 hatada kapalı kartlar yer değiştirir), Buz (önce kırılması gereken kartlar), Altın çift (×3 puan), Kum saati (+8 sn).
- **Adil puanlama:** Eşleşme 100 puan, art arda eşleşmelerde ×4'e kadar seri çarpanı, süre ve kusursuzluk bonusu. Yeni kart keşfetmek ceza almaz; yalnızca daha önce görülmüş bir kartı unutmak "hafıza hatası" sayılır (−25 puan, hayatta kalmada −1 can).
- **Güçlendiriciler:** Göz at, İpucu, Dondur.
- Her oyunda 18 görselden rastgele seçim yapılır; aynı tahta hiç tekrar etmez (günlük mod hariç, orada bilinçli olarak herkes aynı tahtayı oynar).

## İlerleme

- XP, oyuncu seviyesi ve unvanlar (Çaylak → Efsane), altın, 23 başarım, ayrıntılı istatistik ekranı, günlük seri.
- Mağaza: güçlendiriciler ve prosedürel çizilen 7 kart arkası (gerçek para yok).
- 5 kart paketi: mevcut 3 görsel pakete ek olarak vektörel **Neon Şekiller** (her çiftin farklı silueti var, renk körlüğüne uygun) ve **Hayvan Dostlar**.

## Görsel ve işitsel his

- 3D kart çevirme, dağıtma animasyonu, eşleşmede parçacık patlaması, zaferde konfeti, yüzen puan etiketleri, seri ve olay bannerları, zaman halkası, hareketli arka plan.
- Kodla sentezlenen 14 ses efekti (APK'ya ses dosyası eklenmez), ayara bağlı dokunsal geri bildirim.
- Yeni koyu/açık marka paleti, kendi çizilmiş ikon seti, ilk açılışta 3 sayfalık tanıtım.

## Teknik

- Saf Kotlin `GameEngine` (seri, hata tespiti, buz, karıştırma, düello, ipucu) ve `Progression` (ödüller, başarımlar, seviye, günlük seri) — 20 birim testi.
- Profil verisi DataStore'da (`memory_profile`); skor tablosu v2 şemasına (hamle + tarih sütunları) eski kayıtlar silinmeden taşınır.
- Türkçe kaynaklar `values-tr` altına taşındı (yalnızca tr-TR değil tüm Türkçe cihazlar Türkçe görür).
- Bazı Android 14 sürümlerinde sistem titreşim servisini çökerten üst üste kısa titreşimleri önlemek için titreşimler 80 ms aralıkla sınırlanır.
- `versionCode 30000`, `versionName 3.0.0`.

## Yayın öncesi

1. `keystore.properties` ile imzalayıp `bundleRelease` alın (şu anki `.aab` imzasız).
2. Eski 2.0 sürümünün üzerine kurulumda skorların taşındığını gerçek cihazda doğrulayın.
3. Mağaza görsellerini (`play-store-assets/`) yeni ekranlarla yenileyin.
