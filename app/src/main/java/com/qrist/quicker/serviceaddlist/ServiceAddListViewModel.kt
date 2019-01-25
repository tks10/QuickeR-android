package com.qrist.quicker.serviceaddlist

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.qrist.quicker.data.QRCodeRepository
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.models.ServiceAddItem
import com.qrist.quicker.utils.serviceIdToIconUrl
import com.qrist.quicker.utils.serviceIdToServiceName


class ServiceAddListViewModel(
    private val context: Application,
    private val repository: QRCodeRepository
) : AndroidViewModel(context) {

    var qrCodes: List<QRCode> = repository.getQRCodes()

    fun getServiceAddItems(): List<ServiceAddItem> {
        val serviceAddItems = mutableListOf<ServiceAddItem>()
        val defaultServices = mutableListOf<QRCode.Default>()
        val userServices = mutableListOf<QRCode.User>()

        qrCodes.forEach {
            when (it) {
                is QRCode.Default -> {
                    defaultServices.add(it)
                }
                is QRCode.User -> {
                    userServices.add(it)
                }
            }
        }

        QRCode.Default.DEFAULT_SERVICES_ID.forEach { id ->
            val myService = defaultServices.findLast { it.serviceId == id }
            val isRegistered = myService != null
            val serviceName = serviceIdToServiceName(id)
            val serviceIcon = myService?.serviceIconUrl ?: serviceIdToIconUrl(id)
            val viewer = ServiceAddItem(
                serviceName = serviceName,
                serviceIconUrl = serviceIcon,
                isRegistered = isRegistered
            )

            serviceAddItems.add(viewer)
        }

        userServices.forEach {
            val viewer = ServiceAddItem(
                serviceName = it.serviceName,
                serviceIconUrl = it.serviceIconUrl,
                isRegistered = true
            )

            serviceAddItems.add(viewer)
        }

        return serviceAddItems
    }
}
