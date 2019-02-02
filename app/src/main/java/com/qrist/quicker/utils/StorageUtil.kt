package com.qrist.quicker.utils

import android.os.Environment
import com.qrist.quicker.BuildConfig

private val externalStorageRootDirectory: String = Environment.getExternalStorageDirectory().absolutePath

val storeDirectory: String = "$externalStorageRootDirectory/${BuildConfig.APPLICATION_STORE_DIRECTORY}"
