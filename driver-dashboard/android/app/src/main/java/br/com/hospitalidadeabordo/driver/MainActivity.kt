package br.com.hospitalidadeabordo.driver

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences(PREFS, MODE_PRIVATE) }
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildUi())
        requestRuntimePermissions()
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun buildUi(): ScrollView {
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(28), dp(24), dp(40))
            setBackgroundColor(Color.rgb(246, 242, 232))
        }

        root.addView(TextView(this).apply {
            text = "Hospitalidade a Bordo"
            textSize = 27f
            setTextColor(Color.rgb(23, 41, 56))
        })
        root.addView(TextView(this).apply {
            text = "Painel do motorista • protótipo Android"
            textSize = 15f
            setTextColor(Color.DKGRAY)
            setPadding(0, dp(4), 0, dp(18))
        })

        status = TextView(this).apply {
            textSize = 18f
            setTextColor(Color.rgb(23, 41, 56))
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setBackgroundColor(Color.WHITE)
        }
        root.addView(status, fullWidth())

        root.addView(actionButton("Iniciar viagem / monitoramento") {
            if (!hasLocationPermission()) {
                requestRuntimePermissions()
                return@actionButton
            }
            val intent = Intent(this, DriverSessionService::class.java)
                .setAction(DriverSessionService.ACTION_START)
            startForegroundService(intent)
            prefs.edit().putBoolean(KEY_RIDE_ACTIVE, true).apply()
            refreshStatus()
        })

        root.addView(actionButton("Simular pedido do passageiro") {
            val intent = Intent(this, DriverSessionService::class.java)
                .setAction(DriverSessionService.ACTION_SIMULATE_REQUEST)
            startService(intent)
        })

        root.addView(actionButton("Encerrar viagem") {
            val intent = Intent(this, DriverSessionService::class.java)
                .setAction(DriverSessionService.ACTION_STOP)
            startService(intent)
            prefs.edit().putBoolean(KEY_RIDE_ACTIVE, false).apply()
            refreshStatus()
        })

        root.addView(actionButton("Permitir botão flutuante sobre outros apps") {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            }
        })

        root.addView(sectionTitle("Como quero ser avisado"))
        root.addView(settingSwitch("Voz para pedidos manuais", KEY_VOICE, true))
        root.addView(settingSwitch("Heads-up para novos pedidos", KEY_HEADS_UP, true))
        root.addView(settingSwitch("Vibração", KEY_VIBRATION, true))
        root.addView(settingSwitch("Botão flutuante", KEY_OVERLAY, true))
        root.addView(settingSwitch("Música automática", KEY_MUSIC_AUTOMATION, true))
        root.addView(settingSwitch("Urgentes sempre alertam", KEY_URGENT_OVERRIDE, true))

        root.addView(sectionTitle("Última posição do motorista"))
        root.addView(TextView(this).apply {
            val lat = prefs.getString(KEY_LAST_LAT, "—")
            val lng = prefs.getString(KEY_LAST_LNG, "—")
            val acc = prefs.getString(KEY_LAST_ACCURACY, "—")
            val address = prefs.getString(KEY_LAST_ADDRESS, "Aguardando captura")
            text = "$lat, $lng • precisão $acc m\n$address"
            textSize = 15f
            setTextColor(Color.DKGRAY)
        })

        root.addView(TextView(this).apply {
            text = "Durante a condução, o app deve funcionar sem exigir leitura ou toque. Use os controles somente quando estiver parado."
            textSize = 13f
            setTextColor(Color.GRAY)
            setPadding(0, dp(24), 0, 0)
        })

        scroll.addView(root)
        return scroll
    }

    private fun settingSwitch(label: String, key: String, default: Boolean): Switch = Switch(this).apply {
        text = label
        textSize = 16f
        isChecked = prefs.getBoolean(key, default)
        setPadding(0, dp(8), 0, dp(8))
        setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean(key, checked).apply() }
    }

    private fun actionButton(label: String, action: () -> Unit): Button = Button(this).apply {
        text = label
        isAllCaps = false
        textSize = 16f
        setOnClickListener { action() }
        layoutParams = fullWidth(top = 12)
    }

    private fun sectionTitle(textValue: String): TextView = TextView(this).apply {
        text = textValue
        textSize = 19f
        setTextColor(Color.rgb(23, 41, 56))
        setPadding(0, dp(26), 0, dp(8))
    }

    private fun refreshStatus() {
        if (!::status.isInitialized) return
        val active = prefs.getBoolean(KEY_RIDE_ACTIVE, false)
        val pending = prefs.getString(KEY_PENDING_REQUEST, null)
        status.text = when {
            active && !pending.isNullOrBlank() -> "🚘 VIAGEM ATIVA\nSolicitação: $pending"
            active -> "🚘 VIAGEM ATIVA\nAguardando solicitações"
            else -> "VIAGEM ENCERRADA\nPronto para iniciar"
        }
    }

    private fun requestRuntimePermissions() {
        val missing = mutableListOf<String>()
        if (!hasLocationPermission()) missing += Manifest.permission.ACCESS_FINE_LOCATION
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            missing += Manifest.permission.POST_NOTIFICATIONS
        }
        if (missing.isNotEmpty()) requestPermissions(missing.toTypedArray(), 1001)
    }

    private fun hasLocationPermission(): Boolean =
        checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun fullWidth(top: Int = 0) = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { topMargin = dp(top) }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        const val PREFS = "driver_settings"
        const val KEY_RIDE_ACTIVE = "ride_active"
        const val KEY_VOICE = "voice"
        const val KEY_HEADS_UP = "heads_up"
        const val KEY_VIBRATION = "vibration"
        const val KEY_OVERLAY = "overlay"
        const val KEY_MUSIC_AUTOMATION = "music_automation"
        const val KEY_URGENT_OVERRIDE = "urgent_override"
        const val KEY_PENDING_REQUEST = "pending_request"
        const val KEY_LAST_LAT = "last_lat"
        const val KEY_LAST_LNG = "last_lng"
        const val KEY_LAST_ACCURACY = "last_accuracy"
        const val KEY_LAST_ADDRESS = "last_address"
    }
}
