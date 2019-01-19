package com.qrist.quicker.models

import com.squareup.moshi.JsonClass
import java.lang.IllegalStateException

sealed class QRCode(
    open val id: String,
    open val qrCodeUrl: String
) {

    @JsonClass(generateAdapter = true)
    data class Default(
        override val id: String,
        override val qrCodeUrl: String,
        val serviceId: Int
    ) : QRCode(id, qrCodeUrl) {

        val serviceName: String? = when(serviceId) {
            TWITTER_SERVICE_ID -> "Twitter"
            FACEBOOK_SERVICE_ID -> "Facebook"
            LINE_SERVICE_ID -> "Line"
            else -> {
                IllegalStateException("Service id does not fit.")
                null
            }
        }

        companion object {
            private const val TWITTER_SERVICE_ID = 0
            private const val FACEBOOK_SERVICE_ID = 1
            private const val LINE_SERVICE_ID = 2
        }
    }

    @JsonClass(generateAdapter = true)
    data class User(
        override val id: String,
        override val qrCodeUrl: String,
        val serviceName: String,
        val serviceIconUrl: String
    ) : QRCode(id, qrCodeUrl)
}

