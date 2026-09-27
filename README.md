💎 Blok Blast — Android Boshqotirma O'yini
Blok Blast — bu zamonaviy Kotlin va Jetpack Compose texnologiyalari asosida yaratilgan, yorqin 3D javohir bloklar, qiziqarli sarguzasht bosqichlari va kuchli kombo portlashlarga ega arkada-boshqotirma o'yini.
✨ Asosiy Imkoniyatlar
🎨 8 Xil Rang-barang 3D Javohir Bloklar:
Qizil Yoqut (Ruby), Yashil Zumrad (Emerald), Moviy Sapfir (Sapphire), Oltin Kahrabo (Amber), Binafsha Ametist (Amethyst), Pushti Marjon (Coral), Feruza Kristal (Cyan) va Olovli Apelsin (Tangerine).
🗺️ 3 Xil O'yin Rejimi:
Sarguzasht Bosqichlari (15 ta Bosqich): Har bir bosqich o'ziga xos doska tuzilishi va vazifalarga ega (Ilk Qadamlar, Javohir Ovchisi, Muzlik Davri, Oltin Piramida, Bomba Maydoni, Zumrad Labirint, Afsonaviy Blast Ustasi va boshqalar).
Klassik Cheksiz Rejim: Cheksiz o'ynab, eng yuqori rekord o'rnatish rejimi.
Kunlik Oltin Sinov: Maxsus olmosli va bombali maydonda kunlik vazifani bajarib, qo'shimcha tangalar yutib olish imkoniyati.
🧊 Maxsus O'yin Elementlari:
Olmoslar (Gems): Qator yoki ustun to'lganda yig'iladigan qimmatbaho javohirlar.
Muz Qatlamlari (1 va 2 qavatli): Bloklarni qator to'ldirib eritish kerak bo'lgan muz to'siqlar.
Bomba Bloklar: Qator to'lganda atrofdagi 3x3 maydonni zanjirli reaksiya bilan portlatuvchi bloklar.
Tosh Devorlar: Bosqichlarni qiziqarliroq qiluvchi maxsus to'siqlar.
⚡ Kuchaytirgichlar (Boosters) va Do'kon:
🔨 Bolg'a: Doskadagi istalgan 1 ta blok yoki muzni sindiradi.
💣 Bomba 3x3: Tanlangan 3x3 maydonni portlatib tozalaydi.
🔄 Burish 90°: Navbatdagi barcha shakllarni 90 darajaga buradi.
🎲 Yangilash: Navbatdagi 3 ta shaklni yangisiga almashtiradi.
👆 Qulay Boshqaruv:
Shakllarni barmoq bilan sudrab joylashtirish (Drag & Drop) yoki bosib joylashtirish (Tap to Place).
Shakl qo'yiladigan joy va to'ladigan qator/ustunlarni oldindan oltin rangda ko'rsatish (Ghost Preview).
🔊 Ovoz, Tebranish va Saqlash:
Har bir harakat, kombo portlash va g'alaba uchun sintezlangan ovoz hamda tebranish (Haptic) effektlari.
Barcha yulduzlar, tangalar, rekordlar va o'yinlar tarixi Room (SQLite) mahalliy ma'lumotlar bazasida avtomatik saqlanadi.
🛠️ Texnologik Stek
Til: Kotlin
UI Framework: Jetpack Compose (Material Design 3 + Custom Canvas 2D/3D Drawing)
Arxitektura: MVVM (Model-View-ViewModel) + Clean Repository Pattern
Ma'lumotlar Bazasi: Jetpack Room (KSP bilan)
Asinxronlik: Kotlin Coroutines & StateFlow
Audio & Haptics: Android AudioTrack (Real-time Sound Synthesis) & Vibrator API
📂 Loyiha Tuzilishi
code
Text
app/src/main/java/com/example/
├── data/
│   ├── local/          # Room Database, DAO va Entity fayllari
│   └── repository/     # O'yin natijalari va do'kon logikasi (GameRepository)
├── model/              # Blok shakllari, ranglar va 15 ta bosqich katalogi
├── ui/
│   ├── components/     # 8x8 interaktiv Canvas doska va 3D blok chizish
│   ├── screens/        # Asosiy menyu, Bosqichlar, O'yin va Do'kon ekranlari
│   └── theme/          # Kosmik arkada ranglar palitrasi va tipografiya
├── util/               # Ovoz va tebranish (Haptic) menejeri
├── viewmodel/          # O'yin qoidalari, kombo va fizika holati (BlockBlastViewModel)
└── MainActivity.kt     # Ilova kirish nuqtasi va navigatsiya
🚀 O'rnatish va Ishga Tushirish
Repozitoriyani klon qiling yoki ZIP shaklida yuklab oling:
code
Bash
git clone <repository-url>
Loyihani Android Studio dasturida oching.
Gradle sinxronizatsiyasi yakunlanishini kuting va Run (Shift + F10) tugmasini bosing.
APK fayl yaratish uchun terminalda quyidagi buyruqni bajaring:
code
Bash
./gradlew assembleDebug
