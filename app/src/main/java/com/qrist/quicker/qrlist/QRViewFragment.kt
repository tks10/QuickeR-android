package com.qrist.quicker.qrlist

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
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
    private var codeId: String? = null
    private var viewModel: QRViewViewModel? = null
    private var changedServiceNameVisibility: MutableLiveData<Unit>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        codeId = arguments!!.getString(BUNDLE_ARG_ID)
        viewModel = codeId?.let {
            obtainViewModel(it, QRViewViewModel::class.java)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val binding: FragmentQrviewBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_qrview, container, false)

        binding.setLifecycleOwner(this)
        binding.viewmodel = viewModel

        binding.root.qrCardView.setOnClickListener {
            val changeBounds = ChangeBounds().apply { duration = 100L }
            TransitionManager.beginDelayedTransition(binding.root.qrCardView, changeBounds)
            viewModel?.switchServiceNameVisibility()
            changedServiceNameVisibility?.notify()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e("create fragment", "$this $codeId $viewModel")
        codeId?.let {
            viewModel?.fetchImageUrl(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.e("destroy fragment", "$this $codeId $viewModel")
        viewModel = null
        codeId = null
        changedServiceNameVisibility = null
    }

    private fun setServiceNameChangedLiveData(liveData: MutableLiveData<Unit>) {
        changedServiceNameVisibility = liveData.apply {
            observe(this@QRViewFragment, Observer {
                viewModel?.fetchServiceNameVisibility()
            })
        }
    }

    private fun MutableLiveData<Unit>.notify() {
        this.value = Unit
    }

    companion object {
        private const val BUNDLE_ARG_ID = "id"

        fun newInstance(qrCodeId: String, serviceNameChangedLiveData: MutableLiveData<Unit>): QRViewFragment =
            QRViewFragment().apply {
                arguments = Bundle().apply {
                    putString(BUNDLE_ARG_ID, qrCodeId)
                }
                setServiceNameChangedLiveData(serviceNameChangedLiveData)
            }
    }
}
