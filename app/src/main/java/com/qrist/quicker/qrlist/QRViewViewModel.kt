package com.qrist.quicker.qrlist

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.util.Log
import com.qrist.quicker.R
import com.qrist.quicker.data.QRCodeRepository
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.utils.MyApplication
import com.qrist.quicker.utils.convertUrlFromDrawableResId

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

    fun getImageUrl(codeId: String) {
        val qrCode = repository.getQRCode(codeId)
        qrCodeImageLiveData.value = qrCode?.qrCodeUrl ?: run {
            Log.e("qr code", "qr code image is null")
            convertUrlFromDrawableResId(context, R.drawable.ic_error_24dp)
        }
        iconImageLiveData.value = qrCode?.let {
            when (qrCode) {
                is QRCode.User -> qrCode.serviceIconUrl
                is QRCode.Default -> qrCode.serviceIconUrl
            }
        } ?: run {
            Log.e("service", "service image is null")
            convertUrlFromDrawableResId(context, R.drawable.ic_error_24dp)
        }
        serviceIdLiveData.value = qrCode?.let {
            when (qrCode) {
                is QRCode.User -> -1
                is QRCode.Default -> qrCode.serviceId
            }
        }
    }

    fun getBackgroundColor(serviceId: Int): ColorDrawable {
        val colorId = when (serviceId) {
            QRCode.Default.TWITTER_SERVICE_ID -> R.color.twitter
            QRCode.Default.FACEBOOK_SERVICE_ID -> R.color.facebook
            QRCode.Default.LINE_SERVICE_ID -> R.color.line
            else -> R.color.etc
        }
        return ColorDrawable(ContextCompat.getColor(MyApplication.instance, colorId))
    }
}
