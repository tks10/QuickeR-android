package com.qrist.quicker.servicelist

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.qrist.quicker.databinding.ServicelistItemBinding
import com.qrist.quicker.models.ServiceListViewer
import kotlinx.android.synthetic.main.servicelist_item.view.*

class ServiceListAdapter(val context: Context, private val serviceListViewers: List<ServiceListViewer>)
    : RecyclerView.Adapter<ServiceListAdapter.ViewHolder>() {
    private var listener: View.OnClickListener? = null

    class ViewHolder(val binding: ServicelistItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ServicelistItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    fun setOnItemClickListener(listener: View.OnClickListener) {
        this.listener = listener
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Binding
        holder.binding.serviceListViewer = serviceListViewers[position]

        // Set Click Listener
        holder.binding.root.id = holder.adapterPosition
        holder.binding.root.addButton.setOnClickListener {
            listener?.onClick(it)
        }
    }

    override fun getItemCount() = serviceListViewers.size
}
