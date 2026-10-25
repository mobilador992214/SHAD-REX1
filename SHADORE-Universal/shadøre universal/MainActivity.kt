package com.shadore.universal

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.hardware.display.DisplayManager
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var details: TextView
    private lateinit var connectButton: Button
    private lateinit var optimizeButton: Button
    private lateinit var restoreButton: Button

    private val requestCode = 7401

    private var previousPeak: String? = null
    private var previousMin: String? = null

    private val permissionListener =
        Shizuku.OnRequestPermissionResultListener { code, result ->
            if (code == requestCode) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    setStatus(
                        "SHIZUKU TERHUBUNG",
                        "Izin diberikan. Fitur yang didukung siap digunakan."
                    )
                    setButtons(true)
                } else {
                    setStatus(
                        "IZIN DITOLAK",
                        "Izinkan SHADØRE melalui aplikasi Shizuku."
                    )
                    setButtons(false)
                }
            }
        }

    private val binderReceivedListener =
        Shizuku.OnBinderReceivedListener {
            runOnUiThread { refreshShizukuState() }
        }

    private val binderDeadListener =
        Shizuku.OnBinderDeadListener {
            runOnUiThread {
                setStatus(
                    "SHIZUKU TERPUTUS",
                    "Mulai kembali layanan Shizuku."
                )
                setButtons(false)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(9, 10, 16)
        window.navigationBarColor = Color.rgb(9, 10, 16)

        Shizuku.addRequestPermissionResultListener(
            permissionListener
        )
        Shizuku.addBinderReceivedListener(
            binderReceivedListener
        )
        Shizuku.addBinderDeadListener(
            binderDeadListener
        )

        buildUi()
        refreshShizukuState()
    }

    private fun buildUi() {
        val bg = Color.rgb(9, 10, 16)
        val panel = Color.rgb(22, 24, 35)
        val purple = Color.rgb(174, 130, 255)
        val white = Color.rgb(245, 245, 250)
        val muted = Color.rgb(170, 172, 190)

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(bg)

        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(28), dp(22), dp(28))
        }

        scroll.addView(column)

        val logo = TextView(this).apply {
            text = "SHADØRE"
            textSize = 32f
            setTextColor(white)
            typeface = Typeface.create(
                "sans-serif-black",
                Typeface.BOLD
            )
            gravity = Gravity.CENTER
        }

        column.addView(logo, matchWrap())

        val subtitle = TextView(this).apply {
            text = "UNIVERSAL PERFORMANCE"
            textSize = 12f
            letterSpacing = 0.18f
            setTextColor(purple)
            gravity = Gravity.CENTER
        }

        column.addView(subtitle, matchWrap())
        addSpace(column, 24)

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setBackgroundColor(panel)
        }

        column.addView(card, matchWrap())

        val statusTitle = TextView(this).apply {
            text = "SYSTEM STATUS"
            textSize = 12f
            setTextColor(purple)
            typeface = Typeface.DEFAULT_BOLD
        }

        card.addView(statusTitle, matchWrap())

        status = TextView(this).apply {
            text = "Memeriksa layanan..."
            textSize = 18f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
        }

        card.addView(status, matchWrap())

        details = TextView(this).apply {
            text = "Hubungkan Shizuku untuk menggunakan fitur sistem."
            textSize = 13f
            setTextColor(muted)
        }

        card.addView(details, matchWrap())
        addSpace(column, 18)

        connectButton = makeButton(
            "CONNECT SHIZUKU", purple, bg
        )
        column.addView(connectButton, matchWrap())
        connectButton.setOnClickListener {
            connectShizuku()
        }

        addSpace(column, 10)

        optimizeButton = makeButton(
            "ACTIVATE PERFORMANCE",
            Color.rgb(107, 72, 190),
            white
        )
        column.addView(optimizeButton, matchWrap())
        optimizeButton.setOnClickListener {
            optimizeDisplay()
        }

        addSpace(column, 10)

        restoreButton = makeButton(
            "RESTORE DEFAULTS",
            Color.rgb(42, 44, 57),
            white
        )
        column.addView(restoreButton, matchWrap())
        restoreButton.setOnClickListener {
            restoreDisplay()
        }

        addSpace(column, 20)

        val note = TextView(this).apply {
            text = """
                CATATAN KEAMANAN

                SHADØRE meminta Android menggunakan refresh
                rate yang didukung layar perangkat.

                Hasil bergantung pada merek HP, firmware,
                suhu, kemampuan GPU/CPU, dan batas FPS game.

                Aplikasi ini tidak melakukan overclock,
                tidak mengubah file game, dan tidak menjamin
                kenaikan FPS. Refresh rate bukan FPS game.
            """.trimIndent()

            textSize = 12f
            setTextColor(muted)
            setLineSpacing(dp(3).toFloat(), 1f)
        }

        column.addView(note, matchWrap())
        setContentView(scroll)
    }

    private fun connectShizuku() {
        if (!Shizuku.pingBinder()) {
            setStatus(
                "SHIZUKU TIDAK AKTIF",
                "Mulai layanan Shizuku, lalu kembali ke SHADØRE."
            )
            return
        }

        if (
            Shizuku.checkSelfPermission() ==
            PackageManager.PERMISSION_GRANTED
        ) {
            refreshShizukuState()
        } else {
            Shizuku.requestPermission(requestCode)
        }
    }

    private fun refreshShizukuState() {
        if (!::status.isInitialized) return

        if (!Shizuku.pingBinder()) {
            setStatus(
                "SHIZUKU TIDAK AKTIF",
                "Mulai layanan Shizuku terlebih dahulu."
            )
            setButtons(false)
            connectButton.isEnabled = true
            return
        }

        if (
            Shizuku.checkSelfPermission() ==
            PackageManager.PERMISSION_GRANTED
        ) {
            setStatus(
                "SHIZUKU TERHUBUNG",
                "Izin tersedia. Mode Performance siap dicoba."
            )
            setButtons(true)
        } else {
            setStatus(
                "MENUNGGU IZIN",
                "Tekan CONNECT SHIZUKU dan berikan izin."
            )
            setButtons(false)
            connectButton.isEnabled = true
        }
    }

    private fun hasShizukuPermission(): Boolean {
        if (!Shizuku.pingBinder()) {
            setStatus(
                "SHIZUKU TIDAK AKTIF",
                "Mulai layanan Shizuku terlebih dahulu."
            )
            return false
        }

        if (
            Shizuku.checkSelfPermission() !=
            PackageManager.PERMISSION_GRANTED
        ) {
            connectShizuku()
            return false
        }

        return true
    }

    private fun optimizeDisplay() {
        if (!hasShizukuPermission()) return

        optimizeButton.isEnabled = false

        Thread {
            try {
                if (previousPeak == null) {
                    previousPeak = readSetting(
                        "peak_refresh_rate"
                    )
                }

                if (previousMin == null) {
                    previousMin = readSetting(
                        "min_refresh_rate"
                    )
                }

                val maxHz = getSupportedMaxRefreshRate()

                if (maxHz <= 0f) {
                    runOnUiThread {
                        setStatus(
                            "MODE LAYAR TIDAK TERBACA",
                            "Tidak ada perubahan yang diterapkan."
                        )
                        optimizeButton.isEnabled = true
                    }
                    return@Thread
                }

                val value = formatHz(maxHz)

                shell(
                    "settings put system peak_refresh_rate $value"
                )
                shell(
                    "settings put system min_refresh_rate $value"
                )

                runOnUiThread {
                    setStatus(
                        "PERMINTAAN SELESAI",
                        "Refresh rate maksimum yang terdeteksi: " +
                            "$value Hz. Firmware dapat mengabaikan " +
                            "atau membatasi pengaturan ini."
                    )
                    optimizeButton.isEnabled = true
                }
            } catch (e: Exception) {
                runOnUiThread {
                    setStatus(
                        "PENGATURAN GAGAL",
                        e.message ?: "Android menolak perintah."
                    )
                    optimizeButton.isEnabled = true
                }
            }
        }.start()
    }

    private fun restoreDisplay() {
        if (!hasShizukuPermission()) return

        restoreButton.isEnabled = false

        Thread {
            try {
                restoreSetting(
                    "peak_refresh_rate",
                    previousPeak
                )
                restoreSetting(
                    "min_refresh_rate",
                    previousMin
                )

                runOnUiThread {
                    setStatus(
                        "PEMULIHAN DIMINTA",
                        "Nilai sebelumnya dipulihkan jika tersedia. " +
                            "Periksa pengaturan layar HP."
                    )
                    restoreButton.isEnabled = true
                }
            } catch (e: Exception) {
                runOnUiThread {
                    setStatus(
                        "PEMULIHAN GAGAL",
                        e.message ?: "Perintah ditolak oleh sistem."
                    )
                    restoreButton.isEnabled = true
                }
            }
        }.start()
    }

    private fun readSetting(key: String): String? {
        val value = shell("settings get system $key").trim()

        if (value.isBlank() || value == "null") {
            return null
        }

        // Only accept numeric values before using them later.
        val number = value.toFloatOrNull()
            ?: throw IllegalStateException(
                "Nilai pengaturan bukan angka."
            )

        if (!number.isFinite() || number <= 0f) {
            throw IllegalStateException(
                "Nilai pengaturan tidak valid."
            )
        }

        return formatHz(number)
    }

    private fun restoreSetting(
        key: String,
        previous: String?
    ) {
        if (previous == null) {
            shell("settings delete system $key")
        } else {
            val value = previous.toFloatOrNull()
                ?: throw IllegalStateException(
                    "Nilai pemulihan tidak valid."
                )

            if (!value.isFinite() || value <= 0f) {
                throw IllegalStateException(
                    "Nilai pemulihan tidak valid."
                )
            }

            shell(
                "settings put system $key ${formatHz(value)}"
            )
        }
    }

    private fun getSupportedMaxRefreshRate(): Float {
        val manager =
            getSystemService(DISPLAY_SERVICE) as DisplayManager

        var maxHz = 0f

        for (display in manager.displays) {
            for (mode in display.supportedModes) {
                if (mode.refreshRate > maxHz) {
                    maxHz = mode.refreshRate
                }
            }
        }

        return maxHz
    }

    private fun formatHz(value: Float): String {
        return String.format(
            Locale.US, "%.2f", value
        ).trimEnd('0').trimEnd('.')
    }

    private fun shell(command: String): String {
        val process = Shizuku.newProcess(
            arrayOf("sh", "-c", command),
            null,
            null
        )

        val output = BufferedReader(
            InputStreamReader(process.inputStream)
        ).use { it.readText() }

        val error = BufferedReader(
            InputStreamReader(process.errorStream)
        ).use { it.readText() }

        val exitCode = process.waitFor()

        if (exitCode != 0) {
            throw IllegalStateException(
                error.ifBlank {
                    "Perintah ditolak, kode $exitCode."
                }
            )
        }

        return output
    }

    private fun setStatus(
        title: String,
        message: String
    ) {
        status.text = title
        details.text = message
    }

    private fun setButtons(enabled: Boolean) {
        if (!::optimizeButton.isInitialized) return
        if (!::restoreButton.isInitialized) return

        optimizeButton.isEnabled = enabled
        restoreButton.isEnabled = enabled

        optimizeButton.alpha =
            if (enabled) 1f else 0.5f

        restoreButton.alpha =
            if (enabled) 1f else 0.5f
    }

    private fun makeButton(
        label: String,
        background: Int,
        foreground: Int
    ): Button {
        return Button(this).apply {
            text = label
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(foreground)
            setBackgroundColor(background)
            isAllCaps = false
            minHeight = dp(52)
        }
    }

    private fun addSpace(
        parent: LinearLayout,
        heightDp: Int
    ) {
        parent.addView(
            View(this),
            LinearLayout.LayoutParams(1, dp(heightDp))
        )
    }

    private fun matchWrap() =
        LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

    private fun dp(value: Int): Int {
        return (
            value * resources.displayMetrics.density
        ).toInt()
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(
            permissionListener
        )
        Shizuku.removeBinderReceivedListener(
            binderReceivedListener
        )
        Shizuku.removeBinderDeadListener(
            binderDeadListener
        )
        super.onDestroy()
    }
}
