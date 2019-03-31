package com.qrist.quicker.utils

import android.content.ContentResolver
import android.content.Context
import android.databinding.BindingAdapter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v4.content.FileProvider
import android.util.Log
import android.widget.ImageView
import java.io.*
import kotlin.math.ceil
import kotlin.math.max

const val IMAGE_QR_MAX = 640f
const val IMAGE_ICON_MAX = 192f

fun saveImage(bitmap: Bitmap, imageUrl: String, clipTo: Float): Boolean =
    try {
        val maxSize = max(bitmap.width, bitmap.height)

        val resizedBitmap = if (maxSize > clipTo) {
            val resizeRate = maxSize.toFloat() / clipTo
            val width = ceil(bitmap.width / resizeRate).toInt()
            val height = ceil(bitmap.height / resizeRate).toInt()
            Bitmap.createScaledBitmap(bitmap, width, height, true)
        } else {
            bitmap
        }

        val outputStream = FileOutputStream(imageUrl)
        resizedBitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
        outputStream.flush()
        outputStream.close()
        true
    } catch (exception: IOException) {
        exception.printStackTrace()
        false
    }

@BindingAdapter("app:imageUrl")
fun imageUrl(imageView: ImageView, url: String?) {
    if (url.isNullOrBlank()) {
        Log.e("Glide Binding", "URL is empty")
        return
    }
    GlideApp.with(imageView.context)
        .load(url)
        .fitCenter()
        .into(imageView)
}

@BindingAdapter("app:srcCompat")
fun srcCompat(view: ImageView, resourceId: Int) {
    view.setImageResource(resourceId)
}

fun convertUrlFromDrawableResId(context: Context, drawableResId: Int): String {
    val sb = StringBuilder()
    sb.append(ContentResolver.SCHEME_ANDROID_RESOURCE)
    sb.append("://")
    sb.append(context.resources.getResourcePackageName(drawableResId))
    sb.append("/")
    sb.append(context.resources.getResourceTypeName(drawableResId))
    sb.append("/")
    sb.append(context.resources.getResourceEntryName(drawableResId))
    return sb.toString()
}

fun getDrawableFromUri(uri: Uri): Drawable {
    val inputStream = MyApplication.instance.contentResolver.openInputStream(uri)
    return Drawable.createFromStream(inputStream, uri.toString())
}

fun getBitmapFromUri(context: Context, uri: Uri): Bitmap {
    Log.d("ImageUtil", uri.toString())
    val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")?.fileDescriptor
    return BitmapFactory.decodeFileDescriptor(fileDescriptor).copy(Bitmap.Config.ARGB_8888, true)
}

fun getEmptyImage(width: Int = 32, height: Int = 32): Bitmap
        = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
