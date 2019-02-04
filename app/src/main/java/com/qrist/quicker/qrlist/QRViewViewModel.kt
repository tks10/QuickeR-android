package com.qrist.quicker.qrlist

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.Log
import com.qrist.quicker.R
import com.qrist.quicker.data.QRCodeRepository
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.utils.convertUrlFromDrawableResId
import com.qrist.quicker.utils.serviceIdToColorDrawable

@SuppressLint("StaticFieldLeak")
class QRViewViewModel(
    private val context: Context,
    private val repository: QRCodeRepository
) : ViewModel() {
    private val qrCodeImageLiveData: MutableLiveData<String> = MutableLiveData()
    private val iconImageLiveData: MutableLiveData<String> = MutableLiveData()
    private val serviceIdLiveData: MutableLiveData<Int> = MutableLiveData()

    val qrCodeImage: LiveData<String>
        get() = qrCodeImageLiveData
    val iconImage: LiveData<String>
        get() = iconImageLiveData
    val serviceId: LiveData<Int>
        get() = serviceIdLiveData

    fun fetchImageUrl(codeId: String) {
        val qrCode = repository.getQRCode(codeId)
        qrCodeImageLiveData.value = qrCode?.qrCodeUrl ?: run {
            Log.e("qr code", "qr code image is null $codeId")
            convertUrlFromDrawableResId(context, R.drawable.ic_error_24dp)
        }
        iconImageLiveData.value = qrCode?.let {
            when (qrCode) {
                is QRCode.User -> qrCode.serviceIconUrl
                is QRCode.Default -> qrCode.serviceIconUrl
            }
        } ?: run {
            Log.e("service", "service image is null: $codeId")
            convertUrlFromDrawableResId(context, R.drawable.ic_error_24dp)
        }
        serviceIdLiveData.value = qrCode?.let {
            when (qrCode) {
                is QRCode.User -> -1
                is QRCode.Default -> qrCode.serviceId
            }
        } ?: -1
    }

    fun getBackgroundColor(serviceId: Int): ColorDrawable {
        return serviceIdToColorDrawable(serviceId)
    }
}
