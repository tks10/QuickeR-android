package com.qrist.quicker.serviceaddlist

import android.annotation.SuppressLint
import android.content.Context
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.qrist.quicker.databinding.ServiceaddlistItemBinding
import com.qrist.quicker.databinding.UserServiceaddlistItemBinding
import com.qrist.quicker.models.ServiceItem

class ServiceAddListAdapter(val context: Context, private val serviceItems: List<ServiceItem>)
    : androidx.recyclerview.widget.RecyclerView.Adapter<ServiceAddListAdapter.ViewHolder>() {
    private var listener: View.OnClickListener? = null

    class ViewHolder(val binding: ViewDataBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = when (viewType) {
            DEFAULT_SERVICE -> ServiceaddlistItemBinding.inflate(inflater, parent, false)
            else -> UserServiceaddlistItemBinding.inflate(inflater, parent, false)
        }
        return ViewHolder(binding)
    }

    fun setOnItemClickListener(listener: View.OnClickListener) {
        this.listener = listener
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Binding
        when (holder.itemViewType) {
            DEFAULT_SERVICE ->
                (holder.binding as ServiceaddlistItemBinding).serviceItem = serviceItems[position]
            else ->
                (holder.binding as UserServiceaddlistItemBinding).serviceItem = serviceItems[position]
        }

        // Set Click Listener
        holder.binding.root.id = holder.adapterPosition
        holder.binding.root.setOnClickListener {
            listener?.onClick(it)
        }
    }

    override fun getItemCount() = serviceItems.size

    override fun getItemViewType(position: Int): Int =
        when (position == itemCount - 1) {
            true -> USER_SERVICE
            false -> DEFAULT_SERVICE
        }

    companion object {
        private const val DEFAULT_SERVICE = 0
        private const val USER_SERVICE = 1
    }
}
