package com.qrist.quicker.utils

import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.support.annotation.VisibleForTesting
import com.qrist.quicker.data.QRCodeRepository
import com.qrist.quicker.qrlist.QRContainerViewModel
import com.qrist.quicker.qrlist.QRViewViewModel
import com.qrist.quicker.register.RegisterViewModel
import com.qrist.quicker.registeredservicelist.RegisteredServiceListViewModel
import com.qrist.quicker.serviceaddlist.ServiceAddListViewModel

class ViewModelFactory private constructor(
        private val application: Application,
        private val qrCodeRepository: QRCodeRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>) =
            with(modelClass) {
                when {
                    isAssignableFrom(QRContainerViewModel::class.java) ->
                        QRContainerViewModel(application, qrCodeRepository)
                    isAssignableFrom(QRViewViewModel::class.java) ->
                        QRViewViewModel(application, qrCodeRepository)
                    isAssignableFrom(ServiceAddListViewModel::class.java) ->
                        ServiceAddListViewModel(application, qrCodeRepository)
                    isAssignableFrom(RegisterViewModel::class.java) ->
                        RegisterViewModel(application, qrCodeRepository)
                    isAssignableFrom(RegisteredServiceListViewModel::class.java) ->
                        RegisteredServiceListViewModel(application, qrCodeRepository)
                    else ->
                        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            } as T

    companion object {

        @SuppressLint("StatisticFieldLeak")
        @Volatile private var INSTANCE: ViewModelFactory? = null

        fun getInstance(application: Application) =
                INSTANCE ?: synchronized(ViewModelFactory::class.java) {
                    INSTANCE ?: ViewModelFactory(application,
                            Injection.provideQRCodeRepository(application.applicationContext))
                            .also { INSTANCE = it }
                }

        @VisibleForTesting
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
