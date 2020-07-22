package com.qrist.quicker.qrlist

import android.app.Application
import com.qrist.quicker.InstantExecutorExtension
import com.qrist.quicker.data.QRCodeRepository
import com.qrist.quicker.models.QRCode
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class)
internal class QRContainerViewModelTest {

    private lateinit var mockedRepo: QRCodeRepository
    private lateinit var mockedApp: Application
    private lateinit var viewModel: QRContainerViewModel

    @BeforeEach
    fun beforeEacH() {
        mockedRepo = mockk(relaxed = true)
        mockedApp = mockk(relaxed = true)
        viewModel = QRContainerViewModel(mockedApp, mockedRepo)
    }

    @DisplayName("サービス名の視認性テスト")
    @Test
    fun fetchServiceNameVisibilityTest() {
        // Arrange

        // Act
        viewModel.switchServiceNameVisibility()

        // Assert
        verify { mockedRepo.isShowServiceNameInQRView() }
        verify { viewModel.fetchServiceNameVisibility() }
        assertEquals(false, viewModel.isShowServiceName.value)
    }

    @DisplayName("QRコードのフェッチテスト")
    @Test
    fun fetchQRCodeTest() {
        // Arrange
        val list = listOf(QRCode.User(
                "1",
                "https://hoge.com",
                "test",
                "test"
            ))
        every { mockedRepo.getQRCodes() } returns list

        // Act
        viewModel.fetchQRCodes()

        // Assert
        verify { mockedRepo.getQRCodes() }
        assertEquals(list, viewModel.qrCodes)
    }
}