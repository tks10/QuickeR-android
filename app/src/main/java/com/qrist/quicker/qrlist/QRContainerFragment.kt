package com.qrist.quicker.qrlist

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
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
import com.qrist.quicker.R
import com.qrist.quicker.extentions.obtainViewModel
import com.qrist.quicker.models.TutorialComponent

import kotlinx.android.synthetic.main.fragment_qrcontainer.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class QRContainerFragment : Fragment(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private val viewModel: QRContainerViewModel by lazy { obtainViewModel(QRContainerViewModel::class.java) }
    private lateinit var adapter: QRViewFragmentPagerAdapter
    private lateinit var sequence: TapTargetSequence

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_qrcontainer, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        floatingActionButton.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_qr_container_to_serviceaddlist)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.apply {
            tool_bar.menu.clear()
            tool_bar.inflateMenu(R.menu.menu)
            tool_bar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_capture -> {
                        val intentIntegrator = IntentIntegrator.forSupportFragment(this@QRContainerFragment).apply {
                            setPrompt("Scan a QR code")
                            captureActivity = CaptureActivityPortrait::class.java
                        }
                        intentIntegrator.initiateScan()

                        Log.d("Menu", "Capture was tapped.")
                        true
                    }
                    R.id.menu_settings -> {
                        Log.d("Menu", "Settings was tapped.")
                        Navigation.findNavController(view!!).navigate(R.id.action_qr_container_to_registeredservicelist)
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateViewPager()
        tutorial()
    }

    override fun onPause() {
        super.onPause()
        Log.e("container fragment", "pause")
    }

    private fun tutorial() {
        if (viewModel.hasNotDoneTutorial(TutorialComponent.AddServiceButton)) {
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
            sequence.start()
            viewModel.doneTutorial(TutorialComponent.AddServiceButton)
        }
    }

    inner class MyOnPageChangeListener : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            viewModel.currentAdapterPosition = adapter.getAdapterPosition(position)
        }

        override fun onPageSelected(position: Int) {
            val nearLeftEdge: Boolean = (position <= viewModel.qrCodes.size)
            val nearRightEdge: Boolean = (position >= adapter.count - viewModel.qrCodes.size)
            val currentAdapterPosition = viewModel.currentAdapterPosition

            if (nearLeftEdge || nearRightEdge) {
                viewPager.setCurrentItem(adapter.getCenterPosition(currentAdapterPosition), false)
            }
        }
    }

    private fun updateViewPager() {
        viewModel.fetchQRCodes()

        // Fix current adapter position to prevent it is changed by MyOnPageChangeListener#onPageScrolled
        val currentAdapterPosition = viewModel.currentAdapterPosition

        adapter = QRViewFragmentPagerAdapter.getInstance(viewModel.qrCodes, childFragmentManager)
        adapter.detachItems()
        viewPager.visibility = View.INVISIBLE
        tabLayout.visibility = View.INVISIBLE
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 0
        viewPager.addOnPageChangeListener(MyOnPageChangeListener())
        viewPager.currentItem = adapter.getCenterPosition(currentAdapterPosition - 2)

        this.launch {
            delay(WAIT_TIME_ON_LAUNCH)
            viewPager.setCurrentItem(adapter.getCenterPosition(currentAdapterPosition), false)
            viewPager.visibility = View.VISIBLE
            tabLayout.visibility = View.VISIBLE
        }

        tabLayout.setUpWithAdapter(ServiceIconAdapter(viewPager, viewModel.qrCodes))

        val serviceCount = viewModel.qrCodes.size
        getStartedTextView.visibility = if (serviceCount == 0) View.VISIBLE else View.GONE
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

    companion object {
        private const val WAIT_TIME_ON_LAUNCH: Long = 5
    }
}

class CaptureActivityPortrait : CaptureActivity()
