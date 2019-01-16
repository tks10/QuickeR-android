package com.qrist.quicker.qrlist

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.qrist.quicker.R
import com.qrist.quicker.extentions.obtainViewModel
import kotlinx.android.synthetic.main.fragment_qrcontainer.view.*

class QRContainerFragment : Fragment() {

    private val viewModel: QRContainerViewModel by lazy { obtainViewModel(QRContainerViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_qrcontainer, container, false)
        val pagerAdapter = QRViewFragmentPagerAdapter(viewModel.getQRCodes(), activity!!.supportFragmentManager)

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
}
