package br.com.hospitalidadeabordo.driver

import android.content.Context
import org.json.JSONObject
import java.io.File

class OperationalEventStore(private val context: Context) {
    private val prefs = context.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE)
    private val eventFile = File(context.filesDir, "driver_operational_events.jsonl")

    fun recordScreenEvent(packageName: String, state: String) {
        val count = prefs.getInt(MainActivity.KEY_SCREEN_EVENT_COUNT, 0) + 1
        prefs.edit()
            .putInt(MainActivity.KEY_SCREEN_EVENT_COUNT, count)
            .putString(MainActivity.KEY_LAST_PACKAGE, packageName)
            .putString(MainActivity.KEY_LAST_SCREEN_STATE, state)
            .putLong(MainActivity.KEY_LAST_SCREEN_EVENT_AT, System.currentTimeMillis())
            .apply()
    }

    fun recordParseMiss() {
        prefs.edit()
            .putInt(MainActivity.KEY_PARSE_FAILURES, prefs.getInt(MainActivity.KEY_PARSE_FAILURES, 0) + 1)
            .apply()
    }

    fun recordOffer(offer: OfferParser.OfferSnapshot): Boolean {
        val now = System.currentTimeMillis()
        val previousHash = prefs.getString(MainActivity.KEY_LAST_OFFER_HASH, null)
        val previousAt = prefs.getLong(MainActivity.KEY_LAST_OFFER_AT, 0L)
        val isDuplicate = previousHash == offer.fingerprint && now - previousAt < DUPLICATE_WINDOW_MS

        prefs.edit()
            .putString(MainActivity.KEY_LAST_OFFER_HASH, offer.fingerprint)
            .putLong(MainActivity.KEY_LAST_OFFER_AT, now)
            .putString(MainActivity.KEY_LAST_OFFER_SUMMARY, offer.compactSummary())
            .putString(MainActivity.KEY_LAST_OFFER_DETAIL, offer.detailedSummary())
            .putInt(MainActivity.KEY_LAST_OFFER_CONFIDENCE, offer.confidence)
            .apply()

        if (isDuplicate) return false

        prefs.edit()
            .putInt(MainActivity.KEY_OFFER_COUNT, prefs.getInt(MainActivity.KEY_OFFER_COUNT, 0) + 1)
            .apply()

        val json = JSONObject().apply {
            put("type", "offer_observed")
            put("observed_at", offer.observedAt)
            put("fingerprint", offer.fingerprint)
            put("category", offer.category)
            put("fare", offer.fare)
            put("displayed_rate_per_km", offer.displayedRatePerKm)
            put("rating", offer.rating)
            put("rating_count", offer.ratingCount)
            put("verified", offer.verified)
            put("exclusive", offer.exclusive)
            put("bonus_included", offer.bonusIncluded)
            put("pickup_minutes", offer.pickup?.minutes)
            put("pickup_km", offer.pickup?.km)
            put("pickup_address", offer.pickup?.address)
            put("trip_minutes", offer.trip?.minutes)
            put("trip_km", offer.trip?.km)
            put("destination_address", offer.trip?.address)
            put("long_trip", offer.longTrip)
            put("total_minutes", offer.totalMinutes)
            put("total_km", offer.totalKm)
            put("gross_per_km", offer.grossPerKm)
            put("gross_per_hour", offer.grossPerHour)
            put("gross_per_minute", offer.grossPerMinute)
            put("estimated_cost", offer.estimatedCost)
            put("estimated_profit", offer.estimatedProfit)
            put("confidence", offer.confidence)
            put("source_text_count", offer.sourceTextCount)
        }

        runCatching {
            eventFile.appendText(json.toString() + "\n")
            trimIfNeeded()
        }
        return true
    }

    fun clearDiagnostics() {
        prefs.edit()
            .remove(MainActivity.KEY_LAST_OFFER_HASH)
            .remove(MainActivity.KEY_LAST_OFFER_AT)
            .remove(MainActivity.KEY_LAST_OFFER_SUMMARY)
            .remove(MainActivity.KEY_LAST_OFFER_DETAIL)
            .remove(MainActivity.KEY_LAST_OFFER_CONFIDENCE)
            .putInt(MainActivity.KEY_OFFER_COUNT, 0)
            .putInt(MainActivity.KEY_SCREEN_EVENT_COUNT, 0)
            .putInt(MainActivity.KEY_PARSE_FAILURES, 0)
            .remove(MainActivity.KEY_LAST_PACKAGE)
            .remove(MainActivity.KEY_LAST_SCREEN_STATE)
            .remove(MainActivity.KEY_LAST_SCREEN_EVENT_AT)
            .apply()
        runCatching { eventFile.delete() }
    }

    private fun trimIfNeeded() {
        if (!eventFile.exists() || eventFile.length() < MAX_FILE_BYTES) return
        val keep = eventFile.readLines().takeLast(MAX_LINES)
        eventFile.writeText(keep.joinToString("\n", postfix = if (keep.isNotEmpty()) "\n" else ""))
    }

    companion object {
        private const val DUPLICATE_WINDOW_MS = 20_000L
        private const val MAX_FILE_BYTES = 1_500_000L
        private const val MAX_LINES = 500
    }
}
