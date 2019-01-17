package com.qrist.quicker.data

import com.qrist.quicker.models.QRCode

class QRCodeRepository(
    private val local: QRCodeLocalDataSource
) {
    fun getQRCodes(): List<QRCode> {
        return local.getQRCodes()
    }
    fun saveQRCodes(codes: List<QRCode>): Boolean {
        return local.saveQRCodes(codes)
    }

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
