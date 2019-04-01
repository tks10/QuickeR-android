package com.qrist.quicker.utils

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.qrist.quicker.extentions.isUnder
import com.qrist.quicker.extentions.negative
import com.qrist.quicker.extentions.trim

object QRCodeDetector {
    private val emptyFirebaseImage = getEmptyImage().let { FirebaseVisionImage.fromBitmap(it) }
    var isAvailable: Boolean = false
        private set

    private val options = FirebaseVisionBarcodeDetectorOptions.Builder()
        .setBarcodeFormats(
            FirebaseVisionBarcode.FORMAT_QR_CODE
        )
        .build()

    private val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)

    fun updateAvailability() {
        val start = System.currentTimeMillis()
        detector
            .detectInImage(emptyFirebaseImage)
            .addOnSuccessListener {
                isAvailable = true
                val elapsed = System.currentTimeMillis() - start
                Log.d("Firebase Validation", "$elapsed [ms]")
                Log.d("Firebase Validation", "Model is available.")
            }
            .addOnFailureListener {
                isAvailable = false
                val elapsed = System.currentTimeMillis() - start
                Log.d("Firebase Validation", "$elapsed [ms]")
                Log.e("Firebase Validation", "Model is unavailable.")
            }
    }

    fun detect(
        bitmap: Bitmap,
        onSuccess: (images: List<FirebaseVisionBarcode>) -> Unit,
        onFailure: (func: Exception) -> Unit
    ) {
        val firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap)
        detect(firebaseVisionImage, onSuccess, onFailure)
    }

    fun detect(
        image: FirebaseVisionImage,
        onSuccess: (images: List<FirebaseVisionBarcode>) -> Unit,
        onFailure: (func: Exception) -> Unit
    ) {
        detector
            .detectInImage(image)
            .addOnSuccessListener(onSuccess)
            .addOnFailureListener(onFailure)
    }

    fun detectOnNegativeImage(
        bitmap: Bitmap,
        onSuccess: (images: List<FirebaseVisionBarcode>) -> Unit,
        onFailure: (func: Exception) -> Unit
    ) {
        this.detect(bitmap.negative(), onSuccess, onFailure)
    }

    fun trimQRCodeIfDetected(srcBitmap: Bitmap, barcodes: List<FirebaseVisionBarcode>): Bitmap? {
        val processedBarcodes = dropSmallBarcode(barcodes)
        if (processedBarcodes.size != 1) return null

        val qrCodeRect = processedBarcodes[0].boundingBox ?: return null

        return srcBitmap.trim(qrCodeRect)
    }

    private fun dropSmallBarcode(
        barcodes: List<FirebaseVisionBarcode>,
        minPixel: Int = 92
    ): List<FirebaseVisionBarcode> {
        val droppedBarcodes = mutableListOf<FirebaseVisionBarcode>()

        barcodes.forEach {
            it.boundingBox?.let { boudingBox ->
                if (!boudingBox.isUnder(minPixel)) droppedBarcodes.add(it)
            }
        }

        return droppedBarcodes
    }
}
