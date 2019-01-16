package com.qrist.quicker.data

import com.qrist.quicker.models.QRCode

interface QRCodeDataSource {
    fun getQRCodes(): List<QRCode>?
    fun saveQRCodes(codes: List<QRCode>): Boolean
}