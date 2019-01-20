package com.qrist.quicker.servicelist

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.qrist.quicker.data.QRCodeRepository
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.models.ServiceListViewer
import com.qrist.quicker.utils.serviceIdToIconUrl
import com.qrist.quicker.utils.serviceIdToServiceName


class ServiceListViewModel(
    private val context: Application,
    private val repository: QRCodeRepository
) : AndroidViewModel(context) {

    var qrCodes: List<QRCode> = repository.getQRCodes()

    fun getServiceListViewers(): List<ServiceListViewer> {
        val serviceListViewers = mutableListOf<ServiceListViewer>()
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
            val viewer = ServiceListViewer(
                serviceName = serviceName,
                serviceIconUrl = serviceIcon,
                isRegistered = isRegistered
            )

            serviceListViewers.add(viewer)
        }

        userServices.forEach {
            val viewer = ServiceListViewer(
                serviceName = it.serviceName,
                serviceIconUrl = it.serviceIconUrl,
                isRegistered = true
            )

            serviceListViewers.add(viewer)
        }

        return serviceListViewers
    }
}
