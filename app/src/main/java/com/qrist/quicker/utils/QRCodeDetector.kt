package com.qrist.quicker.utils

import android.graphics.Bitmap
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.qrist.quicker.extentions.isUnder
import com.qrist.quicker.extentions.trim


class QRCodeDetector {
    companion object {
        private val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(
                FirebaseVisionBarcode.FORMAT_ALL_FORMATS
            )
            .build()

        private val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)

        fun detect(bitmap: Bitmap, onSuccess: (images: List<FirebaseVisionBarcode>) -> Unit) {
            val firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap)

            detector.detectInImage(firebaseVisionImage).addOnSuccessListener(onSuccess)
        }

        fun trimQRCodeIfDetected(srcBitmap: Bitmap, barcodes: List<FirebaseVisionBarcode>): Bitmap {
            val processedBarcodes = dropSmallBarcode(barcodes)
            if (processedBarcodes.size != 1) return srcBitmap

            val qrCodeRect = processedBarcodes[0].boundingBox ?: return srcBitmap

            return srcBitmap.trim(qrCodeRect)
        }

        private fun dropSmallBarcode(barcodes: List<FirebaseVisionBarcode>, minPixel: Int = 92): List<FirebaseVisionBarcode> {
            val droppedBarcodes = mutableListOf<FirebaseVisionBarcode>()

            barcodes.forEach {
                it.boundingBox?.let { boudingBox ->
                    if (!boudingBox.isUnder(minPixel)) droppedBarcodes.add(it)
                }
            }

            return droppedBarcodes
        }
    }
}
