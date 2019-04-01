package com.qrist.quicker.utils

import android.graphics.*
import com.vdurmont.emoji.EmojiParser


object IconGenerator {
    fun generateIcon(
        content: String,
        iconColor: Int,
        backGroundColor: Int,
        letterSize: Float = 79f,
        iconSize: Float = 128f
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

        val fm = objPaint.fontMetrics
        val centerToLead = -(fm.top + fm.bottom) / 2
        val objBitmap = Bitmap.createBitmap(iconSize.toInt(), iconSize.toInt(), Bitmap.Config.ARGB_8888)
        val objCanvas = Canvas(objBitmap)

        objCanvas.drawColor(backGroundColor)
        objCanvas.drawText(letter, iconSize / 2, iconSize / 2 + centerToLead, objPaint)

        return objBitmap
    }
}
