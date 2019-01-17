package com.qrist.quicker.data

import android.content.SharedPreferences
import android.graphics.Bitmap
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.models.Service
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class QRCodeLocalDataSource(
    val sharedPreferences: SharedPreferences
) : QRCodeDataSource {

    private val qrCodeListAdapter: JsonAdapter<List<QRCode>> = Moshi.Builder()
        .add(PolymorphicJsonAdapterFactory.of(Service::class.java, "Service")
            .withSubtype(Service.TwitterService::class.java, "Service.TwitterService")
            .withSubtype(Service.FacebookService::class.java, "Service.FacebookService")
            .withSubtype(Service.UserService::class.java, "Service.UserService"))
        .add(PolymorphicJsonAdapterFactory.of(QRCode::class.java, "QRCode")
            .withSubtype(QRCode.Default::class.java, "QRCode.Define")
            .withSubtype(QRCode.User::class.java, "QRCode.User"))
        .build().adapter(Types.newParameterizedType(List::class.java, QRCode::class.java))

    private val qrCodeAdapter: JsonAdapter<QRCode> = Moshi.Builder()
        .add(PolymorphicJsonAdapterFactory.of(Service::class.java, "Service")
            .withSubtype(Service.TwitterService::class.java, "Service.TwitterService")
            .withSubtype(Service.FacebookService::class.java, "Service.FacebookService")
            .withSubtype(Service.UserService::class.java, "Service.UserService"))
        .add(PolymorphicJsonAdapterFactory.of(QRCode::class.java, "QRCode")
            .withSubtype(QRCode.Default::class.java, "QRCode.Define")
            .withSubtype(QRCode.User::class.java, "QRCode.User"))
        .build().adapter(Types.newParameterizedType(QRCode::class.java))


    override fun getQRCodes(): List<QRCode> {
        val json: String? = sharedPreferences.getString(PREF_NAME, "{}")
        return qrCodeListAdapter.fromJson(
            json ?: "{}"
        ) ?: listOf(
            QRCode.Error(
                message = "Json parse error"
            )
        )
    }

    override fun getQRCode(id: String): QRCode {
        val qrCodes: List<QRCode> = this.getQRCodes()
        return qrCodes.findLast {
            it.id == id
        } ?: QRCode.Error(message = "QRCode is not found")
    }

    override fun saveQRCode(code: QRCode, image: Bitmap): Boolean {
        val json: String = sharedPreferences.getString(PREF_NAME, "{}") ?: return false
        val editor: SharedPreferences.Editor = sharedPreferences.edit() ?: return false
        editor.putString(PREF_NAME, json + qrCodeAdapter.toJson(code))
        editor.apply()
        return true
    }

    override fun saveQRCodes(codes: List<QRCode>): Boolean {
        val json: String = qrCodeListAdapter.toJson(codes)
        val editor: SharedPreferences.Editor = sharedPreferences.edit() ?: return false
        editor.putString(PREF_NAME, json)
        editor.apply()
        return true
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