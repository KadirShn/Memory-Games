# Hafıza Ustası 3.0

2022 tarihli ilk sürümün, Kotlin ve Jetpack Compose ile baştan tasarlanmış modern Android sürümüdür. 3.0 ile uygulama basit bir hafıza oyunundan, ilerleme sistemi olan tam bir mobil oyuna dönüştü. Ayrıntılı değişiklik listesi [SURUM-3.0-NOTLARI.md](SURUM-3.0-NOTLARI.md) dosyasındadır.

## Özellikler

- **6 oyun modu:** Macera (5 dünya, 30 seviye, yıldız sistemi), Klasik (4×4 / 4×5 / 6×6), Günlük meydan okuma (herkes için aynı tohumlu tahta + günlük seri), Hayatta kalma (büyüyen sonsuz tahtalar, canlar), Zen (süresiz) ve Düello (tek cihazda 2 oyuncu).
- **Sürpriz mekanikler:** Gözü kapalı, Karıştırma, Buz, Altın çift, Kum saati.
- **Adil puanlama:** ×4'e kadar seri çarpanı, süre ve kusursuzluk bonusu. Yeni kart keşfetmek ceza almaz; yalnızca daha önce görülen bir kartı unutmak "hafıza hatası" sayılır.
- **İlerleme:** XP ve oyuncu seviyesi, altın, 23 başarım, istatistikler, günlük seri.
- **Mağaza (gerçek para yok):** Göz at / İpucu / Dondur güçlendiricileri ve prosedürel çizilen 7 kart arkası.
- **5 kart paketi:** Klasik Lezzetler, Kozmik Yolculuk, Büyülü Orman, vektörel Neon Şekiller ve Hayvan Dostlar.
- **Oyun hissi:** 3D kart çevirme, dağıtma animasyonu, parçacıklar, konfeti, seri bannerları; kodla sentezlenen 14 ses efekti ve dokunsal geri bildirim.
- **Erişilebilirlik ve dil:** Hareketi azalt seçeneği, kart açıklamaları, renk körlüğüne uygun Neon paketi; Türkçe ve İngilizce.
- **Gizlilik:** Tamamen çevrimdışı ve reklamsız; internet izni, reklam SDK'sı, hesap veya takip yok.

## Teknik temel

- Android 16 üretim hedefi (`targetSdk 36`), `compileSdk 37`, `minSdk 24`
- Kotlin 2.4, AGP 9.3, Gradle 9.7, Java 17
- Jetpack Compose + Material 3, tek Activity, ViewModel + StateFlow
- Saf Kotlin oyun motoru (`domain/GameEngine.kt`) ve ilerleme kuralları (`domain/Progression.kt`), 20 birim testi
- DataStore (ayarlar ve profil) ve eski SQLite skor tablosuyla geriye uyumlu, şema göçlü veri katmanı
- Yeni harici kütüphane yok: ikonlar, kart arkaları, efektler ve sesler kodla üretiliyor

## Android Studio ile çalıştırma

Ön koşul: Gradle JDK olarak Java 17 veya üzeri.

1. Android Studio'da **File > Open** ile bu `HafizaOyunu` klasörünü açın.
2. Gradle eşitlemesinin tamamlanmasını bekleyin.
3. Bir emülatör başlatın veya USB hata ayıklaması açık bir telefon bağlayın.
4. `app` yapılandırmasını seçip **Run** (`Shift+F10`) düğmesine basın.

## Komutlar

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
```

## Google Play sürümü

İmzalı paket Android Studio'da **Build > Generate Signed App Bundle or APK… > Android App Bundle** ile alınır. İmza anahtarı (`.jks`), şifreler ve `.aab` dosyaları `.gitignore` ile depo dışında tutulur; bunları asla depoya eklemeyin. Yüklemeden önce `versionCode` değerinin Play Console'daki son sürümden büyük olduğundan emin olun.

Mağaza görselleri ve ekran görüntüleri [play-store-assets](play-store-assets/README.md) klasöründedir. AI görsellerinin üretim kayıtları [ASSET_PROVENANCE.md](ASSET_PROVENANCE.md) dosyasındadır.
