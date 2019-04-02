package com.qrist.quicker.qrlist

import android.annotation.SuppressLint
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.PagerAdapter
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.qrist.quicker.models.QRCode

class QRViewFragmentPagerAdapter(
    private var qrCodes: List<QRCode>,
    internal val fragmentManager: androidx.fragment.app.FragmentManager
) : androidx.viewpager.widget.PagerAdapter() {

    private var currentTransaction: androidx.fragment.app.FragmentTransaction? = null
    private var currentFragment: androidx.fragment.app.Fragment? = null

    // begin lifecycle

    override fun startUpdate(container: ViewGroup) {
        if (container.id == -1) {
            throw IllegalStateException("ViewPager with adapter $this requires a view id")
        }
    }

    @SuppressLint("CommitTransaction")
    override fun instantiateItem(container: ViewGroup, position: Int): Any =
        fragmentManager.findFragmentByTag("$position").let { fragment ->
            currentTransaction ?: fragmentManager.beginTransaction().also {
                currentTransaction = it
            }
            when (fragment) {
                null -> {
                    val returnVal = getItem(position)!!
                    currentTransaction?.add(container.id, returnVal, "$position")
                    returnVal
                }
                else -> {
                    detachItems()
                    currentTransaction?.attach(fragment)
                    fragment
                }
            }.apply {
                if (fragment != currentFragment) {
                    fragment?.setMenuVisibility(false)
                    fragment?.userVisibleHint = false
                }
                Log.d("fragment init item", fragment.toString())
            }
        }

    @SuppressLint("CommitTransaction")
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        fragmentManager.findFragmentByTag("$position")?.let { fragment ->
            currentTransaction ?: fragmentManager.beginTransaction().also {
                currentTransaction = it
            }.remove(fragment).commitNowAllowingStateLoss().also {
                currentTransaction = null
            }
            Log.d("delete fragment", fragment.toString())
        }
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        val fragment = `object` as androidx.fragment.app.Fragment
        if (fragment != currentFragment) {
            fragment.setMenuVisibility(true)
            fragment.userVisibleHint = true
            this.currentFragment = fragment
            Log.d("fragment primary item", fragment.toString())
        }
    }

    override fun finishUpdate(container: ViewGroup) {
        currentTransaction?.commitNowAllowingStateLoss().also {
            currentTransaction = null
        }
    }

    // finish lifecycle

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return (`object` as androidx.fragment.app.Fragment).view == view
    }

    override fun getCount(): Int = if (qrCodes.size == 1) 1 else qrCodes.size * NUMBER_OF_LOOPS

    override fun getPageTitle(position: Int): CharSequence? = "  $position  "

    @SuppressLint("CommitTransaction")
    fun detachItems() {
        fragmentManager.fragments.forEach { fragment ->
            fragment?.let {
                currentTransaction ?: fragmentManager.beginTransaction().also { transaction ->
                    currentTransaction = transaction
                }.remove(fragment).commitNowAllowingStateLoss().also {
                    currentTransaction = null
                }
            }
        }
    }

    fun getCenterPosition(position: Int): Int = count / 2 + position

    fun getAdapterPosition(centerPosition: Int) = centerPosition - count / 2

    private fun getValueAt(position: Int): QRCode? =
        when (qrCodes.size) {
            0 -> null
            else -> qrCodes[position % qrCodes.size]
        }

    private fun getItem(position: Int): androidx.fragment.app.Fragment? =
        getValueAt(position)?.let {
            QRViewFragment.newInstance(qrCodeId = it.id)
        }

    override fun saveState(): Parcelable? = null

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
    }

    companion object {
        private const val NUMBER_OF_LOOPS = 10000

        private var INSTANCE: QRViewFragmentPagerAdapter? = null

        fun getInstance(qrCodes: List<QRCode>, fm: androidx.fragment.app.FragmentManager): QRViewFragmentPagerAdapter {
            if (INSTANCE?.fragmentManager !== fm) INSTANCE = null
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