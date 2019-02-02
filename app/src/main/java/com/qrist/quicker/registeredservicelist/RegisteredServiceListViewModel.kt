package com.qrist.quicker.registeredservicelist

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.qrist.quicker.data.QRCodeRepository
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.models.ServiceItem
import java.lang.IllegalArgumentException


class RegisteredServiceListViewModel(
    private val context: Application,
    private val repository: QRCodeRepository
) : AndroidViewModel(context) {

    var qrCodes: List<QRCode> = repository.getQRCodes()
    private val qrCodesIndex: ArrayList<Int> = ArrayList((0 until qrCodes.size).toList())
    private val isServiceEmptyLiveDate = MutableLiveData<Boolean>().apply { value = qrCodes.isEmpty() }

    val isServiceEmpty: LiveData<Boolean>
        get() = isServiceEmptyLiveDate

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
        if (repository.deleteQRCode(id)) {
            this.fetchQRCodes()
            isServiceEmptyLiveDate.value = qrCodes.isEmpty()
            return true
        }

        return false
    }

    fun updateIndex(from: Int, to: Int) {
        if (!isInValidRange(from) || !isInValidRange(to)) throw IllegalArgumentException("Index is out of range.")

        qrCodesIndex[from] = qrCodesIndex[to].also { qrCodesIndex[to] = qrCodesIndex[from] }
    }

    fun reflectIndexChange() {
        repository.updateQRCodesOrder(qrCodesIndex)
    }

    private fun isInValidRange(position: Int): Boolean {
        return 0 <= position && position < qrCodes.size
    }
}
