package com.qrist.quicker.registeredservicelist

import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.qrist.quicker.R
import com.qrist.quicker.databinding.FragmentRegisteredservicelistBinding
import com.qrist.quicker.extentions.obtainViewModel
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.utils.MyApplication
import com.qrist.quicker.utils.getDrawableFromUri
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_registeredservicelist.*
import java.io.File

class RegisteredServiceListFragment : Fragment() {
    private val viewModel: RegisteredServiceListViewModel
            by lazy { obtainViewModel(RegisteredServiceListViewModel::class.java, this) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        DataBindingUtil.inflate<FragmentRegisteredservicelistBinding>(inflater, R.layout.fragment_registeredservicelist, container, false).apply {
            setLifecycleOwner(this@RegisteredServiceListFragment)
            viewmodel = viewModel
        }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registeredServiceList.adapter = this.createAdapter()
        val itemDecoration = DividerItemDecoration(activity!!, DividerItemDecoration.VERTICAL)
        registeredServiceList.addItemDecoration(itemDecoration)
        this.createItemTouchHelper().attachToRecyclerView(registeredServiceList)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.apply {
            tool_bar.menu.clear()
            tool_bar.inflateMenu(R.menu.settings_menu)
            tool_bar.setOnMenuItemClickListener {item ->
                when (item.itemId) {
                    R.id.menu_oss_license -> {
                        startActivity(Intent(context!!, OssLicensesMenuActivity::class.java))
                        OssLicensesMenuActivity.setActivityTitle(context!!.resources.getString(R.string.open_source_license))
                        true
                    }
                    else -> false
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        MyApplication.analytics.setCurrentScreen(requireActivity(), this.javaClass.simpleName, this.javaClass.simpleName)
    }

    private fun updateItems() {
        viewModel.fetchQRCodes()
        registeredServiceList.adapter = createAdapter()
        registeredServiceList.adapter?.notifyDataSetChanged()
    }

    private fun createAdapter(): RegisteredServiceListAdapter =
        RegisteredServiceListAdapter(activity!!, viewModel.getServiceItems()).apply {
            setOnItemClickListener(View.OnClickListener {
                val position = (it.parent as ConstraintLayout).id
                val qrCode = viewModel.qrCodes[position]
                val uri = when(qrCode) {
                    is QRCode.Default -> Uri.parse(qrCode.serviceIconUrl)
                    is QRCode.User -> Uri.fromFile(File(qrCode.serviceIconUrl))
                }

                MaterialDialog(activity!!).show {
                    title(R.string.title_delete)
                    message(R.string.message_delete)
                    icon(drawable = getDrawableFromUri(uri))
                    positiveButton(R.string.agree_delete) {
                        // Do something
                        val qrCode = viewModel.qrCodes[position]
                        if (viewModel.deleteQRCode(qrCode.id)) {
                            this@RegisteredServiceListFragment.updateItems()
                            Snackbar.make(view!!, R.string.service_deleted, Snackbar.LENGTH_LONG).show()
                        }
                    }
                    negativeButton(R.string.cancel) {
                    }
                }
            })
        }

    private fun createItemTouchHelper(): ItemTouchHelper =
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition

                recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)
                viewModel.moveIndex(fromPosition, toPosition)

                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                viewModel.reflectIndexChange()
            }
        })
}
