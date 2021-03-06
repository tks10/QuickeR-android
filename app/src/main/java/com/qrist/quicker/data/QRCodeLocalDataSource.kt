package com.qrist.quicker.data

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.models.TutorialType
import com.qrist.quicker.utils.*
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.io.File
import java.util.*

class QRCodeLocalDataSource(
    val sharedPreferences: SharedPreferences
) : QRCodeDataSource {

    private val qrCodeListAdapter: JsonAdapter<List<QRCode>> = Moshi.Builder()
        .add(
            PolymorphicJsonAdapterFactory.of(QRCode::class.java, "QRCode")
                .withSubtype(QRCode.Default::class.java, "QRCode.Define")
                .withSubtype(QRCode.User::class.java, "QRCode.User")
        )
        .build().adapter(Types.newParameterizedType(List::class.java, QRCode::class.java))

    override fun getQRCodes(notFoundValidation: Boolean): List<QRCode> {
        val json: String? = sharedPreferences.getString(PREF_NAME_MAIN, "[]")
        val codes = qrCodeListAdapter.fromJson(
            json ?: "[]"
        )!!

        return if (notFoundValidation) {
            this.deleteIfNotFound(codes)
        } else {
            codes
        }
    }

    override fun getQRCode(id: String): QRCode? {
        val qrCodes: List<QRCode> = this.getQRCodes()
        return qrCodes.findLast {
            it.id == id
        }
    }

    override fun saveQRCodes(codes: List<QRCode>): Boolean {
        val editor: SharedPreferences.Editor = sharedPreferences.edit() ?: return false
        editor.putString(PREF_NAME_MAIN, qrCodeListAdapter.toJson(codes.toList()))
        editor.apply()

        return true
    }

    override fun saveQRCode(code: QRCode, image: Bitmap): Boolean {
        // if there are same id in the shared preference, overwrite the QRCodes
        val qrCodes = getQRCodes().filter { it.id != code.id } + listOf(code)
        val editor: SharedPreferences.Editor = sharedPreferences.edit() ?: return false
        editor.putString(PREF_NAME_MAIN, qrCodeListAdapter.toJson(qrCodes.toList()))
        editor.apply()
        
        return saveImage(image, code.qrCodeUrl, IMAGE_QR_MAX)
    }

    override fun saveQRCode(serviceId: Int, qrImage: Bitmap): Boolean {
        // if there are same id in the shared preference, overwrite the QRCodes
        val id = UUID.randomUUID().toString()
        val qrCodeUrl = "$storeDirectory/$id.png"
        val qrCode = QRCode.Default(id, qrCodeUrl, serviceId)
        val qrCodes = getQRCodes().filter { it.id != qrCode.id } + listOf(qrCode)
        val editor: SharedPreferences.Editor = sharedPreferences.edit() ?: return false
        editor.putString(PREF_NAME_MAIN, qrCodeListAdapter.toJson(qrCodes.toList()))
        editor.apply()

        return saveImage(qrImage, qrCode.qrCodeUrl, IMAGE_QR_MAX)
    }

    override fun saveQRCode(serviceName: String, qrImage: Bitmap, iconImage: Bitmap): Boolean {
        // if there are same id in the shared preference, overwrite the QRCodes
        val id = UUID.randomUUID().toString()
        val qrCodeUrl = "$storeDirectory/$id.png"
        val serviceIconUrl = "$storeDirectory/${id}_icon.png"
        val qrCode = QRCode.User(id, qrCodeUrl, serviceName, serviceIconUrl)
        val qrCodes = getQRCodes().filter { it.id != qrCode.id } + listOf(qrCode)
        val editor: SharedPreferences.Editor = sharedPreferences.edit() ?: return false
        editor.putString(PREF_NAME_MAIN, qrCodeListAdapter.toJson(qrCodes.toList()))
        editor.apply()

        Log.d("RegisterViewModel", "Registered $id, $serviceName")

        return saveImage(qrImage, qrCode.qrCodeUrl, IMAGE_QR_MAX)
                && saveImage(iconImage, qrCode.serviceIconUrl, IMAGE_ICON_MAX)
    }

    override fun deleteQRCode(id: String): Boolean {
        val qrCodes = getQRCodes().filter { it.id != id }
        val editor: SharedPreferences.Editor = sharedPreferences.edit() ?: return false
        editor.putString(PREF_NAME_MAIN, qrCodeListAdapter.toJson(qrCodes.toList()))
        editor.apply()

        return true
    }

    override fun cacheQRCode(qrImage: Bitmap, cacheDir: File): Uri? =
        saveImageAsCache(qrImage, cacheDir)

    override fun deleteCache(uri: String) {
        deleteCacheImage(uri)
    }

    override fun deleteIfNotFound(codes: List<QRCode>): List<QRCode> {
        val existingCodes = mutableListOf<QRCode>()

        codes.forEach {
            if (validateExistence(it)) existingCodes.add(it)
            else this.deleteQRCode(it.id)
        }

        return existingCodes
    }

    override fun doneTutorial(type: TutorialType) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean(type.toString(), true)
        editor.apply()
    }

    override fun hasBeenDoneTutorial(type: TutorialType): Boolean {
        return sharedPreferences.getBoolean(type.toString(), false)
    }

    override fun updateQRCodesOrder(indexes: List<Int>): Boolean {
        val qrCodes = this.getQRCodes()
        val updatedQRCodes = ArrayList<QRCode>()

        for (i in 0 until indexes.size) {
            updatedQRCodes.add(qrCodes[indexes[i]])
        }

        return this.saveQRCodes(updatedQRCodes)
    }

    override fun isShowServiceNameInQRView(): Boolean {
        return sharedPreferences.getBoolean(PREF_NAME_SERVICE_NAME, true)
    }

    override fun switchServiceNameVisibilityInQRView() {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean(PREF_NAME_SERVICE_NAME, !isShowServiceNameInQRView())
        editor.apply()
    }

    companion object {
        private var INSTANCE: QRCodeLocalDataSource? = null
        private const val PREF_NAME_MAIN = "QRCodesJson"
        private const val PREF_NAME_SERVICE_NAME = "IsShowServiceNameInQRView"

        @JvmStatic
        fun getInstance(sharedPreferences: SharedPreferences) =
            INSTANCE ?: synchronized(QRCodeRepository::class.java) {
                INSTANCE ?: QRCodeLocalDataSource(sharedPreferences = sharedPreferences)
                    .also { INSTANCE = it }
            }

        @JvmStatic
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
