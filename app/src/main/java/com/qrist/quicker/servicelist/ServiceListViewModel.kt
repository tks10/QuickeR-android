package com.qrist.quicker.servicelist

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.qrist.quicker.data.QRCodeRepository
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.models.ServiceListViewer


class ServiceListViewModel(
    private val context: Application,
    private val repository: QRCodeRepository
) : AndroidViewModel(context) {

    var qrCodes: List<QRCode> = repository.getQRCodes()

    fun getServiceListViewers(): List<ServiceListViewer> {
        val serviceListViewers = mutableListOf<ServiceListViewer>()
        qrCodes.forEach {
            val serviceListViewer = when(it) {
                is QRCode.Default -> {
                    ServiceListViewer(
                        serviceName = it.serviceName,
                        serviceIconUrl = it.serviceIconUrl,
                        isRegistered = false
                    )
                }
                is QRCode.User -> {
                    ServiceListViewer(
                        serviceName = it.serviceName,
                        serviceIconUrl = it.serviceIconUrl,
                        isRegistered = false
                    )
                }
            }
            serviceListViewers.add(serviceListViewer)
        }

        return serviceListViewers
    }
}
