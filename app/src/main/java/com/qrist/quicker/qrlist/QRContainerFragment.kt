package com.qrist.quicker.qrlist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.google.zxing.integration.android.IntentIntegrator
import com.qrist.quicker.R
import kotlinx.android.synthetic.main.fragment_qrcontainer.view.*
import com.journeyapps.barcodescanner.CaptureActivity


class QRContainerFragment : Fragment() {
    val RESULT_PICK_QRCODE: Int = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_qrcontainer, container, false)
        val pagerAdapter = QRViewFragmentPagerAdapter(activity!!.supportFragmentManager)

        view.viewPager.offscreenPageLimit = 2
        view.viewPager.adapter = pagerAdapter

        view.tool_bar.inflateMenu(R.menu.menu)
        view.tool_bar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_capture -> {
                    val intentIntegrator = IntentIntegrator.forSupportFragment(this).apply {
                        setPrompt("Scan a QR code")
                        captureActivity = CaptureActivityPortrait::class.java
                    }
                    intentIntegrator.initiateScan()

                    Log.d("Menu", "Capture was tapped.")
                    true
                }
                R.id.menu_settings -> {
                    Log.d("Menu", "Settings was tapped.")
                    true
                }
                else -> {
                    false
                }
            }
        }

        view.floatingActionButton.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_qr_container_to_register)
        }

        view.tabLayout.setupWithViewPager(view.viewPager)

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RESULT_PICK_QRCODE -> {
                    val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, resultData)
                    Log.d("QR result", result.contents)
                }
            }
        }
    }
}

class CaptureActivityPortrait : CaptureActivity()
