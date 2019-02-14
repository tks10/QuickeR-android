package com.qrist.quicker.data

import android.graphics.Bitmap
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.models.TutorialComponent

class QRCodeRepository(
    private val local: QRCodeDataSource
) {
    fun getQRCodes(): List<QRCode> =
        local.getQRCodes(notFoundValidation = true)

    fun getQRCode(id: String): QRCode? =
        local.getQRCode(id)

    fun saveQRCode(code: QRCode, image: Bitmap): Boolean =
        local.saveQRCode(code, image)

    fun saveQRCode(serviceId: Int, qrImage: Bitmap): Boolean =
        local.saveQRCode(serviceId, qrImage)

    fun saveQRCode(serviceName: String, qrImage: Bitmap, iconImage: Bitmap): Boolean =
        local.saveQRCode(serviceName, qrImage, iconImage)

    fun deleteQRCode(id: String): Boolean =
        local.deleteQRCode(id)

    fun doneTutorial(component: TutorialComponent) {
        local.doneTutorial(component)
    }

    fun hasBeenDoneTutorial(component: TutorialComponent): Boolean {
        return local.hasBeenDoneTutorial(component)
    }

    fun updateQRCodesOrder(indexes: List<Int>) {
        local.updateQRCodesOrder(indexes)
    }

    companion object {
        private var INSTANCE: QRCodeRepository? = null

        @JvmStatic
        fun getInstance(qrCodeLocalDataSource: QRCodeLocalDataSource) =
            INSTANCE ?: synchronized(QRCodeRepository::class.java) {
                INSTANCE ?: QRCodeRepository(local = qrCodeLocalDataSource)
                    .also { INSTANCE = it }
            }

        @JvmStatic
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
