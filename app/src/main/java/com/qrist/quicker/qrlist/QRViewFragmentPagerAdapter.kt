package com.qrist.quicker.qrlist

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.qrist.quicker.models.QRCode


class QRViewFragmentPagerAdapter(
    private val qrCodes: List<QRCode>,
    fm: FragmentManager
) : FragmentStatePagerAdapter(fm) {

    override fun getPageTitle(position: Int): CharSequence? {
        return when(qrCodes[position]) {
            is QRCode.Default ->(qrCodes[position] as QRCode.Default).serviceName
            is QRCode.User -> (qrCodes[position] as QRCode.User).serviceName
        }
    }

    override fun getItem(position: Int): Fragment? {
        return QRViewFragment.newInstance(qrCodeId = qrCodes[position].id)
    }

    override fun getCount(): Int {
        return qrCodes.size
    }
}