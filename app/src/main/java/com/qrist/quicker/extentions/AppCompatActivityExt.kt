package com.qrist.quicker.extentions

import android.Manifest
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import java.io.File

@Throws(SecurityException::class)
fun AppCompatActivity.makeAppDirectory(directory: File): Boolean =
    when (checkPermission()) {
        true -> {
            if (!directory.exists())
                directory.mkdir()
            true
        }
        false -> false
    }

fun AppCompatActivity.checkPermission(): Boolean =
    ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

