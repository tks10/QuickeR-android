package com.qrist.quicker.qrlist

import android.app.Application
import com.qrist.quicker.InstantExecutorExtension
import com.qrist.quicker.data.QRCodeRepository
import com.qrist.quicker.models.QRCode
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class)
internal class QRViewViewModelTest {

    private lateinit var mockedRepo: QRCodeRepository
    private lateinit var mockedApp: Application
    private lateinit var viewModel: QRViewViewModel

    @BeforeEach
    fun beforeEach() {
        mockedRepo = mockk<QRCodeRepository>(relaxed = true)
        mockedApp = mockk<Application>(relaxed = true)
        viewModel = QRViewViewModel(mockedApp, mockedRepo)
    }

    @DisplayName("QRコードフェッチテスト")
    @Test
    fun fetchImageUrlTest() {
        // Arrange
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

    @DisplayName("バックグランド色取得テスト")
    @Test
    fun getBackgroundColorTest() {

    }
}