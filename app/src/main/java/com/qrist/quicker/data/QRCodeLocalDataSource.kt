package com.qrist.quicker.data

import android.content.SharedPreferences
import android.graphics.Bitmap
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.utils.externalStorageRootDirectory
import com.qrist.quicker.utils.saveImage
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.util.*

class QRCodeLocalDataSource(
    val sharedPreferences: SharedPreferences
) : QRCodeDataSource {

    private val qrCodeListAdapter: JsonAdapter<List<QRCode>> = Moshi.Builder()
        .add(PolymorphicJsonAdapterFactory.of(QRCode::class.java, "QRCode")
            .withSubtype(QRCode.Default::class.java, "QRCode.Define")
            .withSubtype(QRCode.User::class.java, "QRCode.User"))
        .build().adapter(Types.newParameterizedType(List::class.java, QRCode::class.java))

    override fun getQRCodes(): List<QRCode> {
        val json: String? = sharedPreferences.getString(PREF_NAME, "[]")
        return qrCodeListAdapter.fromJson(
            json ?: "[]"
        )!!
    }

    override fun getQRCode(id: String): QRCode? {
        val qrCodes: List<QRCode> = this.getQRCodes()
        return qrCodes.findLast {
            it.id == id
        }
    }

    override fun saveQRCode(code: QRCode, image: Bitmap): Boolean {
        // if there are same id in the shared preference, overwrite the QRCodes
        val qrCodes = getQRCodes().filter { it.id != code.id } + listOf(code)
        val editor: SharedPreferences.Editor = sharedPreferences.edit() ?: return false
        editor.putString(PREF_NAME, qrCodeListAdapter.toJson(qrCodes.toList()))
        editor.apply()
        return saveImage(image, code.qrCodeUrl)
    }

    override fun saveQRCode(serviceId: Int, qrImage: Bitmap): Boolean {
        // if there are same id in the shared preference, overwrite the QRCodes
        val id = UUID.randomUUID().toString()
        val qrCodeUrl = "$externalStorageRootDirectory/$id.png"
        val qrCode = QRCode.Default(id, qrCodeUrl, serviceId)
        val qrCodes = getQRCodes().filter { it.id != qrCode.id } + listOf(qrCode)
        val editor: SharedPreferences.Editor = sharedPreferences.edit() ?: return false
        editor.putString(PREF_NAME, qrCodeListAdapter.toJson(qrCodes.toList()))
        editor.apply()
        return saveImage(qrImage, qrCode.qrCodeUrl)
    }

    override fun saveQRCode(serviceName: String, qrImage: Bitmap, iconImage: Bitmap): Boolean {
        // if there are same id in the shared preference, overwrite the QRCodes
        val id = UUID.randomUUID().toString()
        val qrCodeUrl = "$externalStorageRootDirectory/$id.png"
        val serviceIconUrl = "$externalStorageRootDirectory/${id}_icon.png"
        val qrCode = QRCode.User(id, qrCodeUrl, serviceName, serviceIconUrl)
        val qrCodes = getQRCodes().filter { it.id != qrCode.id } + listOf(qrCode)
        val editor: SharedPreferences.Editor = sharedPreferences.edit() ?: return false
        editor.putString(PREF_NAME, qrCodeListAdapter.toJson(qrCodes.toList()))
        editor.apply()
        return saveImage(qrImage, qrCode.qrCodeUrl) && saveImage(iconImage, qrCode.serviceIconUrl)
    }

    companion object {
        private var INSTANCE: QRCodeLocalDataSource? = null
        private const val PREF_NAME = "QRCodesJson"

        @JvmStatic fun getInstance(sharedPreferences: SharedPreferences) =
            INSTANCE ?: synchronized(QRCodeRepository::class.java) {
                INSTANCE ?: QRCodeLocalDataSource(sharedPreferences = sharedPreferences)
                    .also { INSTANCE = it }
            }

        @JvmStatic fun destroyInstance() {
            INSTANCE = null
        }
   }
}