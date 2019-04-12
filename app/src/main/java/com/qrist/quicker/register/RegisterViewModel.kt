package com.qrist.quicker.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.content.ContextCompat
import android.util.Log
import com.qrist.quicker.R
import com.qrist.quicker.data.QRCodeRepository
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.models.TutorialType
import com.qrist.quicker.utils.IconGenerator
import com.qrist.quicker.utils.serviceNameToServiceId
import java.lang.IllegalStateException


class RegisterViewModel(
    private val context: Application,
    private val repository: QRCodeRepository
) : AndroidViewModel(context) {
    private val serviceNameLiveData: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }
    private val serviceIconUrlLiveData: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }
    private val qrCodeImageUrlLiveData: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }
    private val isDefaultServiceLiveData: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }
    private val isValidAsServiceLiveData: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }

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

    fun updateQRCodeImageUrl(value: String) {
        qrCodeImageUrlLiveData.value = value
        onChangeParameters()
    }

    fun restoreValues(qrCodeImageUrl: String, serviceName: String, serviceIconUrl: String, isDefaultService: Boolean) {
        qrCodeImageUrlLiveData.value = qrCodeImageUrl
        serviceNameLiveData.value = serviceName
        serviceIconUrlLiveData.value = serviceIconUrl
        isDefaultServiceLiveData.value = isDefaultService
        onChangeParameters()
    }

    private fun onChangeParameters() {
        val isServiceNameValid = serviceName.value.toString().isNotBlank()
        val isQRCodeImageUrlValid = qrCodeImageUrl.value.toString().isNotBlank()
        Log.e("values", "${serviceName.value}, ${serviceIconUrl.value}, ${qrCodeImageUrl.value}")
        isValidAsServiceLiveData.value = isServiceNameValid && isQRCodeImageUrlValid
    }

    fun saveQRCode(qrImage: Bitmap) {
        if (!isValidAsService.value!!) {
            Log.e("Register", "This implementation must have bugs...")
            return
        }
        if (isDefaultService.value!!) {
            val serviceId = serviceNameToServiceId(serviceName.value!!)
            if (hasAlreadyRegistered(serviceId)) {
                throw IllegalStateException("${serviceName.value!!} has already registered.")
            }
            repository.saveQRCode(serviceId, qrImage)
        } else {
            val letterColor = Color.WHITE
            val backgroundColor = ContextCompat.getColor(context, R.color.etc)
            val icon = IconGenerator.generateIcon(serviceName.value!!, letterColor, backgroundColor)
            repository.saveQRCode(serviceName.value!!, qrImage, icon)
        }
    }

    fun hasNotDoneTutorial(type: TutorialType): Boolean {
        return !repository.hasBeenDoneTutorial(type)
    }

    fun doneTutorial(type: TutorialType) {
        repository.doneTutorial(type)
    }

    private fun hasAlreadyRegistered(serviceId: Int): Boolean {
        val qrCodes = repository.getQRCodes()
        val registeredDefaultQRCodes = qrCodes.filter { it is QRCode.Default && it.serviceId == serviceId }
        return registeredDefaultQRCodes.isNotEmpty()
    }
}
