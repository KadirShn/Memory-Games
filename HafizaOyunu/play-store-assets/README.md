# Google Play mağaza varlıkları

Bu klasör, `com.kadirsahin.hafizaoyunu` paketli uygulamanın Google Play mağaza girişi için hazırlanmıştır.

## Yüklenecek dosyalar

- Uygulama simgesi: `graphics/app-icon-512.png`
  - 512 x 512 piksel
  - 32 bit PNG, opak tam kare
  - 360.043 bayt (1 MB sınırının altında)
- Özellik grafiği: `graphics/feature-graphic-1024x500.png`
  - 1.024 x 500 piksel
  - 24 bit, alfa kanalsız PNG
  - 1.173.228 bayt (15 MB sınırının altında)
- Telefon ekran görüntüleri: `screenshots-tr/`
  - Sekiz adet Türkçe, gerçek uygulama ekranı (3.0 sürümü, koyu tema, emülatörde gerçek oynanışla alındı)
  - Her biri 1.080 x 1.920 piksel (9:16; Play'in en fazla 2:1 oran sınırına uygun)
  - 24 bit, alfa kanalsız PNG
  - Durum çubuğu Android demo moduyla temizlendi (12:00, tam pil, bildirim yok)

Önerilen ekran görüntüsü sırası:

1. `01-ana-ekran.png` — ana menü, seviye, altın, günlük seri
2. `02-mod-secimi.png` — 6 oyun modu
3. `03-oyun-tahtasi.png` — oyun sırasında 4× seri anı
4. `04-ayarlar.png` — ses, titreşim, tema ve hareketi azaltma
5. `05-kart-temalari.png` — 5 kart paketi
6. `06-macera-haritasi.png` — macera dünyası ve yıldızlar
7. `07-sonuc-ekrani.png` — 3 yıldızlı sonuç, puan dökümü, ödüller
8. `08-profil.png` — seviye ve istatistikler

2.0 sürümünün eski ekran görüntüleri `eski-ekran-goruntuleri-v2/` klasöründedir; Play'e yüklenmemelidir.

`source/` klasörü, mağaza görsellerinin düzenlenebilir AI kaynaklarını içerir; Google Play'e doğrudan yüklenmesi gerekmez.

## AI üretim kayıtları

Üretim modu: varsayılan (brand/logo ve ads-marketing kullanım durumları).

### Uygulama simgesi ön planı

```text
Use case: logo-brand
Asset type: original Android memory-game app icon foreground artwork
Primary request: Create a single distinctive icon mark for a premium casual memory matching game: two overlapping square memory cards, slightly offset and tilted, with a bright four-point focus sparkle centered between them, suggesting matching, recall and concentration.
Scene/backdrop: genuinely transparent background; no canvas color and no outer container.
Subject: exactly two thick rounded-square cards; the rear card is deep violet with one simple cyan circular gem, the front card is indigo with one simple warm-gold four-point sparkle; both cards are generic and fully original.
Style/medium: polished 3D clay-style mobile game icon, clean friendly geometry, tactile matte surfaces, subtle internal highlights, strong silhouette, modern and playful.
Composition/framing: centered compact mark, front-facing slight three-quarter depth, balanced symmetry, generous transparent padding around the complete mark, readable at 48px, all artwork inside a central safe area.
Lighting/mood: soft studio rim light, energetic and clever, premium casual-game finish.
Color palette: indigo, violet, electric cyan, warm gold and a small coral accent.
Constraints: transparent background with preserved alpha; exactly two cards and one sparkle; no outer rounded-square background, no outer drop shadow, no border, no text, letters, numbers, question marks, brain/head silhouette, logo, brand, watermark, signature, character, face, hands or device mockup; wholly original; no elements touching the canvas edge.
```

### Özellik grafiği arka planı

```text
Use case: ads-marketing
Asset type: Google Play feature graphic background for an original memory matching game
Input images: Image 1 is a style and color reference for the app's newly created two-card icon mark; match its premium 3D clay materials, indigo-violet-cyan-gold palette and friendly casual-game polish, but do not simply enlarge or repeat the icon composition.
Primary request: Create a vibrant, cinematic 2:1 landscape promotional artwork that communicates memory-card matching and concentration at a glance.
Scene/backdrop: rich indigo-to-violet atmospheric gradient with soft cyan and warm-gold light trails, subtle depth and gentle particles; avoid pure black, pure white and dark gray.
Subject: an elegant flowing arc of six original rounded-square memory cards in perspective, with three face-down cards and three face-up cards showing simple generic motifs: a golden focus sparkle, a luminous forest leaf and a tiny ringed cosmic planet. The cards should feel like they are flipping and finding their pairs.
Style/medium: cohesive premium 3D clay-style casual mobile-game key art, tactile matte cards, controlled glossy highlights, clean silhouettes, polished Google Play marketing quality.
Composition/framing: wide 2:1 canvas; keep the primary card arc and focal glow within the central 60% safe area; preserve clean negative space in the center-left for a short title to be added later; keep decorative elements away from outer edges and cutoff zones; no device frame.
Lighting/mood: energetic, clever, magical and inviting; soft volumetric glow and controlled rim lighting.
Color palette: deep indigo, vivid violet, electric cyan, warm gold, small coral accents.
Constraints: no text, letters, numbers, labels, app name, logo, brand, Google Play badge, store icon, rankings, awards, price, call-to-action, watermark, signature, people, hands, faces, characters or third-party imagery; no transparent background; fully filled opaque background; wholly original generic designs; avoid overcrowding and tiny details.
```
