package com.qrist.quicker.servicelist

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.qrist.quicker.R
import com.qrist.quicker.databinding.FragmentServicelistBinding
import com.qrist.quicker.extentions.obtainViewModel
import kotlinx.android.synthetic.main.fragment_servicelist.view.*
import kotlinx.android.synthetic.main.servicelist_item.view.*


class ServiceListFragment : Fragment() {
    private val viewModel: ServiceListViewModel
            by lazy { obtainViewModel(ServiceListViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val binding: FragmentServicelistBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_servicelist, container, false)
        binding.setLifecycleOwner(this)
        binding.viewmodel = viewModel

        binding.root.serviceList.adapter = ServiceListAdapter(activity!!, viewModel.getServiceListViewers()).apply {
            setOnItemClickListener(View.OnClickListener {
                Log.d("Position", (it.parent as ConstraintLayout).id.toString())
            })
        }

        val itemDecoration = DividerItemDecoration(activity!!, DividerItemDecoration.VERTICAL)
        binding.root.serviceList.addItemDecoration(itemDecoration)

        return binding.root
    }
}
