package com.qrist.quicker.qrlist

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter


class QRViewFragmentPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private val tabTitles = arrayOf<CharSequence>("タブ1", "タブ2", "タブ3", "タブ1", "タブ2", "タブ3", "タブ1", "タブ2", "タブ3")

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