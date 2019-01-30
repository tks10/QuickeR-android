package com.qrist.quicker.extentions

import android.graphics.*
import kotlin.math.max
import kotlin.math.min

fun Bitmap.trim(start: Int, top: Int, end: Int, bottom: Int, marginPercent: Int = 6): Bitmap {
    val width = end - start
    val height = bottom - top
    val margin = (width * (marginPercent.toFloat() / 100f)).toInt()
    val s = max(0, start - margin)
    val t = max(0, top - margin)
    val w = min(this.width - start - 1, width + margin * 2)
    val h = min(this.height - top - 1, height + margin * 2)
    return Bitmap.createBitmap(this, s, t, w, h, Matrix(), true)
}

fun Bitmap.trim(rect: Rect, marginPercent: Int = 6): Bitmap {
    return this.trim(rect.left, rect.top, rect.right, rect.bottom, marginPercent)
}

fun Bitmap.negative(): Bitmap {
    val mat =
        floatArrayOf(
            -1.0f,  0.0f,  0.0f, 0.0f, 255f, // red
             0.0f, -1.0f,  0.0f, 0.0f, 255f, // green
             0.0f,  0.0f, -1.0f, 0.0f, 255f, // blue
             0.0f,  0.0f,  0.0f, 1.0f, 0.0f  // alpha
        )
    val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(mat) }
    val bmp = Bitmap.createBitmap(this, 0, 0, this.width, this.height)
    val canvas = Canvas(bmp)
    canvas.drawBitmap(bmp, 0f, 0f, paint)

    return bmp
}
