package com.qrist.quicker.models

import com.qrist.quicker.R
import com.qrist.quicker.utils.MyApplication
import com.qrist.quicker.utils.convertUrlFromDrawableResId
import com.qrist.quicker.utils.serviceIdToServiceName
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

        val serviceName: String =
            if (serviceId in DEFAULT_SERVICES_ID) {
                serviceIdToServiceName(serviceId)
            }
            else {
                throw IllegalStateException("Service id does not fit.")
            }

        val serviceIconUrl: String = when(serviceId) {
            TWITTER_SERVICE_ID -> convertUrlFromDrawableResId(MyApplication.instance, R.drawable.twitter_logo)
            FACEBOOK_SERVICE_ID -> convertUrlFromDrawableResId(MyApplication.instance, R.drawable.facebook_logo)
            LINE_SERVICE_ID -> convertUrlFromDrawableResId(MyApplication.instance, R.drawable.line_logo)
            else -> {
                IllegalStateException("Service id does not fit.")
                convertUrlFromDrawableResId(MyApplication.instance, R.drawable.ic_error_24dp)
            }
        }

        companion object {
            const val TWITTER_SERVICE_ID = 0
            const val FACEBOOK_SERVICE_ID = 1
            const val LINE_SERVICE_ID = 2

            val DEFAULT_SERVICES_ID = arrayListOf(
                TWITTER_SERVICE_ID,
                FACEBOOK_SERVICE_ID,
                LINE_SERVICE_ID
            )
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

