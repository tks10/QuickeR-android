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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_serviceaddlist.*

class ServiceAddListFragment : Fragment() {
    private val viewModel: ServiceAddListViewModel
            by lazy { obtainViewModel(ServiceAddListViewModel::class.java) }
    private val qrImageUrl: String by lazy { ServiceAddListFragmentArgs.fromBundle(arguments!!).qrImageUrl }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        DataBindingUtil.inflate<FragmentServiceaddlistBinding>(inflater, R.layout.fragment_serviceaddlist, container, false).apply{
            setLifecycleOwner(this@ServiceAddListFragment)
            viewmodel = viewModel
        }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        serviceAddList.adapter = initAdapter()

        val itemDecoration = DividerItemDecoration(activity!!, DividerItemDecoration.VERTICAL)
        serviceAddList.addItemDecoration(itemDecoration)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.apply {
            tool_bar.menu.clear()
        }
    }

    override fun onStart() {
        super.onStart()
        serviceAddList.adapter = initAdapter()
        serviceAddList.adapter?.notifyDataSetChanged()
    }

    private fun initAdapter():  ServiceAddListAdapter =
        viewModel.getServiceItems().let { serviceItems ->
            ServiceAddListAdapter(activity!!, serviceItems).apply {
                setOnItemClickListener(View.OnClickListener {
                    val position = (it as ConstraintLayout).id
                    val service = serviceItems[position]
                    val action =
                        ServiceAddListFragmentDirections.actionServiceaddlistToRegister(
                            qrImageUrl,
                            service.serviceName,
                            service.serviceIconUrl
                        )
                    Navigation.findNavController(view!!).navigate(action)
                })
            }
        }
}
