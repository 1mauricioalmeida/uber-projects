package br.com.hospitalidadeabordo.driver

import java.security.MessageDigest
import java.text.NumberFormat
import java.util.Locale

object OfferParser {
    data class Leg(
        val minutes: Int,
        val km: Double,
        val address: String?
    )

    data class OfferSnapshot(
        val observedAt: Long,
        val category: String?,
        val fare: Double,
        val displayedRatePerKm: Double?,
        val rating: Double?,
        val ratingCount: Int?,
        val verified: Boolean,
        val exclusive: Boolean,
        val bonusIncluded: Double?,
        val pickup: Leg?,
        val trip: Leg?,
        val longTrip: Boolean,
        val totalMinutes: Int?,
        val totalKm: Double?,
        val grossPerKm: Double?,
        val grossPerHour: Double?,
        val grossPerMinute: Double?,
        val estimatedCost: Double?,
        val estimatedProfit: Double?,
        val confidence: Int,
        val fingerprint: String,
        val sourceTextCount: Int
    ) {
        fun compactSummary(): String {
            val parts = mutableListOf<String>()
            category?.let(parts::add)
            parts += money(fare)
            totalKm?.let { parts += "${decimal(it)} km" }
            totalMinutes?.let { parts += "$it min" }
            grossPerKm?.let { parts += "${money(it)}/km" }
            grossPerHour?.let { parts += "${money(it)}/h" }
            return parts.joinToString(" • ")
        }

        fun detailedSummary(): String {
            val rows = mutableListOf<String>()
            rows += compactSummary()
            pickup?.let {
                rows += "Embarque: ${it.minutes} min • ${decimal(it.km)} km${it.address?.let { a -> " • $a" } ?: ""}"
            }
            trip?.let {
                rows += "Viagem: ${it.minutes} min • ${decimal(it.km)} km${it.address?.let { a -> " • $a" } ?: ""}"
            }
            if (rating != null) {
                rows += "Passageiro: ${decimal(rating)}${ratingCount?.let { " • $it avaliações" } ?: ""}${if (verified) " • verificado" else ""}"
            }
            bonusIncluded?.let { rows += "Adicional incluído: ${money(it)}" }
            if (estimatedCost != null && estimatedProfit != null) {
                rows += "Custo estimado: ${money(estimatedCost)} • lucro previsto: ${money(estimatedProfit)}"
            }
            rows += "Confiança da leitura: $confidence%"
            return rows.joinToString("\n")
        }
    }

    private data class DurationMatch(
        val start: Int,
        val end: Int,
        val minutes: Int,
        val km: Double
    )

    private val durationDistanceRegex = Regex(
        pattern = "(?:(\\d+)\\s*h(?:ora(?:s)?)?(?:\\s*e\\s*)?)?(\\d+)\\s*min(?:uto(?:s)?)?\\s*\\(([0-9.,]+)\\s*km\\)",
        option = RegexOption.IGNORE_CASE
    )

    private val mainFareRegex = Regex(
        pattern = "(?<!\\+)R\\$\\s*([0-9.]+,[0-9]{2})(?!\\s*/)",
        option = RegexOption.IGNORE_CASE
    )

    private val ratePerKmRegex = Regex(
        pattern = "R\\$\\s*([0-9.]+,[0-9]{2})\\s*/\\s*km",
        option = RegexOption.IGNORE_CASE
    )

    private val bonusRegex = Regex(
        pattern = "\\+\\s*R\\$\\s*([0-9.]+,[0-9]{2})",
        option = RegexOption.IGNORE_CASE
    )

    private val ratingWithCountRegex = Regex(
        pattern = "(?:★|⭐|\\*)?\\s*([0-5][.,]\\d{1,2})\\s*\\((\\d{1,6})\\)",
        option = RegexOption.IGNORE_CASE
    )

    private val ratingOnlyRegex = Regex("^\\s*([4-5][.,]\\d{1,2})\\s*$")

    private val categoryCandidates = listOf(
        "Business Comfort",
        "Uber Black",
        "Black",
        "Comfort",
        "UberX",
        "Uber X",
        "Moto",
        "Flash"
    )

