package com.qrist.quicker

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.qrist.quicker.qrlist.QRContainerFragmentDirections

class OnSendActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        findViewById<Toolbar>(R.id.tool_bar)
            .setupWithNavController(navController, appBarConfiguration)

        // Handle the intent only when savedInstanceState is not null to avoid handling recreating activity.
        // The activity is must be called by sharing if savedInstanceState is null.
        if (savedInstanceState == null) {
            handleIntentOnSend()
        }
    }

    private fun handleIntentOnSend() {
        val extras = intent.extras

        // Check whether or not intent is from sharing.
        extras?.get(INTENT_BUNDLE_KEY)?.let {
            Log.d("Intent", "Intent from other service:, value: $it")

            val action =
                QRContainerFragmentDirections.actionQrContainerToServiceaddlist(it.toString())
            Navigation.findNavController(this, R.id.nav_host_fragment).navigate(
                action,
                NavOptions.Builder().setPopUpTo(R.id.qrContainerFragment, true).build()
            )
        }
    }

    companion object {
        const val INTENT_BUNDLE_KEY = "android.intent.extra.STREAM"
    }
}
