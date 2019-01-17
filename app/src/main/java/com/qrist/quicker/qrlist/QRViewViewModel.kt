package com.qrist.quicker.qrlist

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.util.Log
import com.qrist.quicker.R
import com.qrist.quicker.data.QRCodeRepository
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.models.Service
import com.qrist.quicker.utils.convertUrlFromDrawableResId

@SuppressLint("StaticFieldLeak")
class QRViewViewModel(
    private val context: Context,
    private val repository: QRCodeRepository
): ViewModel() {
    private val qrCodeImageLiveData: MutableLiveData<String> = MutableLiveData()
    private val iconImageLiveData: MutableLiveData<String> = MutableLiveData()

    val qrCodeImage: LiveData<String>
        get() = qrCodeImageLiveData
    val iconImage: LiveData<String>
        get() = iconImageLiveData

    fun getImageUrl(codeId: String) {
        val qrCode = repository.getQRCode(codeId)
        Log.d("imagepath", convertUrlFromDrawableResId(context, R.drawable.qr_code))
        qrCodeImageLiveData.value = qrCode.qrCodeUrl
        iconImageLiveData.value = when (qrCode) {
            is QRCode.User -> qrCode.service.iconUrl
            is QRCode.Default -> {
                val service = qrCode.service
                when (service) {
                    is Service.TwitterService -> convertUrlFromDrawableResId(context, R.drawable.qr_code)
                    is Service.FacebookService -> convertUrlFromDrawableResId(context, R.drawable.qr_code)
                    is Service.LineService -> convertUrlFromDrawableResId(context, R.drawable.qr_code)
                    // if null TODO: remove ability of UserService from QRCode.Default#service
                    else -> convertUrlFromDrawableResId(context, R.drawable.qr_code)
                }
            }
            is QRCode.Error -> convertUrlFromDrawableResId(context, R.drawable.qr_code)
        }
    }
}
