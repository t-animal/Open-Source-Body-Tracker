package de.t_animal.opensourcebodytracker.core.model

enum class PhotoQuality(val maxDimensionPx: Int, val jpegQuality: Int) {
    Original(maxDimensionPx = 0, jpegQuality = 0),
    High(maxDimensionPx = 2560, jpegQuality = 90),
    Medium(maxDimensionPx = 1920, jpegQuality = 80),
    Low(maxDimensionPx = 1280, jpegQuality = 70),
}
