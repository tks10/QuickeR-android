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
    private val serviceNameLiveData: MutableLiveData<Int> = MutableLiveData()
    private val iconImageUrlLiveData: MutableLiveData<String> = MutableLiveData()

    val iconImageUrl: LiveData<String>
        get() = iconImageUrlLiveData
    val serviceName: LiveData<Int>
        get() = serviceNameLiveData

    var qrCodes: List<QRCode> = repository.getQRCodes()


}
