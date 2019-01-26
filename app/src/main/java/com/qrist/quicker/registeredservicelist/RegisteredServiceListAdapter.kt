package com.qrist.quicker.registeredservicelist

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.qrist.quicker.databinding.RegisteredservicelistItemBinding
import com.qrist.quicker.models.ServiceItem
import kotlinx.android.synthetic.main.registeredservicelist_item.view.*

class RegisteredServiceListAdapter(val context: Context, private val serviceItems: List<ServiceItem>)
    : RecyclerView.Adapter<RegisteredServiceListAdapter.ViewHolder>() {
    private var listener: View.OnClickListener? = null

    class ViewHolder(val binding: RegisteredservicelistItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RegisteredservicelistItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    fun setOnItemClickListener(listener: View.OnClickListener) {
        this.listener = listener
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Binding
        holder.binding.serviceItem = serviceItems[position]

        // Set Click Listener
        holder.binding.root.id = holder.adapterPosition
        holder.binding.root.deleteButton.setOnClickListener {
            listener?.onClick(it)
        }
    }

    override fun getItemCount() = serviceItems.size
}
