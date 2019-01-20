package com.qrist.quicker.extentions

import android.Manifest
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import com.qrist.quicker.utils.ViewModelFactory
import java.io.File
import java.io.FileDescriptor
import java.io.IOException

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

object IntentActionType {
    const val RESULT_PICK_QRCODE: Int = 1001
    const val RESULT_PICK_SERVICE_ICON: Int = 1002
}

fun Fragment.onClickImagePicker(actionType: Int) {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)

    // Filter to only show results that can be "opened", such as a
    // file (as opposed to a list of contacts or timezones)
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    intent.type = "image/*"

    startActivityForResult(intent, actionType)
}

// image added by user will notice the fragment by these member imageUri.
// The ACTION_OPEN_DOCUMENT intent was sent with the request code
// READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
// response to some other intent, and the code below shouldn't run at all.
@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "NAME_SHADOWING")
fun Fragment.onPickImageFile(resultData: Intent?, callback: (Bitmap, Uri) -> Unit) {
    // The document selected by the user won't be returned in the intent.
    // Instead, a URI to that document will be contained in the return intent
    // provided to this method as a parameter.
    // Pull that URI using resultData.getData().
    if(resultData != null && resultData.data != null){
        var pfDescriptor: ParcelFileDescriptor? = null
        try{
            val imageUri: Uri = resultData.data
            pfDescriptor = activity!!.contentResolver.openFileDescriptor(imageUri, "r")
            if(pfDescriptor != null){
                val fileDescriptor: FileDescriptor = pfDescriptor.fileDescriptor
                val bmp: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
                pfDescriptor.close()
                callback(bmp, imageUri)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try{
                pfDescriptor?.close()
            }catch (e: Exception){
                e.printStackTrace()
            }
        }

    }
}