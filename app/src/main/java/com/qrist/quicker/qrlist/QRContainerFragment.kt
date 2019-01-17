package com.qrist.quicker.qrlist

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.models.Service
import com.qrist.quicker.R
import com.qrist.quicker.extentions.checkPermission
import com.qrist.quicker.extentions.makeAppDirectory
import com.qrist.quicker.extentions.obtainViewModel
import kotlinx.android.synthetic.main.fragment_qrcontainer.view.*
import java.io.File

class QRContainerFragment : Fragment() {

    private val pagerAdapter by lazy { QRViewFragmentPagerAdapter(viewModel.qrCodes, activity!!.supportFragmentManager) }
    private val directory = File(Environment.getExternalStorageDirectory().absolutePath + "/DCIM/QuickeR/")
    private val testCode = listOf(
        QRCode.User(
            "1",
            directory.absolutePath + "/qr_code.png",
            Service.UserService(
                "1",
                "user",
                directory.absolutePath + "/qr_code.png"
            )
        ),
        QRCode.User(
            "2",
            directory.absolutePath + "/qr_code.png",
            Service.UserService(
                "2",
                "user2",
                directory.absolutePath + "/qr_code.png"
            )
        ),
        QRCode.User(
            "3",
            directory.absolutePath + "/qr_code.png",
            Service.UserService(
                "3",
                "user3",
                directory.absolutePath + "/qr_code.png"
            )
        )
    )

    private val viewModel: QRContainerViewModel by lazy { obtainViewModel(QRContainerViewModel::class.java) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT >= 23 && !checkPermission()) requestExternalStoragePermission()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_qrcontainer, container, false)

        view.viewPager.offscreenPageLimit = 2
        view.viewPager.adapter = pagerAdapter

        view.tool_bar.inflateMenu(R.menu.menu)
        view.tool_bar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_capture -> {
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

        view.tabLayout.setupWithViewPager(view.viewPager)

        return view
    }

    private fun saveImageOnDevice() {
        makeAppDirectory(directory)
        viewModel.qrCodes = testCode
        viewModel.saveQRCodes()
        // do not change state dynamically: dynamic view change with view pager seems very hard.
        pagerAdapter.notifyDataSetChanged()
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
