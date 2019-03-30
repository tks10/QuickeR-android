package com.qrist.quicker.data

import android.graphics.Bitmap
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.models.TutorialType

interface QRCodeDataSource {
    fun getQRCodes(notFoundValidation: Boolean = false): List<QRCode>
    fun getQRCode(id: String): QRCode?
    fun saveQRCodes(codes: List<QRCode>): Boolean
    fun saveQRCode(code: QRCode, image: Bitmap): Boolean
    fun saveQRCode(serviceId: Int, qrImage: Bitmap): Boolean
    fun saveQRCode(serviceName: String, qrImage: Bitmap, iconImage: Bitmap): Boolean
    fun deleteQRCode(id: String): Boolean
    fun deleteIfNotFound(codes: List<QRCode>): List<QRCode>
    fun doneTutorial(type: TutorialType)
    fun hasBeenDoneTutorial(type: TutorialType): Boolean
    fun updateQRCodesOrder(indexes: List<Int>): Boolean
}