package com.qrist.quicker.serviceaddlist

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.qrist.quicker.databinding.ServiceaddlistItemBinding
import com.qrist.quicker.models.ServiceAddItem
import kotlinx.android.synthetic.main.serviceaddlist_item.view.*

class ServiceAddListAdapter(val context: Context, private val serviceAddItems: List<ServiceAddItem>)
    : RecyclerView.Adapter<ServiceAddListAdapter.ViewHolder>() {
    private var listener: View.OnClickListener? = null

    class ViewHolder(val binding: ServiceaddlistItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ServiceaddlistItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    fun setOnItemClickListener(listener: View.OnClickListener) {
        this.listener = listener
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Binding
        holder.binding.serviceAddItem = serviceAddItems[position]

        // Set Click Listener
        holder.binding.root.id = holder.adapterPosition
        holder.binding.root.addButton.setOnClickListener {
            listener?.onClick(it)
        }
    }

    override fun getItemCount() = serviceAddItems.size
}
