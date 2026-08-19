package br.com.hospitalidadeabordo.driver

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.LinkedHashSet
import java.util.Locale

class UberScreenAccessibilityService : AccessibilityService() {
    private val prefs by lazy { getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE) }
    private val store by lazy { OperationalEventStore(this) }
    private val notificationManager by lazy { getSystemService(NotificationManager::class.java) }
    private var lastSignature: Int? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs.edit().putBoolean(MainActivity.KEY_READER_CONNECTED, true).apply()
        createNotificationChannel()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return
        if (packageName != UBER_DRIVER_PACKAGE) return

        val root = rootInActiveWindow ?: return
        val texts = collectVisibleStrings(root)
        if (texts.isEmpty()) return

        val signature = texts.joinToString("|").hashCode()
        if (signature == lastSignature) return
        lastSignature = signature

        val joined = texts.joinToString("\n")
        val screenState = inferScreenState(joined)
        store.recordScreenEvent(packageName, screenState)

        val costPerKm = prefs.getString(MainActivity.KEY_COST_PER_KM, "0")
            .orEmpty()
            .replace(",", ".")
            .toDoubleOrNull()
            ?.coerceAtLeast(0.0)
            ?: 0.0

        val offer = OfferParser.parse(texts, costPerKm)
        if (offer != null) {
            val isNew = store.recordOffer(offer)
            if (isNew) {
                publishOfferNotification(offer)
                sendBroadcast(
                    Intent(ACTION_METRICS_UPDATED)
                        .setPackage(packageNameForBroadcast())
                )
            }
        } else if (looksLikeOffer(joined)) {
            store.recordParseMiss()
        }
    }

    override fun onInterrupt() {
        // O Android pode interromper temporariamente o serviço; não executamos ações no app observado.
    }

    override fun onDestroy() {
        prefs.edit().putBoolean(MainActivity.KEY_READER_CONNECTED, false).apply()
        super.onDestroy()
    }

    private fun collectVisibleStrings(root: AccessibilityNodeInfo): List<String> {
        val result = LinkedHashSet<String>()
        var visited = 0

        fun visit(node: AccessibilityNodeInfo?, depth: Int) {
            if (node == null || depth > MAX_DEPTH || visited >= MAX_NODES) return
            visited += 1

            node.text?.toString()?.normalizeUiText()?.takeIf { it.isNotBlank() }?.let(result::add)
            node.contentDescription?.toString()?.normalizeUiText()?.takeIf { it.isNotBlank() }?.let(result::add)

            for (index in 0 until node.childCount) {
                visit(node.getChild(index), depth + 1)
                if (visited >= MAX_NODES) break
            }
        }

        visit(root, 0)
        return result.take(MAX_TEXTS)
    }

    private fun inferScreenState(text: String): String {
        val normalized = text.lowercase(Locale("pt", "BR"))
        return when {
            (normalized.contains("aceitar") || normalized.contains("selecionar")) && normalized.contains("r$") -> "oferta"
            normalized.contains("iniciar viagem") || normalized.contains("confirmar embarque") -> "embarque"
            normalized.contains("finalizar viagem") || normalized.contains("encerrar viagem") -> "em_viagem"
            normalized.contains("ganhos") && normalized.contains("r$") -> "resultado"
            normalized.contains("ficar online") || normalized.contains("você está online") -> "disponivel"
            else -> "uber_ativo"
        }
    }

    private fun looksLikeOffer(text: String): Boolean =
        text.contains("R$", ignoreCase = true) &&
            (text.contains("Aceitar", ignoreCase = true) || text.contains("Selecionar", ignoreCase = true))

    private fun publishOfferNotification(offer: OfferParser.OfferSnapshot) {
        if (!prefs.getBoolean(MainActivity.KEY_OFFER_NOTIFICATION, true)) return
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return
        }

        notificationManager.notify(
            OFFER_NOTIFICATION_ID,
            Notification.Builder(this, CHANNEL_OFFER_READER)
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentTitle("Oferta lida • ${offer.confidence}%")
                .setContentText(offer.compactSummary())
                .setStyle(Notification.BigTextStyle().bigText(offer.detailedSummary()))
                .setOnlyAlertOnce(false)
                .setAutoCancel(true)
                .build()
        )
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_OFFER_READER,
            "Leitura de ofertas",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Confirma quando uma oferta do Uber Driver foi interpretada pelo Hospitalidade a Bordo"
            enableVibration(false)
            setSound(null, null)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun String.normalizeUiText(): String = replace(Regex("\\s+"), " ").trim()

    private fun packageNameForBroadcast(): String = applicationContext.packageName

    companion object {
        const val UBER_DRIVER_PACKAGE = "com.ubercab.driver"
        const val ACTION_METRICS_UPDATED = "br.com.hospitalidadeabordo.driver.METRICS_UPDATED"
        private const val CHANNEL_OFFER_READER = "offer_reader"
        private const val OFFER_NOTIFICATION_ID = 240
        private const val MAX_DEPTH = 22
        private const val MAX_NODES = 360
        private const val MAX_TEXTS = 220
    }
}
