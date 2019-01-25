package com.qrist.quicker.serviceaddlist

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.qrist.quicker.R
import com.qrist.quicker.databinding.FragmentServiceaddlistBinding
import com.qrist.quicker.extentions.obtainViewModel
import kotlinx.android.synthetic.main.fragment_serviceaddlist.view.*


class ServiceAddListFragment : Fragment() {
    private val viewModel: ServiceAddListViewModel
            by lazy { obtainViewModel(ServiceAddListViewModel::class.java) }
    private val serviceAddItems by lazy { viewModel.getServiceAddItems() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val binding: FragmentServiceaddlistBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_serviceaddlist, container, false)
        binding.setLifecycleOwner(this)
        binding.viewmodel = viewModel

        binding.root.serviceAddList.adapter = ServiceAddListAdapter(activity!!, serviceAddItems).apply {
            setOnItemClickListener(View.OnClickListener {
                val position = (it.parent as ConstraintLayout).id
                val service = serviceAddItems[position]
                val action =
                    ServiceAddListFragmentDirections.actionServiceaddlistToRegister(
                        service.serviceName,
                        service.serviceIconUrl
                    )
                Navigation.findNavController(view!!).navigate(action)
            })
        }

        val itemDecoration = DividerItemDecoration(activity!!, DividerItemDecoration.VERTICAL)
        binding.root.serviceAddList.addItemDecoration(itemDecoration)

        return binding.root
    }
}
