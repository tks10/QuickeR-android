package com.qrist.quicker.utils

import android.content.ContentResolver
import android.content.Context
import android.databinding.BindingAdapter
import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.Glide
import java.io.FileOutputStream
import java.io.IOException

fun saveImage(bitmap: Bitmap, imageUrl: String): Boolean =
    try {
        val outputStream = FileOutputStream(imageUrl)
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
        outputStream.flush()
        outputStream.close()
        true
    } catch (exception: IOException) {
        exception.printStackTrace()
        false
    }

@BindingAdapter("app:imageUrl")
fun imageUrl(imageView: ImageView, url: String) {
    Glide.with(imageView.context).load(url).into(imageView)
}

@BindingAdapter("app:srcCompat")
fun srcCompat(view: ImageView, resourceId: Int) {
    view.setImageResource(resourceId)
}

fun convertUrlFromDrawableResId(context: Context, drawableResId: Int): String {
    val sb = StringBuilder()
    sb.append(ContentResolver.SCHEME_ANDROID_RESOURCE)
    sb.append("://");
    sb.append(context.resources.getResourcePackageName(drawableResId))
    sb.append("/");
    sb.append(context.resources.getResourceTypeName(drawableResId))
    sb.append("/");
    sb.append(context.resources.getResourceEntryName(drawableResId))
    return sb.toString()
}
