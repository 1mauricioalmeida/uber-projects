package br.com.hospitalidadeabordo.driver

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

class DriverSessionService : Service(), LocationListener, TextToSpeech.OnInitListener {
    private lateinit var prefs: android.content.SharedPreferences
    private lateinit var notificationManager: NotificationManager
    private lateinit var locationManager: LocationManager
    private lateinit var music: MusicAutomationController
    private var tts: TextToSpeech? = null
    private var lastLocation: Location? = null
    private var bubbleView: TextView? = null
    private var bubbleParams: WindowManager.LayoutParams? = null
    private var quickPanel: LinearLayout? = null
    private var windowManager: WindowManager? = null
    private var lastGeocodeAt = 0L

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE)
        notificationManager = getSystemService(NotificationManager::class.java)
        locationManager = getSystemService(LocationManager::class.java)
        windowManager = getSystemService(WindowManager::class.java)
        music = MusicAutomationController(this)
        tts = TextToSpeech(this, this)
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startRideSession()
            ACTION_SIMULATE_REQUEST -> simulatePassengerRequest()
            ACTION_STOP -> stopRideSession()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopLocationUpdates()
        removeOverlay()
        tts?.shutdown()
        super.onDestroy()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("pt", "BR")
            tts?.setSpeechRate(0.95f)
        }
    }

    private fun startRideSession() {
        prefs.edit()
            .putBoolean(MainActivity.KEY_RIDE_ACTIVE, true)
            .putLong(KEY_STARTED_AT, System.currentTimeMillis())
            .remove(MainActivity.KEY_PENDING_REQUEST)
            .remove(KEY_START_LAT)
            .remove(KEY_START_LNG)
            .remove(KEY_START_ACCURACY)
            .remove(KEY_START_ADDRESS)
            .remove(KEY_END_LAT)
            .remove(KEY_END_LNG)
            .remove(KEY_END_ACCURACY)
            .remove(KEY_END_ADDRESS)
            .apply()

        startForeground(NOTIFICATION_ACTIVE, buildPersistentNotification("Viagem ativa • aguardando solicitações"))
        startLocationUpdates()
        if (prefs.getBoolean(MainActivity.KEY_OVERLAY, true)) showOverlayIfAllowed()
    }

    private fun simulatePassengerRequest() {
        val summary = "Mais fresco • MPB tranquila • Prefere silêncio"
        prefs.edit().putString(MainActivity.KEY_PENDING_REQUEST, summary).apply()
        music.applyProfile("mpb")
        updatePersistentNotification(summary)
        updateQuickPanel(summary)

        if (prefs.getBoolean(MainActivity.KEY_VOICE, true)) {
            tts?.speak(
                "Passageiro solicita temperatura mais fresca. Prefere silêncio.",
                TextToSpeech.QUEUE_FLUSH,
                null,
                "passenger-request"
            )
        }

        if (prefs.getBoolean(MainActivity.KEY_HEADS_UP, true)) {
            postHeadsUp("Solicitação do passageiro", summary, urgent = false)
        }
    }

    private fun stopRideSession() {
        lastLocation?.let { saveEndLocation(it) }
        prefs.edit()
            .putBoolean(MainActivity.KEY_RIDE_ACTIVE, false)
            .putLong(KEY_ENDED_AT, System.currentTimeMillis())
            .remove(MainActivity.KEY_PENDING_REQUEST)
            .apply()

        stopLocationUpdates()
        removeOverlay()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startLocationUpdates() {
        val fine = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fine && !coarse) return

        try {
            if (fine && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3_000L, 3f, this)
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5_000L, 5f, this)
            }
        } catch (_: SecurityException) {
        }
    }

    private fun stopLocationUpdates() {
        try {
            locationManager.removeUpdates(this)
        } catch (_: SecurityException) {
        }
    }

    override fun onLocationChanged(location: Location) {
        val current = lastLocation
        if (current == null || location.accuracy <= current.accuracy || location.time > current.time + 4_000L) {
            lastLocation = location
        }

        prefs.edit()
            .putString(MainActivity.KEY_LAST_LAT, "%.6f".format(Locale.US, location.latitude))
            .putString(MainActivity.KEY_LAST_LNG, "%.6f".format(Locale.US, location.longitude))
            .putString(MainActivity.KEY_LAST_ACCURACY, "%.1f".format(Locale.US, location.accuracy))
            .apply()

        if (!prefs.contains(KEY_START_LAT)) saveStartLocation(location)

        val now = System.currentTimeMillis()
        if (now - lastGeocodeAt > 30_000L) {
            lastGeocodeAt = now
            reverseGeocode(location, isStart = !prefs.contains(KEY_START_ADDRESS), isEnd = false)
        }
    }

    private fun saveStartLocation(location: Location) {
        prefs.edit()
            .putString(KEY_START_LAT, location.latitude.toString())
            .putString(KEY_START_LNG, location.longitude.toString())
            .putString(KEY_START_ACCURACY, location.accuracy.toString())
            .putLong(KEY_START_LOCATION_AT, location.time)
            .apply()
        reverseGeocode(location, isStart = true, isEnd = false)
    }

    private fun saveEndLocation(location: Location) {
        prefs.edit()
            .putString(KEY_END_LAT, location.latitude.toString())
            .putString(KEY_END_LNG, location.longitude.toString())
            .putString(KEY_END_ACCURACY, location.accuracy.toString())
            .putLong(KEY_END_LOCATION_AT, location.time)
            .apply()
        reverseGeocode(location, isStart = false, isEnd = true)
    }

    @Suppress("DEPRECATION")
    private fun reverseGeocode(location: Location, isStart: Boolean, isEnd: Boolean) {
        Thread {
            val addressText = try {
                val geocoder = Geocoder(this, Locale("pt", "BR"))
                val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()
                address?.getAddressLine(0)
                    ?: listOfNotNull(address?.thoroughfare, address?.subThoroughfare, address?.subLocality, address?.locality, address?.adminArea)
                        .joinToString(", ")
                        .ifBlank { "Endereço não disponível" }
            } catch (_: Exception) {
                "Endereço não disponível"
            }

            prefs.edit().putString(MainActivity.KEY_LAST_ADDRESS, addressText).apply()
            if (isStart) prefs.edit().putString(KEY_START_ADDRESS, addressText).apply()
            if (isEnd) prefs.edit().putString(KEY_END_ADDRESS, addressText).apply()

            val pending = prefs.getString(MainActivity.KEY_PENDING_REQUEST, null)
            updatePersistentNotification(pending ?: "Viagem ativa • $addressText")
        }.start()
    }

    private fun createNotificationChannels() {
        val active = NotificationChannel(CHANNEL_ACTIVE, "Viagem ativa", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Notificação persistente durante a sessão de viagem"
            setShowBadge(false)
        }
        val request = NotificationChannel(CHANNEL_REQUEST, "Pedidos do passageiro", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Avisos curtos de novas solicitações"
            enableVibration(prefs.getBoolean(MainActivity.KEY_VIBRATION, true))
        }
        val urgent = NotificationChannel(CHANNEL_URGENT, "Pedidos urgentes", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Alertas prioritários como Preciso de ajuda"
            enableVibration(true)
        }
        notificationManager.createNotificationChannels(listOf(active, request, urgent))
    }

    private fun buildPersistentNotification(summary: String): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            10,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return Notification.Builder(this, CHANNEL_ACTIVE)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Hospitalidade a Bordo")
            .setContentText(summary)
            .setStyle(Notification.BigTextStyle().bigText(summary))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openIntent)
            .build()
    }

    private fun updatePersistentNotification(summary: String) {
        if (!prefs.getBoolean(MainActivity.KEY_RIDE_ACTIVE, false)) return
        notificationManager.notify(NOTIFICATION_ACTIVE, buildPersistentNotification(summary))
    }

    private fun postHeadsUp(title: String, text: String, urgent: Boolean) {
        val channel = if (urgent) CHANNEL_URGENT else CHANNEL_REQUEST
        val openIntent = PendingIntent.getActivity(
            this,
            if (urgent) 21 else 20,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        notificationManager.notify(
            if (urgent) NOTIFICATION_URGENT else NOTIFICATION_REQUEST,
            Notification.Builder(this, channel)
                .setSmallIcon(if (urgent) android.R.drawable.stat_sys_warning else android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(Notification.BigTextStyle().bigText(text))
                .setAutoCancel(true)
                .setContentIntent(openIntent)
                .build()
        )
    }

    private fun showOverlayIfAllowed() {
        if (Build.VERSION.SDK_INT < 23 || !Settings.canDrawOverlays(this) || bubbleView != null) return
        val wm = windowManager ?: return

        val display = resources.displayMetrics
        val savedSizeDp = prefs.getInt(KEY_BUBBLE_SIZE_DP, DEFAULT_BUBBLE_SIZE_DP)
            .coerceIn(MIN_BUBBLE_SIZE_DP, MAX_BUBBLE_SIZE_DP)
        val sizePx = dp(savedSizeDp)

        val params = WindowManager.LayoutParams(
            sizePx,
            sizePx,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = prefs.getInt(KEY_BUBBLE_X, display.widthPixels - sizePx - dp(8))
                .coerceIn(0, (display.widthPixels - sizePx).coerceAtLeast(0))
            y = prefs.getInt(KEY_BUBBLE_Y, ((display.heightPixels - sizePx) / 2).coerceAtLeast(0))
                .coerceIn(0, (display.heightPixels - sizePx).coerceAtLeast(0))
        }

        val bubble = TextView(this).apply {
            text = "H"
            textSize = bubbleTextSizeSp(sizePx)
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.rgb(23, 41, 56))
                setStroke(dp(2), Color.rgb(198, 164, 99))
            }
        }

        val scaleDetector = ScaleGestureDetector(
            this,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    removeQuickPanel()
                    val minSize = dp(MIN_BUBBLE_SIZE_DP)
                    val maxSize = dp(MAX_BUBBLE_SIZE_DP)
                    val nextSize = (params.width * detector.scaleFactor)
                        .roundToInt()
                        .coerceIn(minSize, maxSize)

                    if (nextSize == params.width) return true

                    val focusOnScreenX = params.x + detector.focusX
                    val focusOnScreenY = params.y + detector.focusY
                    params.width = nextSize
                    params.height = nextSize
                    params.x = (focusOnScreenX - detector.focusX).roundToInt()
                    params.y = (focusOnScreenY - detector.focusY).roundToInt()
                    clampBubbleToScreen(params)

                    bubble.textSize = bubbleTextSizeSp(nextSize)
                    safelyUpdateOverlay(bubble, params)
                    saveBubbleGeometry(params)
                    return true
                }
            }
        )

        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop
        var downRawX = 0f
        var downRawY = 0f
        var downWindowX = 0
        var downWindowY = 0
        var moved = false
        var scaled = false

        bubble.setOnTouchListener { _, event ->
            scaleDetector.onTouchEvent(event)

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downRawX = event.rawX
                    downRawY = event.rawY
                    downWindowX = params.x
                    downWindowY = params.y
                    moved = false
                    scaled = false
                    true
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    scaled = true
                    removeQuickPanel()
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (event.pointerCount > 1 || scaleDetector.isInProgress) {
                        scaled = true
                        true
                    } else {
                        val dx = event.rawX - downRawX
                        val dy = event.rawY - downRawY
                        if (!moved && (abs(dx) > touchSlop || abs(dy) > touchSlop)) {
                            moved = true
                            removeQuickPanel()
                        }
                        if (moved) {
                            params.x = downWindowX + dx.roundToInt()
                            params.y = downWindowY + dy.roundToInt()
                            clampBubbleToScreen(params)
                            safelyUpdateOverlay(bubble, params)
                        }
                        true
                    }
                }

                MotionEvent.ACTION_POINTER_UP -> {
                    scaled = true
                    saveBubbleGeometry(params)
                    true
                }

                MotionEvent.ACTION_UP -> {
                    saveBubbleGeometry(params)
                    if (!moved && !scaled && !scaleDetector.isInProgress) {
                        toggleQuickPanel()
                    }
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    saveBubbleGeometry(params)
                    true
                }

                else -> true
            }
        }

        wm.addView(bubble, params)
        bubbleView = bubble
        bubbleParams = params
    }

    private fun clampBubbleToScreen(params: WindowManager.LayoutParams) {
        val display = resources.displayMetrics
        params.x = params.x.coerceIn(0, (display.widthPixels - params.width).coerceAtLeast(0))
        params.y = params.y.coerceIn(0, (display.heightPixels - params.height).coerceAtLeast(0))
    }

    private fun saveBubbleGeometry(params: WindowManager.LayoutParams) {
        val density = resources.displayMetrics.density
        val sizeDp = (params.width / density).roundToInt()
            .coerceIn(MIN_BUBBLE_SIZE_DP, MAX_BUBBLE_SIZE_DP)
        prefs.edit()
            .putInt(KEY_BUBBLE_X, params.x)
            .putInt(KEY_BUBBLE_Y, params.y)
            .putInt(KEY_BUBBLE_SIZE_DP, sizeDp)
            .apply()
    }

    private fun bubbleTextSizeSp(sizePx: Int): Float {
        val base = dp(DEFAULT_BUBBLE_SIZE_DP).coerceAtLeast(1)
        return (20f * sizePx / base.toFloat()).coerceIn(12f, 26f)
    }

    private fun safelyUpdateOverlay(view: TextView, params: WindowManager.LayoutParams) {
        try {
            windowManager?.updateViewLayout(view, params)
        } catch (_: Exception) {
        }
    }

    private fun toggleQuickPanel() {
        if (quickPanel != null) {
            removeQuickPanel()
            return
        }
        val wm = windowManager ?: return
        val bubble = bubbleParams ?: return
        val panelWidth = dp(260)
        val display = resources.displayMetrics

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = GradientDrawable().apply {
                cornerRadius = dp(18).toFloat()
                setColor(Color.argb(245, 23, 41, 56))
                setStroke(dp(1), Color.rgb(198, 164, 99))
            }
        }
        panel.addView(TextView(this).apply {
            tag = "summary"
            text = prefs.getString(MainActivity.KEY_PENDING_REQUEST, "Sem solicitações pendentes")
            setTextColor(Color.WHITE)
            textSize = 14f
            setPadding(0, 0, 0, dp(8))
        })
        panel.addView(Button(this).apply {
            text = "Abrir painel"
            isAllCaps = false
            setOnClickListener {
                startActivity(Intent(this@DriverSessionService, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        })
        panel.addView(Button(this).apply {
            text = "Encerrar (somente parado)"
            isAllCaps = false
            setOnClickListener { stopRideSession() }
        })

        val bubbleCenter = bubble.x + bubble.width / 2
        val preferLeft = bubbleCenter > display.widthPixels / 2
        val desiredX = if (preferLeft) {
            bubble.x - panelWidth - dp(12)
        } else {
            bubble.x + bubble.width + dp(12)
        }

        val params = WindowManager.LayoutParams(
            panelWidth,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = desiredX.coerceIn(0, (display.widthPixels - panelWidth).coerceAtLeast(0))
            y = bubble.y.coerceIn(0, (display.heightPixels - dp(220)).coerceAtLeast(0))
        }
        wm.addView(panel, params)
        quickPanel = panel
    }

    private fun updateQuickPanel(summary: String) {
        val label = quickPanel?.findViewWithTag<TextView>("summary")
        label?.text = summary
    }

    private fun removeQuickPanel() {
        quickPanel?.let {
            try {
                windowManager?.removeView(it)
            } catch (_: Exception) {
            }
        }
        quickPanel = null
    }

    private fun removeOverlay() {
        removeQuickPanel()
        bubbleView?.let {
            try {
                windowManager?.removeView(it)
            } catch (_: Exception) {
            }
        }
        bubbleView = null
        bubbleParams = null
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        const val ACTION_START = "br.com.hospitalidadeabordo.driver.START"
        const val ACTION_STOP = "br.com.hospitalidadeabordo.driver.STOP"
        const val ACTION_SIMULATE_REQUEST = "br.com.hospitalidadeabordo.driver.SIMULATE_REQUEST"

        private const val CHANNEL_ACTIVE = "ride_active"
        private const val CHANNEL_REQUEST = "passenger_request"
        private const val CHANNEL_URGENT = "passenger_urgent"
        private const val NOTIFICATION_ACTIVE = 100
        private const val NOTIFICATION_REQUEST = 101
        private const val NOTIFICATION_URGENT = 102

        private const val KEY_BUBBLE_X = "overlay_bubble_x"
        private const val KEY_BUBBLE_Y = "overlay_bubble_y"
        private const val KEY_BUBBLE_SIZE_DP = "overlay_bubble_size_dp"
        private const val DEFAULT_BUBBLE_SIZE_DP = 54
        private const val MIN_BUBBLE_SIZE_DP = 32
        private const val MAX_BUBBLE_SIZE_DP = 72

        const val KEY_STARTED_AT = "ride_started_at"
        const val KEY_ENDED_AT = "ride_ended_at"
        const val KEY_START_LOCATION_AT = "ride_start_location_at"
        const val KEY_START_LAT = "ride_start_lat"
        const val KEY_START_LNG = "ride_start_lng"
        const val KEY_START_ACCURACY = "ride_start_accuracy"
        const val KEY_START_ADDRESS = "ride_start_address"
        const val KEY_END_LOCATION_AT = "ride_end_location_at"
        const val KEY_END_LAT = "ride_end_lat"
        const val KEY_END_LNG = "ride_end_lng"
        const val KEY_END_ACCURACY = "ride_end_accuracy"
        const val KEY_END_ADDRESS = "ride_end_address"
    }
}
