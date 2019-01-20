package com.qrist.quicker.utils

import android.app.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        context = this
    }

    companion object {
        private var context: MyApplication? = null

        val instance: MyApplication
            get() = context!!
    }
}
