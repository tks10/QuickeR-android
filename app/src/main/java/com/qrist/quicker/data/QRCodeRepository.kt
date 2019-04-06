package com.qrist.quicker.data

import android.graphics.Bitmap
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.models.TutorialType

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

    fun doneTutorial(type: TutorialType) {
        local.doneTutorial(type)
    }

    fun hasBeenDoneTutorial(type: TutorialType): Boolean {
        return local.hasBeenDoneTutorial(type)
    }

    fun updateQRCodesOrder(indexes: List<Int>) {
        local.updateQRCodesOrder(indexes)
    }

    fun isShowServiceNameInQRView(): Boolean {
        return local.isShowServiceNameInQRView()
    }

    fun switchServiceNameVisiblityInQRView() {
        local.switchServiceNameVisibilityInQRView()
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
