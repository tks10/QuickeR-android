package com.qrist.quicker

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
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


        handleAndConsumeIntentOnSend()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("Intent", "onNewIntent")
        handleAndConsumeIntentOnSend(receivedIntent = intent)
    }

    private fun handleAndConsumeIntentOnSend(receivedIntent: Intent? = null) {
        // Get extras if intent is given by the argument, otherwise get from activity.intent
        val intent = receivedIntent ?: this.intent
        Log.d("Intent", receivedIntent.toString() + this.intent.toString())
        val extras = intent.extras

        // Check whether or not intent is from sharing.
        extras?.get(INTENT_BUNDLE_KEY)?.let {
            Log.d("Intent", "Intent from other service:, value: $it")

            // Clear Intent
            this@OnSendActivity.intent = null

            val action =
                QRContainerFragmentDirections.actionQrContainerToServiceaddlist(it.toString())
            Navigation.findNavController(this, R.id.nav_host_fragment).popBackStack(R.id.qrContainerFragment, false)
            Navigation.findNavController(this, R.id.nav_host_fragment).navigate(action)
        }
    }

    companion object {
        const val INTENT_BUNDLE_KEY = "android.intent.extra.STREAM"
    }
}
