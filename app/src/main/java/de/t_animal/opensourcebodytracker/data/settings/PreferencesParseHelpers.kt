package de.t_animal.opensourcebodytracker.data.settings

import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.core.model.DerivedBodyMetric
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import java.time.LocalTime

private const val MEASURED_PREFIX = "MeasuredBodyMetric"
private const val DERIVED_PREFIX = "DerivedBodyMetric"

fun BodyMetric.storageName(): String = when (this) {
    is MeasuredBodyMetric -> "$MEASURED_PREFIX:$name"
    is DerivedBodyMetric -> "$DERIVED_PREFIX:$name"
    else -> "$MEASURED_PREFIX:$name"
}

private fun buildMetricLookup(): Map<String, BodyMetric> = buildMap {
    for (m in MeasuredBodyMetric.entries) {
        put(m.storageName(), m)
        // Also accept tokens written by R8-obfuscated builds where javaClass.simpleName
        // was mangled. The enum constant name alone is unique across both enum types, so
        // falling back to ":Name" matching recovers those entries.
        // This can be removed once we no longer support restoring preferences from R8-
        // obfuscated versions of the app (i.e. around end of 2026 roughly).
        put(m.name, m)
    }
    for (d in DerivedBodyMetric.entries) {
        put(d.storageName(), d)
        put(d.name, d)
    }
}

private val metricLookup: Map<String, BodyMetric> by lazy { buildMetricLookup() }

private fun resolveToken(token: String): BodyMetric? =
    metricLookup[token] ?: metricLookup[token.substringAfter(":", missingDelimiterValue = "")]

fun parseBodyMetricSet(
    raw: Set<String>?,
    fallback: Set<BodyMetric>,
): Set<BodyMetric> {
    if (raw == null) return fallback
    val parsed = raw.mapNotNull { resolveToken(it) }.toSet()
    return if (parsed.isEmpty()) fallback else parsed
}

fun parseBodyMetricList(raw: String?): List<BodyMetric> {
    if (raw.isNullOrBlank()) return emptyList()
    return raw.split(",").mapNotNull { resolveToken(it) }
}

fun <E : Enum<E>> parseEnumSet(
    raw: Set<String>?,
    values: List<E>,
    fallback: Set<E>,
): Set<E> {
    if (raw == null) {
        return fallback
    }

    val byName = values.associateBy { it.name }
    val parsed = raw.mapNotNull { byName[it] }.toSet()
    return if (parsed.isEmpty()) fallback else parsed
}

fun <E : Enum<E>> parseEnum(raw: String?, values: List<E>, fallback: E): E {
    if (raw.isNullOrBlank()) return fallback
    return values.firstOrNull { it.name == raw } ?: fallback
}

fun parseLocalTime(
    raw: String?,
    fallback: LocalTime,
): LocalTime {
    if (raw.isNullOrBlank()) {
        return fallback
    }

    return runCatching { LocalTime.parse(raw) }.getOrDefault(fallback)
}
