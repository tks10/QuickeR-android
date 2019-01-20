package com.qrist.quicker.register

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.qrist.quicker.data.QRCodeRepository
import com.qrist.quicker.models.QRCode


class RegisterViewModel(
    private val context: Application,
    private val repository: QRCodeRepository
) : AndroidViewModel(context) {
    private val serviceNameLiveData: MutableLiveData<String> = MutableLiveData()
    private val serviceIconUrlLiveData: MutableLiveData<String> = MutableLiveData()
    private val isDefaultServiceLiveData: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }

    val serviceIconUrl: LiveData<String>
        get() = serviceIconUrlLiveData
    val serviceName: LiveData<String>
        get() = serviceNameLiveData
    val isDefaultService: LiveData<Boolean>
        get() = isDefaultServiceLiveData

    var qrCodes: List<QRCode> = repository.getQRCodes()

    fun initServiceInformation(serviceName: String, serviceIconUrl: String) {
        serviceNameLiveData.value = serviceName
        serviceIconUrlLiveData.value = serviceIconUrl
        isDefaultServiceLiveData.value = true
    }
}
