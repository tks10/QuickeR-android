package com.qrist.quicker.qrlist

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.graphics.drawable.Drawable
import android.util.Log

class QRViewViewModel: ViewModel() {
    private val imageLiveData: MutableLiveData<Drawable> = MutableLiveData()
    private val nameLiveData: MutableLiveData<String> = MutableLiveData()

    val image: LiveData<Drawable>
        get() = imageLiveData
    val name: LiveData<String>
        get() = nameLiveData
}
