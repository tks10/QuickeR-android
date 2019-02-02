package com.qrist.quicker.qrlist

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.qrist.quicker.models.QRCode

class QRViewFragmentPagerAdapter(
    private val qrCodes: List<QRCode>,
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

    companion object {
        private const val NUMBER_OF_LOOPS = 10000
    }
}