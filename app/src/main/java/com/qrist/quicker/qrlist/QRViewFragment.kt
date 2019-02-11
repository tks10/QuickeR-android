package com.qrist.quicker.qrlist

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.qrist.quicker.R
import com.qrist.quicker.databinding.FragmentQrviewBinding
import com.qrist.quicker.extentions.obtainViewModel

class QRViewFragment : Fragment() {
    private val codeId by lazy { arguments!!.getString(BUNDLE_ARG_ID) }
    private var viewModel: QRViewViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = obtainViewModel(codeId, QRViewViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val binding: FragmentQrviewBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_qrview, container, false)

        binding.setLifecycleOwner(this)
        binding.viewmodel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e("create fragment", "$this $codeId $viewModel")
        viewModel?.fetchImageUrl(codeId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("destroy fragment", "$this $codeId $viewModel")
        viewModel = null
    }

    companion object {
        private const val BUNDLE_ARG_ID = "id"

        private var INSTANCES: List<QRViewFragment> = listOf()

        fun newInstance(qrCodeId: String): QRViewFragment =
            //INSTANCES.findLast {
            //    it.codeId == qrCodeId
            //} ?:
            QRViewFragment().apply {
                arguments = Bundle().apply {
                    putString(BUNDLE_ARG_ID, qrCodeId)
                }
            }.also {
                INSTANCES = INSTANCES + it
            }
    }
}
