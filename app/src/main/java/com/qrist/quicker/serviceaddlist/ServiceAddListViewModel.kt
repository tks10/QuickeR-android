package com.qrist.quicker.serviceaddlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.qrist.quicker.R
import com.qrist.quicker.data.QRCodeRepository
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.models.ServiceItem
import com.qrist.quicker.utils.serviceIdToIconUrl
import com.qrist.quicker.utils.serviceIdToServiceName


class ServiceAddListViewModel(
    private val context: Application,
    private val repository: QRCodeRepository
) : AndroidViewModel(context) {

    var qrCodes: List<QRCode> = repository.getQRCodes()

    private fun fetchQRCodes() {
        qrCodes = repository.getQRCodes()
    }

    fun getServiceItems(): List<ServiceItem> {
        val serviceItems = mutableListOf<ServiceItem>()
        val defaultServices = mutableListOf<QRCode.Default>()

        fetchQRCodes()

        qrCodes.forEach {
            when (it) {
                is QRCode.Default -> {
                    defaultServices.add(it)
                }
            }
        }

        QRCode.Default.DEFAULT_SERVICES_ID.forEach { id ->
            val myService = defaultServices.findLast { it.serviceId == id }
            if (myService == null) {
                serviceItems.add(
                    ServiceItem(
                        serviceName = serviceIdToServiceName(id),
                        serviceIconUrl = serviceIdToIconUrl(id)
                    )
                )
            }
        }

        serviceItems.add(
            ServiceItem(
                serviceName = context.resources.getString(R.string.other_service),
                serviceIconUrl = ""
            )
        )

        return serviceItems
    }
}
