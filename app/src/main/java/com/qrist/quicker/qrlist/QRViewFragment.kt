package com.qrist.quicker.qrlist

import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.qrist.quicker.R
import com.qrist.quicker.databinding.FragmentQrviewBinding
import com.qrist.quicker.extentions.obtainViewModel
import kotlinx.android.synthetic.main.fragment_qrview.view.*

class QRViewFragment : Fragment() {
    private lateinit var codeId: String
    private var viewModel: QRViewViewModel? = null
    private var containerViewModel: QRContainerViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        codeId = arguments!!.getString(BUNDLE_ARG_ID)!!
        viewModel = obtainViewModel(codeId, QRViewViewModel::class.java)
        containerViewModel = obtainViewModel(QRContainerViewModel::class.java, requireActivity())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val binding: FragmentQrviewBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_qrview, container, false)

        binding.setLifecycleOwner(this)
        binding.viewmodel = viewModel
        binding.containerViewModel = containerViewModel

        binding.root.qrCardView.setOnClickListener {
            val changeBounds = ChangeBounds().apply { duration = 100L }
            TransitionManager.beginDelayedTransition(binding.root.qrCardView, changeBounds)
            containerViewModel?.switchServiceNameVisibility()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e("create fragment", "$this $codeId $viewModel")
        viewModel?.fetchImageUrl(codeId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.e("destroy fragment", "$this $codeId $viewModel")
        viewModel = null
        containerViewModel = null
    }

    companion object {
        private const val BUNDLE_ARG_ID = "id"

        fun newInstance(qrCodeId: String): QRViewFragment =
            QRViewFragment().apply {
                arguments = Bundle().apply {
                    putString(BUNDLE_ARG_ID, qrCodeId)
                }
            }
    }
}
