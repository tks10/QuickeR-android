package com.qrist.quicker.qrlist

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.qrist.quicker.R
import com.qrist.quicker.data.QRCodeRepository
import com.qrist.quicker.models.QRCode

class QRContainerViewModel(
    private val context: Application,
    private val repository: QRCodeRepository
) : AndroidViewModel(context) {

    var qrCodes: List<QRCode> = repository.getQRCodes()

    fun getQRCodes() {
        qrCodes = repository.getQRCodes()
    }

    fun saveQRCodes() {
        val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.qr_code)
        qrCodes.map { repository.saveQRCode(it, bitmap) }
    }
}
