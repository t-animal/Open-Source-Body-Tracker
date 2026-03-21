package de.t_animal.opensourcebodytracker.data.settings

import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.core.model.DerivedBodyMetric
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import java.time.LocalTime

fun BodyMetric.storageName(): String = this.javaClass.simpleName + ":$name"

fun parseBodyMetricSet(
    raw: Set<String>?,
    fallback: Set<BodyMetric>,
): Set<BodyMetric> {
    if (raw == null) {
        return fallback
    }

    val measuredByStorageName = MeasuredBodyMetric.entries.associateBy { it.storageName() }
    val derivedByStorageName = DerivedBodyMetric.entries.associateBy { it.storageName() }
    return raw.mapNotNull { token ->
        measuredByStorageName[token] ?: derivedByStorageName[token]
    }.toSet()
}

fun parseBodyMetricList(raw: String?): List<BodyMetric> {
    if (raw.isNullOrBlank()) return emptyList()
    val measuredByStorageName = MeasuredBodyMetric.entries.associateBy { it.storageName() }
    val derivedByStorageName = DerivedBodyMetric.entries.associateBy { it.storageName() }
    return raw.split(",").mapNotNull { token ->
        measuredByStorageName[token] ?: derivedByStorageName[token]
    }
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
