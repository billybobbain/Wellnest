package com.billybobbain.wellnest.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

object ImageUtils {

    private const val MAX_IMAGE_SIZE = 512 // Max width/height in pixels
    private const val JPEG_QUALITY = 85

    /**
     * Save an image URI to internal storage, resize it, and return the new file URI
     */
    fun saveProfileImage(context: Context, sourceUri: Uri, profileId: Long): String? {
        return try {
            val bitmap = loadAndResizeBitmap(context, sourceUri) ?: return null

            // Create file in internal storage
            val filename = "profile_${profileId}_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, filename)

            // Save bitmap to file
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }

            // Clean up old profile images for this profile
            deleteOldProfileImages(context, profileId, filename)

            // Return file URI
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Load image from URI, resize it to fit MAX_IMAGE_SIZE, and handle rotation
     */
    private fun loadAndResizeBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            // Get image dimensions
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            // Calculate sample size
            options.inSampleSize = calculateInSampleSize(options, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
            options.inJustDecodeBounds = false

            // Load bitmap
            val bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: return null

            // Handle EXIF rotation
            val rotatedBitmap = handleExifRotation(context, uri, bitmap)

            // Final resize if still too large
            if (rotatedBitmap.width > MAX_IMAGE_SIZE || rotatedBitmap.height > MAX_IMAGE_SIZE) {
                val scale = (MAX_IMAGE_SIZE.toFloat() /
                    maxOf(rotatedBitmap.width, rotatedBitmap.height))
                val newWidth = (rotatedBitmap.width * scale).toInt()
                val newHeight = (rotatedBitmap.height * scale).toInt()

                val resized = Bitmap.createScaledBitmap(rotatedBitmap, newWidth, newHeight, true)
                if (resized != rotatedBitmap) {
                    rotatedBitmap.recycle()
                }
                resized
            } else {
                rotatedBitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Calculate appropriate sample size for loading bitmap
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Handle EXIF orientation data to rotate image correctly
     */
    private fun handleExifRotation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )

                val matrix = Matrix()
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                    ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                    ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                    else -> return bitmap
                }

                val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (rotated != bitmap) {
                    bitmap.recycle()
                }
                rotated
            } ?: bitmap
        } catch (e: IOException) {
            e.printStackTrace()
            bitmap
        }
    }

    /**
     * Delete old profile images for a profile (keep only the current one)
     */
    private fun deleteOldProfileImages(context: Context, profileId: Long, currentFilename: String) {
        try {
            val files = context.filesDir.listFiles() ?: return
            files.filter {
                it.name.startsWith("profile_${profileId}_") &&
                it.name != currentFilename
            }.forEach { it.delete() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Delete profile image file
     */
    fun deleteProfileImage(context: Context, imagePath: String?) {
        if (imagePath.isNullOrEmpty()) return

        try {
            val file = File(imagePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Save an insurance card image URI to internal storage, resize it, and return the new file URI
     */
    fun saveInsuranceCardImage(
        context: Context,
        sourceUri: Uri,
        policyId: Long,
        side: String  // "front" or "back"
    ): String? {
        return try {
            val bitmap = loadAndResizeBitmap(context, sourceUri) ?: return null

            // Create file in internal storage with unique naming for insurance cards
            val filename = "insurance_card_${policyId}_${side}_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, filename)

            // Save bitmap to file
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }

            // Clean up old card images for this specific policy and side
            deleteOldInsuranceCardImages(context, policyId, side, filename)

            // Return file URI
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Delete old insurance card images for a specific policy and side (keep only the current one)
     */
    private fun deleteOldInsuranceCardImages(
        context: Context,
        policyId: Long,
        side: String,
        currentFilename: String
    ) {
        try {
            val files = context.filesDir.listFiles() ?: return
            files.filter {
                it.name.startsWith("insurance_card_${policyId}_${side}_") &&
                it.name != currentFilename
            }.forEach { it.delete() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Delete insurance card image file
     */
    fun deleteInsuranceCardImage(context: Context, imagePath: String?) {
        if (imagePath.isNullOrEmpty()) return

        try {
            val file = File(imagePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
