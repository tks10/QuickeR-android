package com.qrist.quicker.data

import android.content.SharedPreferences
import android.graphics.Bitmap
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.utils.saveImage
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

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