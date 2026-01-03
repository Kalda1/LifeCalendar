package cz.jankalista.lifecalendar

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.time.LocalDate

class WallpaperWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    override fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val savedDateStr = prefs.getString("birth_date", null) ?: return Result.failure()

        // Načteme i uložené URI vlastního obrázku
        val customImageUri = prefs.getString("custom_image_uri", null)

        return try {
            val birthDate = LocalDate.parse(savedDateStr)
            // Předáme datum I URI obrázku do kreslící funkce
            updateWallpaper(applicationContext, birthDate, customImageUri)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}