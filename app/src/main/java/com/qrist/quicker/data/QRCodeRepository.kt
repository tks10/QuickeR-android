package com.qrist.quicker.data

import com.qrist.quicker.models.QRCode

class QRCodeRepository(
    private val local: QRCodeDataSource
) {
    fun getQRCodes(): List<QRCode> =
        local.getQRCodes()

    fun getQRCode(id: String): QRCode =
        local.getQRCode(id)

    fun saveQRCodes(codes: List<QRCode>): Boolean =
        local.saveQRCodes(codes)

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
