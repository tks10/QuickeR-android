package com.qrist.quicker

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.widget.Toast
import com.qrist.quicker.extentions.checkPermission
import com.qrist.quicker.extentions.makeAppDirectory
import com.qrist.quicker.utils.ImageUtil
import java.io.File

class MainActivity : AppCompatActivity() {

    private val directory = File(Environment.getExternalStorageDirectory().absolutePath + "/DCIM/QuickeR/")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= 23 && !checkPermission()) requestExternalStoragePermission(this)
        else saveImageOnDevice()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            saveImageOnDevice()
    }

    private fun saveImageOnDevice() {
        val bitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.qr_code)
        makeAppDirectory(directory)
        ImageUtil.saveImage(bitmap, directory.absolutePath + "/qr_code.png")
    }


    private fun requestExternalStoragePermission(activity: AppCompatActivity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )) {
            val toast = Toast.makeText(activity, R.string.accept_me, Toast.LENGTH_SHORT)
            toast.show()
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSION
            )
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSION
            )
        }
    }

    companion object {
        private const val REQUEST_PERMISSION: Int = 1000
    }
}
