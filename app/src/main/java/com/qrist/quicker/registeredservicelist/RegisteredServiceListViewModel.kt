package com.qrist.quicker.registeredservicelist

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.qrist.quicker.data.QRCodeRepository
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.models.ServiceItem


class RegisteredServiceListViewModel(
    private val context: Application,
    private val repository: QRCodeRepository
) : AndroidViewModel(context) {

    var qrCodes: List<QRCode> = repository.getQRCodes()

    fun getServiceItems(): List<ServiceItem> {
        val registeredServiceItems = mutableListOf<ServiceItem>()

        qrCodes.forEach {
            registeredServiceItems.add(
                when (it) {
                    is QRCode.Default -> {
                        ServiceItem(it.serviceName, it.serviceIconUrl)
                    }
                    is QRCode.User -> {
                        ServiceItem(it.serviceName, it.serviceIconUrl)
                    }
                }
            )
        }

        return registeredServiceItems
    }

    fun fetchQRCodes() {
        qrCodes = repository.getQRCodes()
    }

    fun deleteQRCode(id: String): Boolean {
        return repository.deleteQRCode(id)
    }
}
