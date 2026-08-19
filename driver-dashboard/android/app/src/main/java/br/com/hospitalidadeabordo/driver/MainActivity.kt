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
import android.text.InputType
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences(PREFS, MODE_PRIVATE) }
    private lateinit var status: TextView
    private lateinit var readerStatus: TextView
    private lateinit var offerStatus: TextView
    private lateinit var metricsStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildUi())
        requestRuntimePermissions()
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
        refreshReaderDiagnostics()
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
            text = "Painel do motorista • inteligência operacional v0.2"
            textSize = 15f
            setTextColor(Color.DKGRAY)
            setPadding(0, dp(4), 0, dp(18))
        })

        root.addView(sectionTitle("Leitor do Uber Driver"))
        readerStatus = infoCard()
        root.addView(readerStatus, fullWidth())

        root.addView(actionButton("Ativar / gerenciar leitura do Uber Driver") {
            runCatching { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        })

        root.addView(settingSwitch("Notificar quando uma oferta for interpretada", KEY_OFFER_NOTIFICATION, true))

        root.addView(TextView(this).apply {
            text = "Custo estimado por km (opcional)"
            textSize = 15f
            setTextColor(Color.DKGRAY)
            setPadding(0, dp(12), 0, dp(4))
        })
        val costInput = EditText(this).apply {
            hint = "Ex.: 1,55"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(prefs.getString(KEY_COST_PER_KM, ""))
            textSize = 16f
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setBackgroundColor(Color.WHITE)
        }
        root.addView(costInput, fullWidth())
        root.addView(actionButton("Salvar custo por km") {
            val normalized = costInput.text.toString().trim().replace(",", ".")
            val value = normalized.toDoubleOrNull()
            if (value != null && value >= 0.0) {
                prefs.edit().putString(KEY_COST_PER_KM, value.toString()).apply()
                costInput.setText(value.toString().replace(".", ","))
            }
            refreshReaderDiagnostics()
        })

        root.addView(sectionTitle("Última oferta interpretada"))
        offerStatus = infoCard()
        root.addView(offerStatus, fullWidth())

        root.addView(sectionTitle("Diagnóstico do leitor"))
        metricsStatus = infoCard()
        root.addView(metricsStatus, fullWidth())
        root.addView(actionButton("Limpar diagnóstico local") {
            OperationalEventStore(this).clearDiagnostics()
            refreshReaderDiagnostics()
        })

        root.addView(TextView(this).apply {
            text = "Nesta versão, o leitor observa somente o pacote do Uber Driver e extrai informações exibidas na interface. Ele não toca em botões, não aceita e não recusa corridas."
            textSize = 13f
            setTextColor(Color.GRAY)
            setPadding(0, dp(14), 0, dp(6))
        })

        root.addView(sectionTitle("Sessão de viagem"))
        status = infoCard()
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
            text = "Durante a condução, o app deve funcionar sem exigir leitura ou toque. Faça ajustes e diagnóstico somente com o veículo parado."
            textSize = 13f
            setTextColor(Color.GRAY)
            setPadding(0, dp(24), 0, 0)
        })

        scroll.addView(root)
        return scroll
    }

    private fun infoCard(): TextView = TextView(this).apply {
        textSize = 16f
        setTextColor(Color.rgb(23, 41, 56))
        setPadding(dp(16), dp(16), dp(16), dp(16))
        setBackgroundColor(Color.WHITE)
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

    private fun refreshReaderDiagnostics() {
        if (!::readerStatus.isInitialized) return

        val enabled = isReaderEnabled()
        val connected = prefs.getBoolean(KEY_READER_CONNECTED, false)
        readerStatus.text = when {
            enabled && connected -> "✅ Leitor ativo e conectado\nObservando somente: com.ubercab.driver"
            enabled -> "🟡 Leitor habilitado; aguardando conexão do Android"
            else -> "⚪ Leitor desativado\nToque no botão abaixo e habilite ‘Leitor operacional do Uber Driver’."
        }

        val detail = prefs.getString(KEY_LAST_OFFER_DETAIL, null)
        val lastAt = prefs.getLong(KEY_LAST_OFFER_AT, 0L)
        offerStatus.text = if (detail.isNullOrBlank()) {
            "Nenhuma oferta interpretada ainda."
        } else {
            "$detail\nLida em: ${formatTime(lastAt)}"
        }

        val screenEvents = prefs.getInt(KEY_SCREEN_EVENT_COUNT, 0)
        val offers = prefs.getInt(KEY_OFFER_COUNT, 0)
        val misses = prefs.getInt(KEY_PARSE_FAILURES, 0)
        val state = prefs.getString(KEY_LAST_SCREEN_STATE, "—")
        val lastEventAt = prefs.getLong(KEY_LAST_SCREEN_EVENT_AT, 0L)
        val cost = prefs.getString(KEY_COST_PER_KM, "0")
        metricsStatus.text = buildString {
            append("Eventos relevantes: $screenEvents\n")
            append("Ofertas únicas: $offers\n")
            append("Telas parecidas com oferta não interpretadas: $misses\n")
            append("Estado reconhecido: $state\n")
            append("Último evento: ${if (lastEventAt > 0) formatTime(lastEventAt) else "—"}\n")
            append("Custo/km configurado: R$ ${cost?.replace('.', ',') ?: "0"}")
        }
    }

    private fun isReaderEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ).orEmpty()
        val fullName = "$packageName/${UberScreenAccessibilityService::class.java.name}"
        val shortName = "$packageName/.UberScreenAccessibilityService"
        return enabledServices.split(':').any {
            it.equals(fullName, ignoreCase = true) || it.equals(shortName, ignoreCase = true)
        }
    }

    private fun formatTime(timestamp: Long): String =
        SimpleDateFormat("dd/MM HH:mm:ss", Locale("pt", "BR")).format(Date(timestamp))

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

        const val KEY_READER_CONNECTED = "reader_connected"
        const val KEY_OFFER_NOTIFICATION = "offer_notification"
        const val KEY_COST_PER_KM = "cost_per_km"
        const val KEY_SCREEN_EVENT_COUNT = "screen_event_count"
        const val KEY_OFFER_COUNT = "offer_count"
        const val KEY_PARSE_FAILURES = "parse_failures"
        const val KEY_LAST_PACKAGE = "last_package"
        const val KEY_LAST_SCREEN_STATE = "last_screen_state"
        const val KEY_LAST_SCREEN_EVENT_AT = "last_screen_event_at"
        const val KEY_LAST_OFFER_HASH = "last_offer_hash"
        const val KEY_LAST_OFFER_AT = "last_offer_at"
        const val KEY_LAST_OFFER_SUMMARY = "last_offer_summary"
        const val KEY_LAST_OFFER_DETAIL = "last_offer_detail"
        const val KEY_LAST_OFFER_CONFIDENCE = "last_offer_confidence"
    }
}
