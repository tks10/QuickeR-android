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

        val service: Service? = when(serviceId) {
            TWITTER_SERVICE_ID -> Service.TwitterService(id)
            FACEBOOK_SERVICE_ID -> Service.FacebookService(id)
            else -> {
                IllegalStateException("Service id does not fit.")
                null
            }
        }

        companion object {
            private const val TWITTER_SERVICE_ID = 0
            private const val FACEBOOK_SERVICE_ID = 1
        }
    }

    @JsonClass(generateAdapter = true)
    data class User(
        override val id: String,
        override val qrCodeUrl: String,
        val service: Service
    ) : QRCode(id, qrCodeUrl)
}
