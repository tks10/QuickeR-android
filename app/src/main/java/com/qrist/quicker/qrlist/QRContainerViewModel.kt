package com.qrist.quicker.qrlist

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.qrist.quicker.data.QRCodeRepository
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.models.TutorialType
import com.qrist.quicker.utils.QRCodeDetector

class QRContainerViewModel(
    private val context: Application,
    private val repository: QRCodeRepository
) : AndroidViewModel(context) {

    var qrCodes: List<QRCode> = repository.getQRCodes()
    var currentAdapterPosition: Int = 0

    fun fetchQRCodes() {
        qrCodes = repository.getQRCodes()
    }

    fun hasNotDoneTutorial(type: TutorialType): Boolean {
        return !repository.hasBeenDoneTutorial(type)
    }

    fun doneTutorial(type: TutorialType) {
        repository.doneTutorial(type)
    }

    fun fetchQRCodeAvailability() {
        QRCodeDetector.updateAvailability()
    }

    fun isQRCodeDetectorAvailable(): Boolean = QRCodeDetector.isAvailable
}
