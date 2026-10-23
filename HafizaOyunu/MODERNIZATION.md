# Hafıza Oyunu modernizasyon planı

## Tamamlanan 2.0 dönüşümü

### Platform ve yayın uygunluğu

- Play'in Android 16 şartı için `targetSdk 36` ayarlandı.
- Güncel kararlı araç zincirine geçildi: AGP 9.3.2, Gradle 9.7.1, Kotlin 2.4.10 ve Java 17.
- Eski ve kaldırılmış Kotlin Android Extensions tamamen çıkarıldı.
- Üretim derlemesinde R8 küçültme ve kaynak optimizasyonu etkinleştirildi.
- Edge-to-edge sistem çubukları, modern splash screen ve Android yedekleme kuralları eklendi.

### Mimari

- Altı Activity ve üç kopya oyun sınıfı, tek Activity + Compose akışında birleştirildi.
- Oyun kuralları Android'den bağımsız saf Kotlin `GameEngine` içine alındı.
- Sayaç ve ekran durumu `GameViewModel` ile yaşam döngüsüne bağlandı; uygulama arka plana gidince oyun duraklıyor.
- Skorlar ve ayarlar reaktif akışlarla UI'a aktarılıyor.
- Mevcut kullanıcıların `HafizaOyunu2/Skor` verileri silinmeden okunmaya devam ediyor.

### Oynanış ve kalite

- Kart açılış animasyonu, başlangıç önizlemesi, seri göstergesi, dokunsal geri bildirim ve sonuç başarıları eklendi.
- Yanlış eşleşme görünür kalıp otomatik kapanıyor; bu sırada hatalı çoklu dokunmalar engelleniyor.
- 4×5 modunun 8. eşleşmede skor kaydetmesi ve tekrar oynarken 4×4'e dönmesi hataları giderildi.
- Ekran dönüşü ve farklı boyutlar için sabit XML ölçüleri kaldırıldı; responsive Compose grid kullanıldı.
- Skor tablosu puan eşitliğinde süreye göre sıralanıyor; veri temizleme için güvenli onay var.
- Türkçe/İngilizce, erişilebilir kart açıklamaları, açık/koyu/sistem teması ve Material You renkleri eklendi.
- DataStore ile kalıcı çalışan Klasik, Kozmik Yolculuk ve Büyülü Orman kart temaları eklendi; eski klasik çizimler özgün, modern 3D yiyecek görselleriyle yenilendi.
- Eski AdMob/reklam altyapısı tamamen kaldırıldı; uygulamada reklam SDK'sı, reklam kimliği veya `AD_ID` izni bulunmuyor.
- Kozmik ve orman paketlerinin 36 kart görseli marka, karakter ve sanatçı referansı kullanılmadan AI ile özgün olarak üretildi.
- Saf oyun motoru için eşleşme, hata kilidi, kazanma ve önizleme birim testleri yazıldı.

## Yayına çıkmadan önce

1. Android 16 fiziksel cihaz/emülatörde 4×4, 4×5 ve 6×6 turlarını tamamlayın.
2. Eski mağaza sürümünün üstüne kurulum yapıp skorların taşındığını doğrulayın.
3. `versionCode = 20000` değerini Play Console'daki son kodla karşılaştırın ve gerekirse yükseltin.
4. Kendi release keystore'unuzu yerel `keystore.properties` üzerinden bağlayın; anahtarı depoya eklemeyin.
5. `bundleRelease`, lint ve test çıktıları temizken `.aab` dosyasını önce dahili test kanalına yükleyin.
6. Pre-launch report, Android vitals, erişilebilirlik ve küçük ekran sonuçlarını inceleyin.

## Sonraki ürün fazları

- Günlük görevler, ardışık gün serisi ve yerel başarı koleksiyonu
- Süresiz Zen modu ve ileride indirilebilir yeni kart paketleri
- Opsiyonel Play Games Services başarıları/bulut skorları (açık kullanıcı onayıyla)
- Tablet ve katlanabilir cihazlarda iki panelli skor/oyun yerleşimi
- Compose UI testleri, baseline profile ve Macrobenchmark performans modülü
- Erişilebilirlik için renk körlüğü paletleri ve azaltılmış hareket tercihi

Bu fazlar bilinçli olarak 2.0 çekirdeğinden ayrıdır: internet hesabı, analitik veya servis eklemek yeni veri güvenliği beyanları ve Play Console yapılandırması gerektirir.
