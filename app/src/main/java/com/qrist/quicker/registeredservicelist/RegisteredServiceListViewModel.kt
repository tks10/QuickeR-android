package com.qrist.quicker.registeredservicelist

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.qrist.quicker.data.QRCodeRepository
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.models.RegisteredServiceItem


class RegisteredServiceListViewModel(
    private val context: Application,
    private val repository: QRCodeRepository
) : AndroidViewModel(context) {

    var qrCodes: List<QRCode> = repository.getQRCodes()

    fun getRegisteredServiceItems(): List<RegisteredServiceItem> {
        val registeredServiceItems = mutableListOf<RegisteredServiceItem>()

        qrCodes.forEach {
            registeredServiceItems.add(
                when (it) {
                    is QRCode.Default -> {
                        RegisteredServiceItem(it.serviceName, it.serviceIconUrl)
                    }
                    is QRCode.User -> {
                        RegisteredServiceItem(it.serviceName, it.serviceIconUrl)
                    }
                }
            )
        }

        return registeredServiceItems
    }
}
