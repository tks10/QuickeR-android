package com.qrist.quicker.data

import android.graphics.Bitmap
import com.qrist.quicker.models.QRCode

interface QRCodeDataSource {
    fun getQRCodes(): List<QRCode>
    fun getQRCode(id: String): QRCode?
    fun saveQRCode(code: QRCode, image: Bitmap): Boolean
    fun saveQRCode(serviceId: Int, qrImage: Bitmap): Boolean
    fun saveQRCode(serviceName: String, qrImage: Bitmap, iconImage: Bitmap): Boolean
}