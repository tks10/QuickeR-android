package com.qrist.quicker.qrlist

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.afollestad.materialdialogs.MaterialDialog
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.CaptureActivity
import com.qrist.quicker.R
import com.qrist.quicker.extentions.checkPermission
import com.qrist.quicker.extentions.makeAppDirectory
import com.qrist.quicker.extentions.obtainViewModel
import com.qrist.quicker.utils.storeDirectory
import kotlinx.android.synthetic.main.fragment_qrcontainer.*
import kotlinx.android.synthetic.main.fragment_qrcontainer.view.*
import java.io.File

class QRContainerFragment : Fragment(), ViewPager.OnPageChangeListener {
    override fun onPageScrollStateChanged(p0: Int) {
    }

    override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
    }

    override fun onPageSelected(position: Int) {
        val nearLeftEdge: Boolean = (position <= viewModel.qrCodes.size)
        val nearRightEdge: Boolean = (position >= adapter.count - viewModel.qrCodes.size)
        if (nearLeftEdge || nearRightEdge) {
            view?.viewPager?.setCurrentItem(adapter.getCenterPosition(0), false)
        }
    }

    private val viewModel: QRContainerViewModel by lazy { obtainViewModel(QRContainerViewModel::class.java) }
    private val directory = File(storeDirectory)
    private lateinit var sequence: TapTargetSequence
    private lateinit var adapter: QRViewFragmentPagerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT >= 23 && !checkPermission()) {
            sequence = TapTargetSequence(activity)
                .targets(
                    TapTarget.forView(floatingActionButton, context!!.resources.getString(R.string.message_start))
                        .outerCircleColor(R.color.colorAccent)
                        .titleTextColor(R.color.colorTextOnSecondary)
                        .drawShadow(true)
                        .outerCircleAlpha(1.0f)
                        .cancelable(false)
                        .tintTarget(false)
                        .id(1)
                )
            requestExternalStoragePermission(byFab = false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_qrcontainer, container, false)
        val toolbar: Toolbar = activity!!.findViewById(R.id.tool_bar)

        view.viewPager.offscreenPageLimit = 5
        view.viewPager.adapter = QRViewFragmentPagerAdapter(viewModel.qrCodes, childFragmentManager)

        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.menu)
        toolbar.setOnMenuItemClickListener { item ->
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
                    Navigation.findNavController(view).navigate(R.id.action_qr_container_to_registeredservicelist)
                    true
                }
                else -> {
                    false
                }
            }
        }

        view.floatingActionButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 23 && !checkPermission()) {
                requestExternalStoragePermission(byFab = true)
            } else {
                Navigation.findNavController(view).navigate(R.id.action_qr_container_to_serviceaddlist)
            }
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        updateViewPager()
    }

    private fun updateViewPager() {
        viewModel.fetchQRCodes()

        adapter = QRViewFragmentPagerAdapter(viewModel.qrCodes, childFragmentManager)
        view?.viewPager?.adapter = adapter
        view?.viewPager?.currentItem = adapter.getCenterPosition(0)
        view?.viewPager?.addOnPageChangeListener(this)

        view?.tabLayout?.setUpWithAdapter(ServiceIconAdapter(view?.viewPager!!, viewModel.qrCodes))

        val serviceCount = viewModel.qrCodes.size
        view?.getStartedTextView?.visibility = if (serviceCount == 0) View.VISIBLE else View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 49374) {
                val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, resultData)
                Log.d("QR result", result.contents)

                val url = Uri.parse(result.contents)
                val intent = Intent(Intent.ACTION_VIEW, url)

                MaterialDialog(activity!!).show {
                    title(R.string.title_open_url)
                    message(text = url.toString())
                    positiveButton(R.string.message_open_url) {
                        this@QRContainerFragment.startActivity(intent)
                    }
                }
            }
        }
    }

    private fun saveImageOnDevice() {
        makeAppDirectory(directory)
        updateViewPager()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_ON_CREATE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveImageOnDevice()
        }
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveImageOnDevice()
            when (requestCode) {
                REQUEST_PERMISSION_ON_CREATE -> sequence.start()
                REQUEST_PERMISSION_BY_FAB -> {
                    Navigation.findNavController(view!!).navigate(R.id.action_qr_container_to_serviceaddlist)
                }
            }
        }

    }

    private fun requestExternalStoragePermission(byFab: Boolean) {
        if (shouldShowRequestPermissionRationale(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            val toast = Toast.makeText(activity, R.string.accept_me, Toast.LENGTH_SHORT)
            toast.show()
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                if (byFab) REQUEST_PERMISSION_BY_FAB else REQUEST_PERMISSION_ON_CREATE
            )
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                if (byFab) REQUEST_PERMISSION_BY_FAB else REQUEST_PERMISSION_ON_CREATE
            )
        }
    }

    companion object {
        private const val REQUEST_PERMISSION_ON_CREATE: Int = 1000
        private const val REQUEST_PERMISSION_BY_FAB: Int = 1001
    }
}

class CaptureActivityPortrait : CaptureActivity()
