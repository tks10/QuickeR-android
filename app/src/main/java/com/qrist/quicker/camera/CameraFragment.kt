package com.qrist.quicker.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.Toast
import androidx.navigation.Navigation

import com.qrist.quicker.R
import com.qrist.quicker.extentions.checkCameraPermission
import com.qrist.quicker.extentions.requestPermission
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_camera.*

class CameraFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_camera, container, false)

    @SuppressLint("ShowToast")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraPreview.qrCodeCallback = { value ->
            activity?.runOnUiThread {
                Toast.makeText(context, "$value", Toast.LENGTH_LONG)
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
        if (!checkCameraPermission()) {
            requestPermission(Manifest.permission.CAMERA, REQUEST_PERMISSION_ON_CAMERA)
        } else {
            cameraPreview.startCameraPreview()
        }
    }

    override fun onPause() {
        super.onPause()
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
