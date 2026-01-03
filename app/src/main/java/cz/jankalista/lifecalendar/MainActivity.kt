package cz.jankalista.lifecalendar

import cz.jankalista.lifecalendar.R
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    // Image Picker Registration
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("custom_image_uri", uri.toString())
                .apply()

            updateUIState()
            Toast.makeText(this, "Custom background selected.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI Components
        val datePicker = findViewById<DatePicker>(R.id.datePicker)
        val btnApply = findViewById<Button>(R.id.btnApply)
        val btnPickImage = findViewById<Button>(R.id.btnPickImage)
        val btnResetImage = findViewById<Button>(R.id.btnResetImage)
        val tvStats = findViewById<TextView>(R.id.tvStats)

        // Load Preferences
        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val savedDate = prefs.getString("birth_date", null)

        // Initialize UI
        updateUIState()

        if (savedDate != null) {
            val date = LocalDate.parse(savedDate)
            datePicker.updateDate(date.year, date.monthValue - 1, date.dayOfMonth)
            updateStatsUI(date, tvStats)
        }

        // --- LISTENERS ---

        // 1. Pick Image
        btnPickImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        // 2. Reset Image (Remove custom wallpaper)
        btnResetImage.setOnClickListener {
            prefs.edit().remove("custom_image_uri").apply()
            updateUIState()
            Toast.makeText(this, "Reset to default wallpaper.", Toast.LENGTH_SHORT).show()
        }

        // 3. Generate Wallpaper
        btnApply.setOnClickListener {
            val birthDate = LocalDate.of(datePicker.year, datePicker.month + 1, datePicker.dayOfMonth)
            prefs.edit().putString("birth_date", birthDate.toString()).apply()

            val customImageUri = prefs.getString("custom_image_uri", null)

            updateStatsUI(birthDate, tvStats)
            scheduleWallpaperUpdate()

            // Disable button during processing
            btnApply.isEnabled = false
            btnApply.text = "PROCESSING..."

            Thread {
                try {
                    updateWallpaper(this, birthDate, customImageUri)

                    runOnUiThread {
                        Toast.makeText(this, "DONE! Wallpaper updated.", Toast.LENGTH_LONG).show()
                        btnApply.isEnabled = true
                        btnApply.text = "INITIALIZE WALLPAPER"
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        btnApply.isEnabled = true
                        btnApply.text = "INITIALIZE WALLPAPER"
                    }
                }
            }.start()
        }
    }

    private fun updateUIState() {
        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val uri = prefs.getString("custom_image_uri", null)
        val tvStatus = findViewById<TextView>(R.id.tvImageStatus)
        val btnReset = findViewById<Button>(R.id.btnResetImage)

        if (uri != null) {
            tvStatus.text = "> Using CUSTOM background image."
            tvStatus.setTextColor(getColor(android.R.color.holo_green_light))
            btnReset.visibility = View.VISIBLE
        } else {
            tvStatus.text = "> Using DEFAULT Nothing OS wallpaper."
            tvStatus.setTextColor(getColor(R.color.white))
            btnReset.visibility = View.GONE
        }
    }

    private fun updateStatsUI(birthDate: LocalDate, tvStats: TextView) {
        val today = LocalDate.now()
        val daysLived = ChronoUnit.DAYS.between(birthDate, today)
        val weeksLived = ChronoUnit.WEEKS.between(birthDate, today)
        val currentAge = ChronoUnit.YEARS.between(birthDate, today).toDouble()

        // Stats based on Czech Male average (approx 76 years)
        val totalExpectedDays = 76 * 365.25
        val progressPercent = (daysLived / totalExpectedDays * 100).coerceAtMost(100.0)

        val survivalRate = when {
            currentAge < 45 -> 100.0 - (currentAge * 0.04)
            currentAge < 65 -> 98.2 - ((currentAge - 45) * 0.5)
            currentAge < 76 -> 88.0 - ((currentAge - 65) * 2.2)
            else -> 50.0 - ((currentAge - 76) * 6.0)
        }.coerceIn(0.0, 100.0)

        tvStats.text = """
            DAYS LIVED: $daysLived
            WEEKS: $weeksLived
            LIFE PROGRESS: ${String.format("%.2f", progressPercent)} %
            EST. SURVIVAL RATE: ${String.format("%.1f", survivalRate)} %
        """.trimIndent()
    }

    private fun scheduleWallpaperUpdate() {
        val workRequest = PeriodicWorkRequestBuilder<WallpaperWorker>(7, TimeUnit.DAYS)
            .addTag("wallpaper_update")
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "wallpaper_periodic", ExistingPeriodicWorkPolicy.UPDATE, workRequest
        )
    }
}