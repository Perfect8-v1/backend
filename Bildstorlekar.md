Bildstorlekar - Best Practice för Perfect8 🖼️
Version 1.0 Rekommendation: 4 storlekar

java:
public enum ImageSize {
    THUMBNAIL(150, 150, "thumb"),      // Listor, varukorg
    SMALL(400, 400, "small"),          // Mobilvy, produktkort
    MEDIUM(800, 800, "medium"),        // Desktop produktvy
    LARGE(1600, 1600, "large");        // Fullskärm, zoom

    // Original behålls alltid separat
}

Varför just dessa?

1. THUMBNAIL (150x150)
    Produktlistor
    Sökresultat
    Varukorg
    Admin-paneler

2. SMALL (400x400)
    Mobil produktvy
    Kategori-grid
    Blog preview
3. MEDIUM (800x800)
    Desktop produktvy
    iPad/tablet
    Blog-artikel

4. LARGE (1600x1600)
    Produktzoom
    Retina displays
    Hero-bilder



Filnamn-konvention:
produktbild_12345_thumb.webp    (150x150)
produktbild_12345_small.webp    (400x400)
produktbild_12345_medium.webp   (800x800)
produktbild_12345_large.webp    (1600x1600)
produktbild_12345_original.jpg  (behåll original)

Frontend Best Practice:
html:
<!-- Responsive images med srcset -->
<img
    src="image_medium.webp"
    srcset="
        image_small.webp 400w,
        image_medium.webp 800w,
        image_large.webp 1600w"
    sizes="
        (max-width: 640px) 100vw,
        (max-width: 1024px) 50vw,
        33vw"
    alt="Produktbild"
/>

Flutter/Mobile:
dart:
// Flutter väljer rätt storlek baserat på skärm
Image.network(
    getImageUrl(size: MediaQuery.of(context).size.width > 400
    ? ImageSize.MEDIUM
    : ImageSize.SMALL)
)

Version 2.0 (Framtida):

1. WebP + AVIF format
2. CDN med on-demand resizing
3. Art direction (olika beskärningar)
4. Progressive loading

Image Entity för Version 1.0:
java:
@Entity
    public class Image {
    private String originalUrl;
    private String thumbnailUrl;  
    private String smallUrl;
    private String mediumUrl;
    private String largeUrl;
    private String format;  // "JPEG", "PNG", "WEBP"
    private Integer originalWidth;
    private Integer originalHeight;
}

Lagrings-tips:

Version 1.0: Lokal disk (eller enkel S3)
Generera alla storlekar vid uppladdning
WebP som standard (30-50% mindre)
Fallback till JPEG för äldre browsers

Vill du att jag skapar:
