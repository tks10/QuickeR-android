package com.qrist.quicker.qrlist

import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.util.Log
import com.qrist.quicker.models.QRCode

class QRViewFragmentPagerAdapter(
    private var qrCodes: List<QRCode>,
    fm: FragmentManager
) : FragmentStatePagerAdapter(fm) {

    override fun getCount(): Int = (qrCodes.size * NUMBER_OF_LOOPS).let {
        when (qrCodes.size) {
            1 -> 1
            else -> it
        }
    }

    fun getCenterPosition(position: Int) = qrCodes.size * NUMBER_OF_LOOPS / 2 + position

    private fun getValueAt(position: Int): QRCode? =
        when (qrCodes.size) {
            0 -> null
            else -> qrCodes[position % qrCodes.size]
        }

    override fun getItem(position: Int): Fragment? =
        getValueAt(position)?.let {
            QRViewFragment.newInstance(qrCodeId = it.id)
        }

    override fun saveState(): Parcelable? = null

    companion object {
        private const val NUMBER_OF_LOOPS = 10000

        private var INSTANCE: QRViewFragmentPagerAdapter? = null

        fun getInstance(qrCodes: List<QRCode>, fm: FragmentManager) =
            INSTANCE?.apply {
                if (this.qrCodes != qrCodes) this.qrCodes = qrCodes
                Log.d("PagerAdapter", "$this")
            } ?: QRViewFragmentPagerAdapter(qrCodes, fm).apply {
                INSTANCE = this
            }
    }
}