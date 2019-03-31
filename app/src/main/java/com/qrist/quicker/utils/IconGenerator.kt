package com.qrist.quicker.utils

import android.graphics.*
import java.lang.IllegalArgumentException


object IconGenerator {
    fun generateIcon(
        content: String,
        letterSize: Float = 79f,
        iconSize: Int = 128,
        backGroundColor: Int = Color.argb(255, 140, 140, 140),
        iconColor: Int = Color.WHITE
    ): Bitmap {
        if (letterSize > iconSize) {
            throw IllegalArgumentException("letterSize must be smaller than iconSize.")
        }

        val letter = if (content.isNotEmpty()) content.first().toString() else " "
        val objPaint = Paint()
        val bounds = Rect()

        objPaint.isAntiAlias = true
        objPaint.color = iconColor
        objPaint.textSize = letterSize
        objPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        objPaint.getTextBounds(letter, 0, 1, bounds)

        val textWidth = bounds.width().toFloat()
        val textHeight = bounds.height().toFloat()
        val objBitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
        val objCanvas = Canvas(objBitmap)
        objCanvas.drawColor(backGroundColor)
        objCanvas.drawText(letter, (iconSize - textWidth) / 2, (iconSize - textHeight) / 2 + textHeight, objPaint)

        return objBitmap
    }
}
