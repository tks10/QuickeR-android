package com.qrist.quicker.registeredservicelist

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.qrist.quicker.R
import com.qrist.quicker.databinding.FragmentRegisteredservicelistBinding
import com.qrist.quicker.extentions.obtainViewModel


class RegisteredServiceListFragment : Fragment() {
    private val viewModel: RegisteredServiceListViewModel
            by lazy { obtainViewModel(RegisteredServiceListViewModel::class.java) }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val binding: FragmentRegisteredservicelistBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_registeredservicelist, container, false)

        binding.setLifecycleOwner(this)
        binding.viewmodel = viewModel

        return binding.root
    }
}
