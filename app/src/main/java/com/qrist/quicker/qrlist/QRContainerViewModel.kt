package com.qrist.quicker.qrlist

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.util.Log
import com.qrist.quicker.data.QRCodeRepository
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.models.Service

class QRContainerViewModel(
    context: Application,
    private val repository: QRCodeRepository
) : AndroidViewModel(context) {

    fun getQRCodes(): List<String> =
        repository.getQRCodes().map {
            Log.d("QRCode", it.toString())
            when (it) {
                is QRCode.User -> {
                    when(it.service) {
                        is Service.UserService ->
                            it.service.name
                        else -> "some error occurred"
                    }
                }
                else -> "some error occurred"
            }
        }!!
}
