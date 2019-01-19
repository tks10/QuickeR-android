package com.qrist.quicker.utils

import android.content.Context
import com.qrist.quicker.data.QRCodeLocalDataSource
import com.qrist.quicker.data.QRCodeRepository

object Injection {

    fun provideQRCodeRepository(context: Context): QRCodeRepository {
        return QRCodeRepository.getInstance(
            QRCodeLocalDataSource.getInstance(
                (context.applicationContext.getSharedPreferences("DataSave", Context.MODE_PRIVATE))
            )
        )
    }
}