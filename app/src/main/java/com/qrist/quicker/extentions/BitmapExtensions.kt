package com.qrist.quicker.extentions

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import kotlin.math.max
import kotlin.math.min

fun Bitmap.trim(start: Int, top: Int, end: Int, bottom: Int, margin: Int = 10): Bitmap {
    val width = end - start
    val height = bottom - top
    val s = max(0, start - margin)
    val t = max(0, top - margin)
    val w = min(this.width - start - 1, width + margin * 2)
    val h = min(this.height - top - 1, height + margin * 2)
    return Bitmap.createBitmap(this, s, t, w, h, Matrix(), true)
}

fun Bitmap.trim(rect: Rect, margin: Int = 10): Bitmap {
    return this.trim(rect.left, rect.top, rect.right, rect.bottom, margin)
}
