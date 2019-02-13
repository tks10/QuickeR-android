package com.qrist.quicker.qrlist

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.qrist.quicker.data.QRCodeRepository
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.models.TutorialComponent

class QRContainerViewModel(
    private val context: Application,
    private val repository: QRCodeRepository
) : AndroidViewModel(context) {

    var qrCodes: List<QRCode> = repository.getQRCodes()

    fun fetchQRCodes() {
        qrCodes = repository.getQRCodes()
    }

    fun hasNotDoneTutorial(component: TutorialComponent): Boolean {
        return !repository.hasDoneTutorial(component)
    }

    fun doneTutorial(component: TutorialComponent) {
        repository.doneTutorial(component)
    }
}
