package com.qrist.quicker.utils

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.lang.IllegalArgumentException

object QRCodeGenerator {
    private val encoder = BarcodeEncoder()

    fun generate(
        data: String,
        size: Int = 512,
        errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.H
    ): Bitmap {
        if (data.isEmpty()) {
            throw IllegalArgumentException("The data of QRCode must not be empty.")
        }

        val encodingHints = hashMapOf<EncodeHintType, Any>().also {
            it[EncodeHintType.ERROR_CORRECTION] = errorCorrectionLevel
        }

        return encoder.encodeBitmap(data, BarcodeFormat.QR_CODE, size, size, encodingHints)
    }
}
