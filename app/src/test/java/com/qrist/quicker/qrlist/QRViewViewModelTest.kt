package com.qrist.quicker.qrlist

import android.app.Application
import com.qrist.quicker.InstantExecutorExtension
import com.qrist.quicker.data.QRCodeRepository
import com.qrist.quicker.models.QRCode
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class)
internal class QRViewViewModelTest {

    @DisplayName("QRコードの画像URLのフェッチテスト")
    @Test
    fun fetchImageUrlTest() {
        // Arrange
        val mockedRepo = mockk<QRCodeRepository>(relaxed = true)
        val mockedApp = mockk<Application>(relaxed = true)
        val viewModel = QRViewViewModel(mockedApp, mockedRepo)
        every { mockedRepo.getQRCode("test") } returns
                QRCode.User(
                    "1",
                    "https://hoge.com",
                    "test",
                    "test")

        // Act
        viewModel.fetchImageUrl("test")

        // Assert
        verify { mockedRepo.getQRCode("test") }
        assertEquals("https://hoge.com", viewModel.qrCodeImage.value)
        assertEquals("test", viewModel.iconImage.value)
        assertEquals(-1, viewModel.serviceId.value)
        assertEquals("test", viewModel.serviceName.value)
    }
}