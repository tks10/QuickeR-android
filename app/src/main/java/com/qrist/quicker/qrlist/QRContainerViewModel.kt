package com.qrist.quicker.qrlist

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.qrist.quicker.data.QRCodeRepository
import com.qrist.quicker.models.QRCode

class QRContainerViewModel(
    context: Application,
    private val repository: QRCodeRepository
) : AndroidViewModel(context) {

    fun getQRCodes(): List<QRCode> =
        repository.getQRCodes()
}
