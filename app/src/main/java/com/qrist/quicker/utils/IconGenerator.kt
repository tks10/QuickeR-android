package com.qrist.quicker.utils

import android.graphics.*
import com.vdurmont.emoji.EmojiParser


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

        val filteredContent = EmojiParser.removeAllEmojis(content)
        val letter = if (filteredContent.isNotEmpty()) filteredContent.first().toString() else " "
        val objPaint = Paint()
        val bounds = Rect()

        objPaint.isAntiAlias = true
        objPaint.color = iconColor
        objPaint.textSize = letterSize
        objPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        objPaint.textAlign = Paint.Align.CENTER
        objPaint.getTextBounds(letter, 0, 1, bounds)

        val textHeight = bounds.height().toFloat()
        val objBitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
        val objCanvas = Canvas(objBitmap)
        objCanvas.drawColor(backGroundColor)
        objCanvas.drawText(letter, iconSize / 2f, (iconSize - textHeight) / 2 + textHeight, objPaint)

        return objBitmap
    }
}
