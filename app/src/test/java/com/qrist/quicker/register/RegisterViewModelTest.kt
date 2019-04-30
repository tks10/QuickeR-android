package com.qrist.quicker.register

import android.app.Application
import android.util.Log
import com.qrist.quicker.InstantExecutorExtension
import com.qrist.quicker.data.QRCodeRepository
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class)
internal class RegisterViewModelTest {

    private lateinit var mockedRepo: QRCodeRepository
    private lateinit var mockedApp: Application
    private lateinit var viewModel: RegisterViewModel

    @BeforeEach
    fun beforeEacH() {
        mockedRepo = mockk(relaxed = true)
        mockedApp = mockk(relaxed = true)
        viewModel = RegisterViewModel(mockedApp, mockedRepo)
    }

    @DisplayName("サービス情報初期化テスト")
    @Test
    fun initServiceInformationTest() {
        // Arrange

        // Act
        viewModel.initServiceInformation("test", "test_url")

        // Assert
        assertEquals("test", viewModel.serviceName.value)
        assertEquals("test_url", viewModel.serviceIconUrl.value)
        assertEquals(true, viewModel.isDefaultService.value)
    }

    @DisplayName("サービス名の更新テスト")
    @Test
    fun updateServiceNameTest() {
        // Arrange

        // Act
        viewModel.updateServiceName("test")

        // Assert
        assertEquals("test", viewModel.serviceName.value)
    }

    @DisplayName("QRコードアイコンURLの更新テスト")
    @Test
    fun updateQRCodeImageUrlTest() {
        // Arrange

        // Act
        viewModel.updateQRCodeImageUrl("test_url")

        // Assert
        assertEquals("test_url", viewModel.qrCodeImageUrl.value)
    }
}