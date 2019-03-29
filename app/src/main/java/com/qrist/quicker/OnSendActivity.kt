package com.qrist.quicker

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController

class OnSendActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        findViewById<Toolbar>(R.id.tool_bar).setupWithNavController(navController, appBarConfiguration)

        // Handle the intent only when savedInstanceState is not null to avoid handling recreating activity.
        if (savedInstanceState == null) {
            handleIntentOnSend(intent)
        }
    }

    override fun onNewIntent(newIntent: Intent?) {
        super.onNewIntent(newIntent)
        handleIntentOnSend(newIntent)
    }

    private fun handleIntentOnSend(receivedIntent: Intent?) {
        // Check whether or not intent is from sharing.
        // In any case, pop all stacks and start from ServiceAddListFragment.
        receivedIntent?.extras?.get(INTENT_BUNDLE_KEY)?.let {
            Log.d("Intent", "Intent from other service:, value: $it")

            val action =
                NavigationGraphDirections.actionGlobalServiceaddlist(it.toString())
            findNavController(R.id.nav_host_fragment).navigate(action)
        }
    }

    companion object {
        const val INTENT_BUNDLE_KEY = "android.intent.extra.STREAM"
    }
}
