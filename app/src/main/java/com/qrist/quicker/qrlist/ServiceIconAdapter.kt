package com.qrist.quicker.qrlist

import android.net.Uri
import android.support.v4.view.ViewPager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nshmura.recyclertablayout.RecyclerTabLayout
import com.qrist.quicker.R
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.utils.getDrawableFromUri
import com.qrist.quicker.utils.serviceIdToIconUrl
import kotlinx.android.synthetic.main.custom_tab.view.*
import java.io.File
import kotlin.math.min

class ServiceIconAdapter(
    viewPager: ViewPager,
    private val qrCodes: List<QRCode>
) : RecyclerTabLayout.Adapter<ServiceIconAdapter.ViewHolder>(viewPager) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.custom_tab, parent, false)
        val onScreenLimit = min(qrCodes.size, MAX_ON_SCREEN_LIMIT).let {
            when (it) {
                1 -> 1
                2 -> 3
                else -> it
            }
        }
        val width: Int = parent.measuredWidth / onScreenLimit
        view.root.maxWidth = width
        view.root.minWidth = width
        return ViewHolder(view, viewPager)
    }

    override fun getItemCount(): Int = viewPager.adapter!!.count

    private fun getValueAt(position: Int): QRCode? =
        when (qrCodes.size) {
            0 -> null
            else -> qrCodes[position % qrCodes.size]
        }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val qrCode: QRCode? = getValueAt(position)
        qrCode?.let {
            val serviceIconUrl = when (qrCode) {
                is QRCode.Default -> Uri.parse(serviceIdToIconUrl(qrCode.serviceId))
                is QRCode.User -> Uri.fromFile(File(qrCode.serviceIconUrl))
            }
            holder.view.tab_icon.setImageDrawable(getDrawableFromUri(serviceIconUrl))
        }
    }

    class ViewHolder(
        val view: View,
        viewPager: ViewPager
    ) : RecyclerView.ViewHolder(view) {
        init {
            itemView.setOnClickListener{
                val pos: Int = adapterPosition
                if (pos != NO_POSITION) {
                    viewPager.setCurrentItem(pos, true)
                }
            }
        }
    }

    companion object {
        private const val MAX_ON_SCREEN_LIMIT = 6
        private const val NO_POSITION = -1
    }
}
