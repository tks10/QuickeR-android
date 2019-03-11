package com.qrist.quicker.camera

import com.qrist.quicker.camera.widget.QRCodeValue
import org.junit.Test

class QRCodeValueTest {
    @Test
    fun testValue() {
        val httpValue = QRCodeValue.create("http://test.com")
        val httpsValue = QRCodeValue.create("https://test.com")
        val deepLinkValue = QRCodeValue.create("hello://test.com")
        val deepLinkValue2 = QRCodeValue.create("httpsss://test.com")
        val notUrlValue = QRCodeValue.create("hello")

        assert(httpValue is QRCodeValue.URLValue)
        assert(httpsValue is QRCodeValue.URLValue)
        assert(deepLinkValue is QRCodeValue.DeepLinkValue)
        assert(notUrlValue is QRCodeValue.RawValue)
    }
}