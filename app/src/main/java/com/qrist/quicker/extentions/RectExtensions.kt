package com.qrist.quicker.extentions

import android.graphics.Rect

fun Rect.isUnder(pixel: Int): Boolean {
    return this.width() < pixel || this.height() < pixel
}
