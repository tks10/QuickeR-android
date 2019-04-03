package com.qrist.quicker.registeredservicelist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.qrist.quicker.data.QRCodeRepository
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.models.ServiceItem
import java.lang.IllegalArgumentException

class RegisteredServiceListViewModel(
    private val context: Application,
    private val repository: QRCodeRepository
) : AndroidViewModel(context) {

    var qrCodes: List<QRCode> = repository.getQRCodes()
    private var qrCodesIndex: ArrayList<Int> = ArrayList((0 until qrCodes.size).toList())
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
        qrCodesIndex = ArrayList((0 until qrCodes.size).toList())
    }

    fun deleteQRCode(id: String): Boolean {
        if (repository.deleteQRCode(id)) {
            this.fetchQRCodes()
            isServiceEmptyLiveDate.value = qrCodes.isEmpty()
            return true
        }

        return false
    }

    fun moveIndex(from: Int, to: Int) {
        if (!isInValidRange(from) || !isInValidRange(to)) throw IllegalArgumentException("Index is out of range.")
        if (from == to) return

        if (to > from) {
            for (i in from until to) {
                qrCodesIndex[i] = qrCodesIndex[i+1].also { qrCodesIndex[i+1] = qrCodesIndex[i]}
            }
        } else {
            for (i in from downTo to+1) {
                qrCodesIndex[i] = qrCodesIndex[i-1].also { qrCodesIndex[i-1] = qrCodesIndex[i]}
            }
        }
    }

    fun reflectIndexChange() {
        repository.updateQRCodesOrder(qrCodesIndex)
        for (i in 0 until qrCodes.size) qrCodesIndex[i] = i
    }

    private fun isInValidRange(position: Int): Boolean {
        return 0 <= position && position < qrCodes.size
    }
}
