package de.t_animal.opensourcebodytracker.feature.photos.helpers

import kotlin.math.roundToLong

internal const val DEFAULT_ANIMATION_SPEED_FPS = 5f
internal const val MIN_ANIMATION_SPEED_FPS = 0.25f
internal const val MAX_ANIMATION_SPEED_FPS = 15f
internal const val PRELOAD_HEAP_BUDGET_FRACTION = 0.50f
private const val ARGB_8888_BYTES_PER_PIXEL = 4L

internal val ANIMATION_SPEED_STEPS = listOf(
    0.25f,
    0.5f,
    1f,
    2f,
    4f,
    6f,
    8f,
    10f,
    12f,
    15f,
)

internal fun nextSlowerSpeedFps(currentSpeedFps: Float): Float {
    return ANIMATION_SPEED_STEPS
        .lastOrNull { step -> step < currentSpeedFps }
        ?: MIN_ANIMATION_SPEED_FPS
}

internal fun nextFasterSpeedFps(currentSpeedFps: Float): Float {
    return ANIMATION_SPEED_STEPS
        .firstOrNull { step -> step > currentSpeedFps }
        ?: MAX_ANIMATION_SPEED_FPS
}

internal fun canDecreaseSpeed(currentSpeedFps: Float): Boolean {
    return ANIMATION_SPEED_STEPS.any { step -> step < currentSpeedFps }
}

internal fun canIncreaseSpeed(currentSpeedFps: Float): Boolean {
    return ANIMATION_SPEED_STEPS.any { step -> step > currentSpeedFps }
}

internal fun previousFrameIndex(
    currentIndex: Int,
    frameCount: Int,
): Int {
    if (frameCount <= 0) {
        return 0
    }
    return (currentIndex - 1 + frameCount) % frameCount
}

internal fun nextFrameIndex(
    currentIndex: Int,
    frameCount: Int,
): Int {
    if (frameCount <= 0) {
        return 0
    }
    return (currentIndex + 1) % frameCount
}

internal fun frameDelayMillisForSpeed(speedFps: Float): Long {
    val safeSpeed = speedFps.coerceIn(MIN_ANIMATION_SPEED_FPS, MAX_ANIMATION_SPEED_FPS)
    return (1000f / safeSpeed).roundToLong().coerceAtLeast(1L)
}

internal fun estimateDecodedMemoryBytes(
    widthPx: Int,
    heightPx: Int,
    frameCount: Int,
): Long {
    if (widthPx <= 0 || heightPx <= 0 || frameCount <= 0) {
        return 0L
    }

    return widthPx.toLong() *
        heightPx.toLong() *
        frameCount.toLong() *
        ARGB_8888_BYTES_PER_PIXEL
}

internal fun maxPreloadBudgetBytes(
    maxHeapBytes: Long,
): Long {
    if (maxHeapBytes <= 0L) {
        return 0L
    }

    return (maxHeapBytes * PRELOAD_HEAP_BUDGET_FRACTION).roundToLong().coerceAtLeast(0L)
}

internal fun isWithinPreloadMemoryBudget(
    estimatedDecodedBytes: Long,
    maxHeapBytes: Long,
): Boolean {
    return estimatedDecodedBytes <= maxPreloadBudgetBytes(maxHeapBytes)
}
