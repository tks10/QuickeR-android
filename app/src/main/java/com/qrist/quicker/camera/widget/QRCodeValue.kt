package com.qrist.quicker.camera.widget

sealed class QRCodeValue {
    data class RawValue(
        val value: String
    ) : QRCodeValue()

    data class URLValue(
        val url: String
    ) : QRCodeValue()

    data class DeepLinkValue(
        val deepLink: String
    ) : QRCodeValue()

    companion object {
        private val urlRegex: Regex = "^https?://.*".toRegex()
        private val deepLinkRegex: Regex = "^(?!https?).*://.*".toRegex()

        fun create(value: String) =
            when {
                value.matches(urlRegex) -> URLValue(value)
                value.matches(deepLinkRegex) -> DeepLinkValue(value)
                else -> RawValue(value)
            }
    }
}