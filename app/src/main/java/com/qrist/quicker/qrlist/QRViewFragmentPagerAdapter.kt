package com.qrist.quicker.qrlist

import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.util.Log
import com.qrist.quicker.models.QRCode
import com.qrist.quicker.qrlist.widgets.QRViewFragmentStatePagerAdapter

class QRViewFragmentPagerAdapter(
    private var qrCodes: List<QRCode>,
    private val fm: FragmentManager
) : QRViewFragmentStatePagerAdapter(fm) {

    fun getCenterPosition(position: Int): Int = qrCodes.size * NUMBER_OF_LOOPS / 2 + position

    private fun getValueAt(position: Int): QRCode? =
        when (qrCodes.size) {
            0 -> null
            else -> qrCodes[position % qrCodes.size]
        }

    override fun getCount(): Int =
        (qrCodes.size * NUMBER_OF_LOOPS).let {
            when (qrCodes.size) {
                1 -> 1
                else -> it
            }
        }

    override fun getItem(position: Int): Fragment? =
        getValueAt(position)?.let {
            QRViewFragment.newInstance(qrCodeId = it.id)
        }

    override fun getItemId(position: Int): String? =
        getValueAt(position)?.let {
            it.id
        }

    override fun saveState(): Parcelable? = null

    companion object {
        private const val NUMBER_OF_LOOPS = 10000

        private var INSTANCE: QRViewFragmentPagerAdapter? = null

        fun getInstance(qrCodes: List<QRCode>, fm: FragmentManager): QRViewFragmentPagerAdapter {
            if (INSTANCE?.fm !== fm) INSTANCE = null
            return INSTANCE?.apply {
                // if qrCodes is changed, the reference is gonna be changed because of LiveData.
                if (this.qrCodes !== qrCodes) {
                    this.qrCodes = qrCodes
                }
                Log.d("PagerAdapter", "$this is updated, qrCodes is ${this.qrCodes}")
            } ?: QRViewFragmentPagerAdapter(qrCodes, fm).also {
                INSTANCE = it
            }
        }
    }
}