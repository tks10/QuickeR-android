package com.qrist.quicker.qrlist

import android.app.Activity
import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.qrist.quicker.models.QRCode
import androidx.navigation.Navigation
import com.google.zxing.integration.android.IntentIntegrator
import com.qrist.quicker.R
import com.qrist.quicker.extentions.checkPermission
import com.qrist.quicker.extentions.makeAppDirectory
import com.qrist.quicker.extentions.obtainViewModel
import kotlinx.android.synthetic.main.fragment_qrcontainer.view.*
import com.journeyapps.barcodescanner.CaptureActivity
import com.qrist.quicker.utils.storeDirectory

import java.io.File

class QRContainerFragment : Fragment() {
    val RESULT_PICK_QRCODE: Int = 1001

    private val viewModel: QRContainerViewModel by lazy { obtainViewModel(QRContainerViewModel::class.java) }
    private val directory = File(storeDirectory)
    private val testCode = listOf(
        QRCode.Default(
            "0",
            directory.absolutePath + "/qr_code.png",
            QRCode.Default.TWITTER_SERVICE_ID
        ),
        QRCode.Default(
            "2",
            directory.absolutePath + "/qr_code.png",
            QRCode.Default.LINE_SERVICE_ID
        ),
        QRCode.User(
            "3",
            directory.absolutePath + "/qr_code.png",
            "user",
            directory.absolutePath + "/qr_code.png"
        ),
        QRCode.User(
            "4",
            directory.absolutePath + "/qr_code.png",
            "user2",
            directory.absolutePath + "/qr_code.png"
        ),
        QRCode.User(
            "5",
            directory.absolutePath + "/qr_code.png",
            "user3",
            directory.absolutePath + "/qr_code.png"
        )
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT >= 23 && !checkPermission()) requestExternalStoragePermission()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_qrcontainer, container, false)

        view.viewPager.offscreenPageLimit = 2
        view.viewPager.adapter = QRViewFragmentPagerAdapter(viewModel.qrCodes, childFragmentManager)

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
            Navigation.findNavController(view).navigate(R.id.action_qr_container_to_servicelist)
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

    private fun saveImageOnDevice() {
        makeAppDirectory(directory)
        viewModel.qrCodes = testCode
        viewModel.saveQRCodes()
        viewModel.getQRCodes()
        view?.viewPager?.adapter = QRViewFragmentPagerAdapter(viewModel.qrCodes, childFragmentManager)
        view?.viewPager?.adapter?.notifyDataSetChanged()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            saveImageOnDevice()
    }

    private fun requestExternalStoragePermission() {
        if (shouldShowRequestPermissionRationale(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )) {
            val toast = Toast.makeText(activity, R.string.accept_me, Toast.LENGTH_SHORT)
            toast.show()
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSION
            )
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSION
            )
        }
    }

    companion object {
        private const val REQUEST_PERMISSION: Int = 1000
    }
}

class CaptureActivityPortrait : CaptureActivity()
