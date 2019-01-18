package com.qrist.quicker.data

import android.graphics.Bitmap
import com.qrist.quicker.models.QRCode

class QRCodeRepository(
    private val local: QRCodeDataSource
) {
    fun getQRCodes(): List<QRCode> =
        local.getQRCodes()

    fun getQRCode(id: String): QRCode? =
        local.getQRCode(id)

    fun saveQRCode(code: QRCode, image: Bitmap): Boolean =
        local.saveQRCode(code, image)

    companion object {
        private var INSTANCE: QRCodeRepository? = null

        @JvmStatic fun getInstance(qrCodeLocalDataSource: QRCodeLocalDataSource) =
                INSTANCE ?: synchronized(QRCodeRepository::class.java) {
                    INSTANCE ?: QRCodeRepository(local = qrCodeLocalDataSource)
                            .also { INSTANCE = it }
                }

        @JvmStatic fun destroyInstance() {
            INSTANCE = null
        }
    }
}
