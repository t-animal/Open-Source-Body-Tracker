package de.t_animal.opensourcebodytracker.data.photos

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import de.t_animal.opensourcebodytracker.core.model.PhotoQuality
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToInt

class PhotoCompressor @Inject constructor() {

    /**
     * Compresses [sourceFile] and writes the result to [targetFile].
     * Returns `false` without writing if the source was already compressed at the same or
     * lower quality (detected via EXIF UserComment written by a previous compression).
     */
    fun compressPhoto(
        sourceFile: File,
        targetFile: File,
        photoQuality: PhotoQuality,
    ): Boolean {
        val sourceExif = ExifInterface(sourceFile)

        // Skip if already compressed at equal or lower quality to avoid lossy re-encoding.
        val previousQuality = sourceExif.getAttribute(ExifInterface.TAG_USER_COMMENT)
            ?.removePrefix("photoQuality=")
            ?.let { runCatching { PhotoQuality.valueOf(it) }.getOrNull() }
        if (previousQuality != null && previousQuality.ordinal >= photoQuality.ordinal) {
            return false
        }

        val exifOrientation = sourceExif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL,
        )

        // Pass 1: read JPEG header only to get dimensions — no pixels decoded, no memory allocated.
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(sourceFile.absolutePath, options)
        val originalWidth = options.outWidth
        val originalHeight = options.outHeight

        val orientedWidth = if (exifSwapsDimensions(exifOrientation)) originalHeight else originalWidth
        val orientedHeight = if (exifSwapsDimensions(exifOrientation)) originalWidth else originalHeight
        val longestSide = max(orientedWidth, orientedHeight)

        // Pass 2: decode pixels with inSampleSize for coarse downsampling during decode.
        // inSampleSize only supports powers of 2, so this gets us close to the target without
        // ever loading the full-resolution bitmap into memory (e.g. 50MP → ~12.5MP at sampleSize=2).
        val maxDimensionPx = photoQuality.maxDimensionPx
        val sampleSize = calculateInSampleSize(longestSide, maxDimensionPx)
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val sampledBitmap = BitmapFactory.decodeFile(sourceFile.absolutePath, decodeOptions)
            ?: throw IllegalStateException("Failed to decode photo: ${sourceFile.absolutePath}")

        // Precise scale to exact target: inSampleSize gives us ~2x the target at most,
        // this final step resizes to the exact maxDimensionPx (e.g. 2000px → 1920px).
        val scaledBitmap = scaleDown(sampledBitmap, maxDimensionPx)
        if (scaledBitmap !== sampledBitmap) {
            sampledBitmap.recycle()
        }

        FileOutputStream(targetFile).use { out ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, photoQuality.jpegQuality, out)
        }
        scaledBitmap.recycle()

        // Copy EXIF orientation to output (bitmap.compress strips all EXIF data) and
        // record which quality setting was applied.
        val targetExif = ExifInterface(targetFile)
        targetExif.setAttribute(ExifInterface.TAG_ORIENTATION, exifOrientation.toString())
        targetExif.setAttribute(ExifInterface.TAG_USER_COMMENT, "photoQuality=${photoQuality.name}")
        targetExif.saveAttributes()

        return true
    }

    private fun calculateInSampleSize(longestSide: Int, maxDimensionPx: Int): Int {
        var sampleSize = 1
        while (longestSide / (sampleSize * 2) >= maxDimensionPx) {
            sampleSize *= 2
        }
        return sampleSize
    }

    private fun scaleDown(bitmap: Bitmap, maxDimensionPx: Int): Bitmap {
        val longestSide = max(bitmap.width, bitmap.height)
        if (longestSide <= maxDimensionPx) return bitmap

        val scale = maxDimensionPx.toFloat() / longestSide
        val newWidth = (bitmap.width * scale).roundToInt()
        val newHeight = (bitmap.height * scale).roundToInt()
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun exifSwapsDimensions(orientation: Int): Boolean = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90,
        ExifInterface.ORIENTATION_ROTATE_270,
        ExifInterface.ORIENTATION_TRANSPOSE,
        ExifInterface.ORIENTATION_TRANSVERSE,
        -> true
        else -> false
    }
}
