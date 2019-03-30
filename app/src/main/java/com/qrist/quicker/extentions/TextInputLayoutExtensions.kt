package com.qrist.quicker.extentions

import android.content.res.ColorStateList
import android.databinding.BindingAdapter
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import com.qrist.quicker.R
import com.qrist.quicker.utils.MyApplication

fun TextInputLayout.setTextColorHint(color: Int) {
    val states = arrayOf(intArrayOf(android.R.attr.state_enabled))
    val colors = intArrayOf(color)
    this.defaultHintTextColor = ColorStateList(states, colors)
}

@BindingAdapter("android:forceEnableHint")
fun TextInputLayout.setEnableColor(value: Boolean) {
    if (value) {
        val color = ContextCompat.getColor(MyApplication.instance, R.color.colorSecondary)
        this.setTextColorHint(color)
    }
}
