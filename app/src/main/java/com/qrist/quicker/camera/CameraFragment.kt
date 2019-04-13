package com.qrist.quicker.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import androidx.navigation.Navigation
import com.afollestad.materialdialogs.MaterialDialog

import com.qrist.quicker.R
import com.qrist.quicker.camera.widget.QRCodeValue
import com.qrist.quicker.extentions.checkCameraPermission
import com.qrist.quicker.extentions.requestPermission
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_camera.*

class CameraFragment : Fragment() {
    private var isDialogSeen = false
    private var previousValue: QRCodeValue? = null
    // FIXME: install lifecycle for material dialog and delete it.
    // https://github.com/afollestad/material-dialogs/blob/ea501b80434b50d1fffffaa939db44bcd8e32563/documentation/LIFECYCLE.md
    private var dialog: MaterialDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_camera, container, false)

    private fun displayURLDialog(url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        isDialogSeen = true
        startActivity(intent)
    }

    private fun displayRawDialog(value: String) {
        dialog = MaterialDialog(context!!).show {
            title(R.string.title_result_non_url)
            message(text = value)
            positiveButton(R.string.message_copy) {
                val clipboardManager: ClipboardManager =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboardManager.primaryClip = ClipData.newPlainText("", value)
                isDialogSeen = false
                previousValue = null
                dialog = null
            }
            negativeButton(R.string.cancel) {
                isDialogSeen = false
                previousValue = null
                dialog = null
            }
            setOnCancelListener {
                isDialogSeen = false
                previousValue = null
                dialog = null
            }
        }
        isDialogSeen = true
    }

    @SuppressLint("ShowToast")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraPreview.qrCodeCallback = { value ->
            activity?.runOnUiThread {
                if (isDialogSeen) return@runOnUiThread
                if (value == previousValue) return@runOnUiThread
                previousValue = value
                when (value) {
                    is QRCodeValue.URLValue -> displayURLDialog(value.url)
                    is QRCodeValue.DeepLinkValue -> displayURLDialog(value.deepLink)
                    is QRCodeValue.RawValue -> displayRawDialog(value.value)
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.apply {
            tool_bar.menu.clear()
        }
    }

    override fun onResume() {
        super.onResume()
        isDialogSeen = false
        if (!checkCameraPermission()) {
            requestPermission(Manifest.permission.CAMERA, REQUEST_PERMISSION_ON_CAMERA)
        } else {
            cameraPreview.startCameraPreview()
        }
    }

    override fun onPause() {
        super.onPause()
        dialog?.dismiss()
        if (checkCameraPermission()) {
            cameraPreview.stopCameraPreview()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                .popBackStack(R.id.qrContainerFragment, false)
        } else {
            cameraPreview.startCameraPreview()
        }
    }

    companion object {
        private const val REQUEST_PERMISSION_ON_CAMERA = 1
    }
}
