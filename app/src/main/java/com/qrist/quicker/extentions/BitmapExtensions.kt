package com.qrist.quicker.extentions

import android.graphics.Bitmap
import android.graphics.Rect

fun Bitmap.trim(start: Int, top: Int, end: Int, bottom: Int): Bitmap {
    val width = end - start
    val height = bottom - top
    return Bitmap.createBitmap(this, start, top, width, height)
}

fun Bitmap.trim(rect: Rect): Bitmap {
    return this.trim(rect.left, rect.top, rect.right, rect.bottom)
}
