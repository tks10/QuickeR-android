package com.qrist.quicker.extentions

import android.databinding.BindingAdapter
import android.view.View

var View.isVisible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.INVISIBLE
    }

var View.isInvisible: Boolean
    get() = visibility == View.INVISIBLE
    set(value) {
        visibility = if (value) View.INVISIBLE else View.VISIBLE
    }

var View.isGone: Boolean
    get() = visibility == View.GONE
    set(value) {
        visibility = if (value) View.GONE else View.VISIBLE
    }

@BindingAdapter("android:isVisible")
fun View.setIsVisible(value: Boolean) {
    isVisible = value
}

@BindingAdapter("android:isGone")
fun View.setIsGone(value: Boolean) {
    isGone = value
}