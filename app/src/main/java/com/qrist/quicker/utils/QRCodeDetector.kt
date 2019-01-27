package com.qrist.quicker.utils

import android.graphics.Bitmap
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.qrist.quicker.extentions.trim

class QRCodeDetector {
    companion object {
        private val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(
                FirebaseVisionBarcode.FORMAT_QR_CODE
            )
            .build()

        private val detector = FirebaseVision.getInstance().visionBarcodeDetector

        fun detect(bitmap: Bitmap, onSuccess: (images: List<FirebaseVisionBarcode>) -> Unit) {
            val firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap)

            detector.detectInImage(firebaseVisionImage).addOnSuccessListener(onSuccess)
        }

        fun trimQRCodeIfDetected(srcBitmap: Bitmap, barcodes: List<FirebaseVisionBarcode>): Bitmap {
            if (barcodes.size != 1) return srcBitmap

            val qrCodeRect = barcodes[0].boundingBox

            qrCodeRect?.let {
                return srcBitmap.trim(it)
            } ?: return srcBitmap
        }
    }
}
