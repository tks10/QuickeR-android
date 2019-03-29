package com.qrist.quicker.utils

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        context = this
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }

    companion object {
        private var context: MyApplication? = null
        private var firebaseAnalytics: FirebaseAnalytics? = null

        val instance: MyApplication
            get() = context!!
        val analytics: FirebaseAnalytics
            get() = firebaseAnalytics!!
    }
}
