package com.qrist.quicker.utils

import android.graphics.*
import java.lang.IllegalArgumentException


object IconGenerator {
    fun generateIcon(
        content: String,
        letterSize: Float = 64f,
        iconSize: Int = 128,
        backGroundColor: Int = Color.argb(255, 97, 97, 97),
        iconColor: Int = Color.WHITE
    ): Bitmap {
        if (letterSize > iconSize) {
            throw IllegalArgumentException("letterSize must be smaller than iconSize.")
        }

        val letter = if (content.isNotEmpty()) content.first().toString() else " "
        val objPaint = Paint()

        objPaint.isAntiAlias = true
        objPaint.color = iconColor
        objPaint.textSize = letterSize
        objPaint.getTextBounds(letter, 0, 1, Rect(0, 0, iconSize, iconSize))

        val fm = objPaint.fontMetrics
        val textWidth = objPaint.measureText(letter)
        val textHeight = (Math.abs(fm.top) + fm.bottom)
        val objBitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)

        val objCanvas = Canvas(objBitmap)
        objCanvas.drawColor(backGroundColor)
        objCanvas.drawText(letter, (iconSize - textWidth) / 2, textHeight, objPaint)

        return objBitmap
    }
}
