package com.qrist.quicker.extentions

import android.Manifest
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import com.qrist.quicker.utils.ViewModelFactory
import java.io.File

fun <T : ViewModel> Fragment.obtainViewModel(viewModelClass: Class<T>) =
    ViewModelProviders.of(this, ViewModelFactory.getInstance(activity?.application!!)).get(viewModelClass)

@Throws(SecurityException::class)
fun Fragment.makeAppDirectory(directory: File): Boolean =
    when (checkPermission()) {
        true -> {
            if (!directory.exists())
                directory.mkdir()
            true
        }
        false -> false
    }

fun Fragment.checkPermission(): Boolean =
    ActivityCompat.checkSelfPermission(
        this.context!!,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

