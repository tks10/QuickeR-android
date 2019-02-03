package com.qrist.quicker.utils

import android.net.Uri
import android.os.Environment
import com.qrist.quicker.BuildConfig
import com.qrist.quicker.models.QRCode
import java.io.File
import java.io.FileNotFoundException

private val externalStorageRootDirectory: String = Environment.getExternalStorageDirectory().absolutePath

val storeDirectory: String = "$externalStorageRootDirectory/${BuildConfig.APPLICATION_STORE_DIRECTORY}"

fun validateExistence(code: QRCode): Boolean {
    try {
        val qrCodeUrl = Uri.parse(code.qrCodeUrl)
        MyApplication.instance.contentResolver.openInputStream(qrCodeUrl)
    } catch (e: FileNotFoundException) {
        e.printStackTrace()

        return false
    }

    if (code is QRCode.User) {
        try {
            val iconUrl = Uri.fromFile(File(code.serviceIconUrl))
            MyApplication.instance.contentResolver.openInputStream(iconUrl)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()

            return false
        }
    }

    return true
}
