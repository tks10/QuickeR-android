package com.qrist.quicker.utils

import android.graphics.*
import android.util.Log
import com.vdurmont.emoji.EmojiParser


object IconGenerator {
    fun generateIcon(
        content: String,
        iconColor: Int,
        backGroundColor: Int,
        letterSize: Float = 77f,
        iconSize: Float = 128f
    ): Bitmap {
        if (letterSize > iconSize) {
            throw IllegalArgumentException("letterSize must be smaller than iconSize.")
        }
        if (content.isEmpty()) {
            throw IllegalArgumentException("content must not be empty.")
        }

        val emojis = EmojiParser.extractEmojis(content)
        val filteredContent = EmojiParser.removeAllEmojis(content)
        val firstLetter = if (filteredContent.isEmpty() || content.first() != filteredContent.first()) {
            if (emojis.isEmpty()) {
                Log.e("IconGenerator", "IllegalState!!")
                emojis.add(" ")
            }
            emojis.first()
        } else {
            content.first().toString()
        }
        val objPaint = Paint()
        val bounds = Rect()

        objPaint.isAntiAlias = true
        objPaint.color = iconColor
        objPaint.textSize = letterSize
        objPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        objPaint.textAlign = Paint.Align.CENTER
        objPaint.getTextBounds(firstLetter, 0, 1, bounds)

        val fm = objPaint.fontMetrics
        val centerToLead = -(fm.top + fm.bottom) / 2
        val objBitmap = Bitmap.createBitmap(iconSize.toInt(), iconSize.toInt(), Bitmap.Config.ARGB_8888)
        val objCanvas = Canvas(objBitmap)

        objCanvas.drawColor(backGroundColor)
        objCanvas.drawText(firstLetter, iconSize / 2, iconSize / 2 + centerToLead, objPaint)

        return objBitmap
    }
}
