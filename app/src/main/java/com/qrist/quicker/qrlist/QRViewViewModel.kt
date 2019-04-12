package com.qrist.quicker.qrlist

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.graphics.drawable.ColorDrawable
import android.util.Log
import com.qrist.quicker.R
import com.qrist.quicker.data.QRCodeRepository
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.utils.convertUrlFromDrawableResId
import com.qrist.quicker.utils.serviceIdToColorDrawable

@SuppressLint("StaticFieldLeak")
class QRViewViewModel(
    private val context: Application,
    private val repository: QRCodeRepository
) : AndroidViewModel(context) {
    private val qrCodeImageLiveData: MutableLiveData<String> = MutableLiveData()
    private val iconImageLiveData: MutableLiveData<String> = MutableLiveData()
    private val serviceIdLiveData: MutableLiveData<Int> = MutableLiveData()
    private val serviceNameLiveData: MutableLiveData<String> = MutableLiveData()

    val qrCodeImage: LiveData<String>
        get() = qrCodeImageLiveData
    val iconImage: LiveData<String>
        get() = iconImageLiveData
    val serviceId: LiveData<Int>
        get() = serviceIdLiveData
    val serviceName: LiveData<String>
        get() = serviceNameLiveData

    fun fetchImageUrl(codeId: String) {
        val qrCode = repository.getQRCode(codeId)
        qrCodeImageLiveData.value = qrCode?.qrCodeUrl ?: run {
            Log.e("qr code", "qr code image is null $codeId")
            convertUrlFromDrawableResId(context, R.drawable.ic_error_24dp)
        }
        iconImageLiveData.value = qrCode?.serviceIconUrl ?: run {
            Log.e("service", "service image is null: $codeId")
            convertUrlFromDrawableResId(context, R.drawable.ic_error_24dp)
        }
        serviceIdLiveData.value = qrCode?.let {
            when (qrCode) {
                is QRCode.User -> -1
                is QRCode.Default -> qrCode.serviceId
            }
        } ?: -1
        serviceNameLiveData.value = qrCode?.serviceName ?: run {
            Log.e("service", "service name is null: $codeId")
            ""
        }
    }

    fun getBackgroundColor(serviceId: Int): ColorDrawable {
        return serviceIdToColorDrawable(serviceId)
    }
}
