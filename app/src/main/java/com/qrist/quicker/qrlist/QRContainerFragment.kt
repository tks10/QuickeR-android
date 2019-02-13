package com.qrist.quicker.qrlist

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.navigation.Navigation
import com.afollestad.materialdialogs.MaterialDialog
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.CaptureActivity
import com.nshmura.recyclertablayout.RecyclerTabLayout
import com.qrist.quicker.R
import com.qrist.quicker.extentions.obtainViewModel
import com.qrist.quicker.utils.storeDirectory
import kotlinx.android.synthetic.main.fragment_qrcontainer.*
import kotlinx.android.synthetic.main.fragment_qrcontainer.view.*
import java.io.File

class QRContainerFragment : Fragment() {

    private val viewModel: QRContainerViewModel by lazy { obtainViewModel(QRContainerViewModel::class.java) }
    private val directory = File(storeDirectory)
    private lateinit var adapter: QRViewFragmentPagerAdapter
    private lateinit var sequence: TapTargetSequence

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_qrcontainer, container, false)
        val toolbar: Toolbar = activity!!.findViewById(R.id.tool_bar)

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
            Navigation.findNavController(view).navigate(R.id.action_qr_container_to_serviceaddlist)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        updateViewPager(view!!)
        sequence.start()
    }

    override fun onPause() {
        super.onPause()
        Log.e("container fragment", "pause")
        adapter.detachItems()
    }

    private fun updateViewPager(view: View) {
        viewModel.fetchQRCodes()

        adapter = QRViewFragmentPagerAdapter.getInstance(viewModel.qrCodes, childFragmentManager)
        view.viewPager.adapter = adapter
        view.viewPager.offscreenPageLimit = 0
        view.viewPager.currentItem = adapter.getCenterPosition(0)

        view.tabLayout.setUpWithAdapter(ServiceIconAdapter(view.viewPager, viewModel.qrCodes))
        (view.tabLayout.adapter as RecyclerTabLayout.Adapter).currentIndicatorPosition = adapter.getCenterPosition(0)

        val serviceCount = viewModel.qrCodes.size
        view.getStartedTextView.visibility = if (serviceCount == 0) View.VISIBLE else View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 49374) {
                val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, resultData)
                Log.d("QR result", result.contents)

                scannedQRCode(result.contents)
            }
        }
    }

    private fun scannedQRCode(resultText: String) {
        when (URLUtil.isValidUrl(resultText)) {
            true -> {
                val uri = Uri.parse(resultText)
                val intent = Intent(Intent.ACTION_VIEW, uri)

                MaterialDialog(activity!!).show {
                    title(R.string.title_open_url)
                    message(text = uri.toString())
                    positiveButton(R.string.message_open_url) {
                        this@QRContainerFragment.startActivity(intent)
                    }
                    negativeButton(R.string.cancel)
                }
            }
            false -> {
                MaterialDialog(activity!!).show {
                    title(R.string.title_result_non_url)
                    message(text = resultText)
                    positiveButton(R.string.message_copy) {
                        val clipboardManager: ClipboardManager =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboardManager.primaryClip = ClipData.newPlainText("", resultText)
                    }
                    negativeButton(R.string.message_close)
                }
            }
        }
    }
}

class CaptureActivityPortrait : CaptureActivity()
