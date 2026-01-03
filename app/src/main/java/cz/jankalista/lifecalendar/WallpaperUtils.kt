package cz.jankalista.lifecalendar

import android.app.WallpaperManager
import android.content.Context
import android.graphics.*
import android.net.Uri
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max

fun updateWallpaper(context: Context, birthDate: LocalDate, imageUriString: String? = null) {
    // 1. ZJISTÍME ROZLIŠENÍ DISPLEJE TELEFONU
    // Abychom vytvořili tapetu přesně na míru, bez ohledu na to, jak velká je zdrojová fotka.
    val metrics = context.resources.displayMetrics
    val screenWidth = metrics.widthPixels
    val screenHeight = metrics.heightPixels

    // 2. NAČTEME ZDROJOVÝ OBRÁZEK (Z URI nebo Resources)
    val options = BitmapFactory.Options().apply { inMutable = false } // Teď nám stačí immutable, budeme ho kreslit jinam
    var sourceBitmap: Bitmap? = null

    if (imageUriString != null) {
        try {
            val inputStream = context.contentResolver.openInputStream(Uri.parse(imageUriString))
            sourceBitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()
        } catch (e: Exception) { e.printStackTrace() }
    }

    if (sourceBitmap == null) {
        sourceBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.nothing_wallpaper, options)
    }

    if (sourceBitmap == null) return // Pokud vše selže

    // 3. VYTVOŘÍME NOVÉ PLÁTNO PŘESNĚ PODLE DISPLEJE
    // Toto je to "Final Bitmap", které bude mít vždy správný poměr stran
    val finalBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(finalBitmap)

    // 4. "CENTER CROP" - Vypočítáme, jak fotku roztáhnout a oříznout
    val sourceWidth = sourceBitmap.width
    val sourceHeight = sourceBitmap.height

    val scaleX = screenWidth.toFloat() / sourceWidth
    val scaleY = screenHeight.toFloat() / sourceHeight
    // Vybereme větší měřítko, aby fotka vyplnila celou plochu (a přečuhující části se ořízly)
    val scale = max(scaleX, scaleY)

    val scaledWidth = scale * sourceWidth
    val scaledHeight = scale * sourceHeight

    // Vypočítáme posun, aby byla fotka vycentrovaná
    val left = (screenWidth - scaledWidth) / 2
    val top = (screenHeight - scaledHeight) / 2

    val targetRect = RectF(left, top, left + scaledWidth, top + scaledHeight)

    // Nakreslíme zdrojovou fotku na naše nové plátno (s ořezem)
    // Použijeme filtr pro kvalitnější zmenšení/zvětšení
    val paintBitmap = Paint().apply { isFilterBitmap = true }
    canvas.drawBitmap(sourceBitmap, null, targetRect, paintBitmap)

    // Zdrojovou bitmapu už nepotřebujeme, uvolníme paměť (důležité u velkých fotek!)
    sourceBitmap.recycle()

    // ==========================================
    // TEĎ KRESLÍME TEČKY (Už na správné rozměry)
    // ==========================================

    val paintPassed = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = false // Pixel-art styl
    }
    val paintFuture = Paint().apply {
        color = Color.parseColor("#40FFFFFF")
        style = Paint.Style.FILL
        isAntiAlias = false
    }

    val weeksLived = ChronoUnit.WEEKS.between(birthDate, LocalDate.now()).toInt()
    val totalYears = 76
    val columns = 52

    // Pozice (20% z výšky DISPLEJE, ne fotky)
    val marginTop = screenHeight * 0.40f
    val marginLeft = screenWidth * 0.10f

    val gridWidth = screenWidth - (2 * marginLeft)
    val gridHeight = screenHeight * 0.40f

    val stepX = gridWidth / columns
    val stepY = gridHeight / totalYears
    val dotSize = stepX * 0.40f

    var currentWeekCounter = 0
    for (row in 0 until totalYears) {
        for (col in 0 until columns) {
            val x = marginLeft + (col * stepX)
            val y = marginTop + (row * stepY)

            val paint = if (currentWeekCounter < weeksLived) paintPassed else paintFuture
            canvas.drawRect(x, y, x + dotSize, y + dotSize, paint)
            currentWeekCounter++
        }
    }

    // 5. NASTAVENÍ TAPETY
    val wm = WallpaperManager.getInstance(context)
    try {
        wm.setBitmap(finalBitmap, null, true, WallpaperManager.FLAG_LOCK)
    } catch (e: Exception) {
        wm.setBitmap(finalBitmap)
    }

    // finalBitmap.recycle() // Necháme na GC, nebo recyklujeme, pokud už ji nepotřebujeme
}