    fun parse(rawTexts: List<String>, costPerKm: Double): OfferSnapshot? {
        val texts = rawTexts
            .map { it.replace(Regex("\\s+"), " ").trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(220)

        if (texts.isEmpty()) return null
        val all = texts.joinToString("\n")
        val hasOfferAction = all.contains("Aceitar", ignoreCase = true) ||
            all.contains("Selecionar", ignoreCase = true)
        val category = categoryCandidates.firstOrNull { all.contains(it, ignoreCase = true) }
        val fare = mainFareRegex.find(all)?.groupValues?.getOrNull(1)?.toPtDouble()
            ?: return null

        val durationMatches = durationDistanceRegex.findAll(all).mapNotNull { match ->
            val hours = match.groupValues.getOrNull(1)?.toIntOrNull() ?: 0
            val minutesPart = match.groupValues.getOrNull(2)?.toIntOrNull() ?: return@mapNotNull null
            val km = match.groupValues.getOrNull(3)?.toFlexibleDouble() ?: return@mapNotNull null
            DurationMatch(
                start = match.range.first,
                end = match.range.last + 1,
                minutes = hours * 60 + minutesPart,
                km = km
            )
        }.toList()

        if (!hasOfferAction && category == null && durationMatches.isEmpty()) return null

        val legs = durationMatches.take(2).mapIndexed { index, match ->
            val nextStart = durationMatches.getOrNull(index + 1)?.start ?: all.length
            val rawAddress = all.substring(match.end.coerceAtMost(all.length), nextStart.coerceAtMost(all.length))
            Leg(match.minutes, match.km, cleanAddress(rawAddress))
        }

        val pickup = legs.getOrNull(0)
        val trip = legs.getOrNull(1)
        val totalMinutes = if (pickup != null && trip != null) pickup.minutes + trip.minutes else null
        val totalKm = if (pickup != null && trip != null) pickup.km + trip.km else null
        val grossPerKm = totalKm?.takeIf { it > 0.0 }?.let { fare / it }
        val grossPerHour = totalMinutes?.takeIf { it > 0 }?.let { fare * 60.0 / it }
        val grossPerMinute = totalMinutes?.takeIf { it > 0 }?.let { fare / it }
        val estimatedCost = totalKm?.takeIf { costPerKm > 0.0 }?.let { it * costPerKm }
        val estimatedProfit = estimatedCost?.let { fare - it }

        val displayedRate = ratePerKmRegex.find(all)?.groupValues?.getOrNull(1)?.toPtDouble()
        val ratingWithCount = ratingWithCountRegex.find(all)
        val rating = ratingWithCount?.groupValues?.getOrNull(1)?.toFlexibleDouble()
            ?: texts.asSequence().mapNotNull { ratingOnlyRegex.find(it)?.groupValues?.getOrNull(1)?.toFlexibleDouble() }.firstOrNull()
        val ratingCount = ratingWithCount?.groupValues?.getOrNull(2)?.toIntOrNull()
        val bonus = bonusRegex.find(all)?.groupValues?.getOrNull(1)?.toPtDouble()
        val verified = all.contains("Verificado", ignoreCase = true)
        val exclusive = all.contains("Exclusivo", ignoreCase = true)
        val longTrip = all.contains("Viagem longa", ignoreCase = true) || (trip?.minutes ?: 0) >= 45

        var confidence = 25
        if (category != null) confidence += 10
        if (hasOfferAction) confidence += 10
        if (pickup != null) confidence += 12
        if (trip != null) confidence += 15
        if (!pickup?.address.isNullOrBlank()) confidence += 5
        if (!trip?.address.isNullOrBlank()) confidence += 5
        if (rating != null) confidence += 5
        if (displayedRate != null) confidence += 5
        if (verified || exclusive || bonus != null || longTrip) confidence += 3
        confidence = confidence.coerceAtMost(100)

        val fingerprintSource = listOf(
            category.orEmpty(),
            fare.toString(),
            pickup?.minutes.toString(),
            pickup?.km.toString(),
            pickup?.address.orEmpty(),
            trip?.minutes.toString(),
            trip?.km.toString(),
            trip?.address.orEmpty(),
            bonus.toString()
        ).joinToString("|")

        return OfferSnapshot(
            observedAt = System.currentTimeMillis(),
            category = category,
            fare = fare,
            displayedRatePerKm = displayedRate,
            rating = rating,
            ratingCount = ratingCount,
            verified = verified,
            exclusive = exclusive,
            bonusIncluded = bonus,
            pickup = pickup,
            trip = trip,
            longTrip = longTrip,
            totalMinutes = totalMinutes,
            totalKm = totalKm,
            grossPerKm = grossPerKm,
            grossPerHour = grossPerHour,
            grossPerMinute = grossPerMinute,
            estimatedCost = estimatedCost,
            estimatedProfit = estimatedProfit,
            confidence = confidence,
            fingerprint = sha256(fingerprintSource),
            sourceTextCount = texts.size
        )
    }

    private fun cleanAddress(raw: String): String? {
        val ignoredExact = setOf(
            "Aceitar", "Selecionar", "Exclusivo", "Verificado", "Comfort", "Business Comfort",
            "Viagem longa (mais de 45 min)", "Navegar"
        )
        val pieces = raw.lines()
            .map { it.replace(Regex("\\s+"), " ").trim(' ', '•', '-', '|') }
            .filter { it.isNotBlank() }
            .filterNot { token -> ignoredExact.any { token.equals(it, ignoreCase = true) } }
            .filterNot { token -> mainFareRegex.containsMatchIn(token) || ratePerKmRegex.containsMatchIn(token) || bonusRegex.containsMatchIn(token) }
            .filterNot { token -> ratingWithCountRegex.containsMatchIn(token) }
            .filterNot { token -> token.contains("Viagem longa", ignoreCase = true) }
            .take(3)
        return pieces.joinToString(", ").takeIf { it.length >= 5 }
    }

    private fun String.toPtDouble(): Double? = replace(".", "").replace(",", ".").toDoubleOrNull()

    private fun String.toFlexibleDouble(): Double? {
        val trimmed = trim()
        return if (trimmed.contains(',') && trimmed.contains('.')) {
            trimmed.replace(".", "").replace(",", ".").toDoubleOrNull()
        } else if (trimmed.contains(',')) {
            trimmed.replace(",", ".").toDoubleOrNull()
        } else {
            trimmed.toDoubleOrNull()
        }
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray())
        .joinToString("") { "%02x".format(it) }
        .take(24)

    private fun money(value: Double): String = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)

    private fun decimal(value: Double): String = String.format(Locale("pt", "BR"), "%.2f", value)
}
