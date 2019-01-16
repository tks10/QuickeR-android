package com.qrist.quicker.qrlist

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter


class QRViewFragmentPagerAdapter(
    private val tabTitles: List<String>,
    fm: FragmentManager
) : FragmentPagerAdapter(fm) {

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitles[position]
    }

    override fun getItem(position: Int): Fragment? {
        return QRViewFragment.newInstance(position = position)
    }

    override fun getCount(): Int {
        return tabTitles.size
    }
}