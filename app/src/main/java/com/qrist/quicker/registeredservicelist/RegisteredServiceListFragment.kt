package com.qrist.quicker.registeredservicelist

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.MaterialDialog
import com.qrist.quicker.R
import com.qrist.quicker.databinding.FragmentRegisteredservicelistBinding
import com.qrist.quicker.extentions.obtainViewModel
import kotlinx.android.synthetic.main.fragment_registeredservicelist.view.*


class RegisteredServiceListFragment : Fragment() {
    private val viewModel: RegisteredServiceListViewModel
            by lazy { obtainViewModel(RegisteredServiceListViewModel::class.java) }
    private val serviceItems by lazy { viewModel.getServiceItems() }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val binding: FragmentRegisteredservicelistBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_registeredservicelist, container, false)

        binding.setLifecycleOwner(this)
        binding.viewmodel = viewModel

        binding.root.registeredServiceList.adapter =
                RegisteredServiceListAdapter(activity!!, serviceItems).apply {
                    setOnItemClickListener(View.OnClickListener {
                        val position = (it.parent as ConstraintLayout).id
                        val service = serviceItems[position]
                        MaterialDialog(activity!!).show {
                            title(R.string.title_delete)
                            message(R.string.message_delete)
                            positiveButton(R.string.agree_delete) { dialog ->
                                // Do something
                                Snackbar.make(view!!, "Yes", Snackbar.LENGTH_LONG).show()
                            }
                            negativeButton(R.string.cancel) { dialog ->
                                // Do something
                                Snackbar.make(view!!, "No", Snackbar.LENGTH_LONG).show()
                            }
                        }
                    })
                }

        val itemDecoration = DividerItemDecoration(activity!!, DividerItemDecoration.VERTICAL)
        binding.root.registeredServiceList.addItemDecoration(itemDecoration)

        return binding.root
    }
}
