package com.qrist.quicker.servicelist

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.qrist.quicker.R
import com.qrist.quicker.databinding.FragmentQrviewBinding
import com.qrist.quicker.extentions.obtainViewModel


class ServiceListFragment : Fragment() {
    private val viewModel: ServiceListViewModel
            by lazy { obtainViewModel(ServiceListViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val binding: FragmentQrviewBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_servicelist, container, false)
        binding.setLifecycleOwner(this)
        binding.viewmodel = viewModel
        return binding.root
    }
}
