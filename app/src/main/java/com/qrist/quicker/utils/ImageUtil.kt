package com.qrist.quicker.utils

import android.graphics.Bitmap
import java.io.FileOutputStream
import java.io.IOException

object ImageUtil {

    fun saveImage(bitmap: Bitmap, imageUrl: String): Boolean =
        try {
            val outputStream = FileOutputStream(imageUrl)
            bitmap.compress(Bitmap.CompressFormat.PNG, 70, outputStream)
            outputStream.flush()
            outputStream.close()
            true
        } catch (exception: IOException) {
            false
        }
}