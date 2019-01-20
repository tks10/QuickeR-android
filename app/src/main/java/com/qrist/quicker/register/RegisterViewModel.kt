package com.qrist.quicker.register

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import com.qrist.quicker.data.QRCodeRepository
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.utils.serviceNameToServiceId
import java.io.File


class RegisterViewModel(
    private val context: Application,
    private val repository: QRCodeRepository
) : AndroidViewModel(context) {
    private val serviceNameLiveData: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }
    private val serviceIconUrlLiveData: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }
    private val qrCodeImageUrlLiveData: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }
    private val isDefaultServiceLiveData: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }
    private val isValidAsServiceLiveData: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }

    private val directory = File(Environment.getExternalStorageDirectory().absolutePath + "/DCIM/QuickeR/")

    val serviceIconUrl: LiveData<String>
        get() = serviceIconUrlLiveData
    val serviceName: LiveData<String>
        get() = serviceNameLiveData
    val qrCodeImageUrl: LiveData<String>
        get() = qrCodeImageUrlLiveData
    val isDefaultService: LiveData<Boolean>
        get() = isDefaultServiceLiveData
    val isValidAsService: LiveData<Boolean>
        get() = isValidAsServiceLiveData

    var qrCodes: List<QRCode> = repository.getQRCodes()

    fun initServiceInformation(serviceName: String, serviceIconUrl: String) {
        serviceNameLiveData.value = serviceName
        serviceIconUrlLiveData.value = serviceIconUrl
        isDefaultServiceLiveData.value = true
        onChangeParameters()
    }

    fun updateServiceName(value: String) {
        serviceNameLiveData.value = value
        onChangeParameters()
    }

    fun updateServiceIconUrl(value: String) {
        serviceIconUrlLiveData.value = value
        onChangeParameters()
    }

    fun updateQRCodeImageUrl(value: String) {
        qrCodeImageUrlLiveData.value = value
        onChangeParameters()
    }

    private fun onChangeParameters() {
        val isServiceNameValid = serviceName.value.toString().isNotBlank()
        val isServiceIconUrlValid = serviceIconUrl.value.toString().isNotBlank()
        val isQRCodeImageUrlValid = qrCodeImageUrl.value.toString().isNotBlank()
        isValidAsServiceLiveData.value = isServiceNameValid && isServiceIconUrlValid && isQRCodeImageUrlValid
    }

    fun saveQRCode(qrCodeImage: Bitmap) {
        if (!isValidAsService.value!!) {
            Log.e("Register", "This implementation must have bugs...")
            return
        }
        val qrCode = if (isDefaultService.value!!) {
            QRCode.Default(
                id = "100",
                qrCodeUrl = directory.absolutePath + "/qr_code.png",
                serviceId = serviceNameToServiceId(serviceName.value!!)
            )
        } else {
            QRCode.User(
                id = "100",
                qrCodeUrl = directory.absolutePath + "/qr_code.png",
                serviceName = serviceName.value!!,
                serviceIconUrl = serviceIconUrl.value!!
            )
        }

        repository.saveQRCode(qrCode, qrCodeImage)
    }
}
