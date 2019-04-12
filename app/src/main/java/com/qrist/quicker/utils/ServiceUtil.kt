package com.qrist.quicker.utils

import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import com.qrist.quicker.R
import com.qrist.quicker.models.QRCode

fun serviceIdToIconDrawableResId(serviceId: Int): Int {
    return when (serviceId) {
        QRCode.Default.TWITTER_SERVICE_ID -> R.drawable.twitter_logo
        QRCode.Default.FACEBOOK_SERVICE_ID -> R.drawable.facebook_logo
        QRCode.Default.LINE_SERVICE_ID -> R.drawable.line_logo
        else -> R.drawable.ic_error_24dp
    }
}

fun serviceIdToColorDrawable(serviceId: Int): ColorDrawable {
    val colorId = when (serviceId) {
        QRCode.Default.TWITTER_SERVICE_ID -> R.color.twitter
        QRCode.Default.FACEBOOK_SERVICE_ID -> R.color.facebook
        QRCode.Default.LINE_SERVICE_ID -> R.color.line
        else -> R.color.etc
    }
    return ColorDrawable(ContextCompat.getColor(MyApplication.instance, colorId))
}

fun serviceIdToServiceName(serviceId: Int): String {
    return when (serviceId) {
        QRCode.Default.TWITTER_SERVICE_ID -> "Twitter"
        QRCode.Default.FACEBOOK_SERVICE_ID -> "Facebook"
        QRCode.Default.LINE_SERVICE_ID -> "LINE"
        else -> "Unknown Service"
    }
}

fun serviceIdToIconUrl(serviceId: Int): String {
    return convertUrlFromDrawableResId(MyApplication.instance, serviceIdToIconDrawableResId(serviceId))
}

fun serviceNameToServiceId(serviceName: String): Int {
    QRCode.Default.DEFAULT_SERVICES_ID.forEach { id ->
        if (serviceIdToServiceName(id) == serviceName) return id
    }

    return -1
}
