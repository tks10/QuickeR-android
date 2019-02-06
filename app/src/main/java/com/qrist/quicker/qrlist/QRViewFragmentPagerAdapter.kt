package com.qrist.quicker.qrlist

import android.annotation.SuppressLint
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.PagerAdapter
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.qrist.quicker.models.QRCode

class QRViewFragmentPagerAdapter(
    private var qrCodes: List<QRCode>,
    private val fm: FragmentManager
) : PagerAdapter() {

    private var currentTransaction: FragmentTransaction? = null
    private var currentFragment: Fragment? = null

    // begin lifecycle

    override fun startUpdate(container: ViewGroup) {
        if (container.id == -1) {
            throw IllegalStateException("ViewPager with adapter $this requires a view id")
        }
        Log.d("fragment", "start update!")
    }

    @SuppressLint("CommitTransaction")
    override fun instantiateItem(container: ViewGroup, position: Int): Any =
        fm.findFragmentByTag(getItemId(position)).let { fragment ->
            currentTransaction ?: fm.beginTransaction().also {
                currentTransaction = it
            }
            when (fragment) {
                null -> {
                    val returnVal = getItem(position)!!
                    currentTransaction!!.add(container.id, returnVal, getItemId(position))
                    returnVal
                }
                else -> {
                    currentTransaction!!.attach(fragment)
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

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        Log.d("delete fragment", `object`.toString())
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        val fragment = `object` as Fragment
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
        Log.d("fragment pager adapter", "update finished!")
    }

    // finish lifecycle

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return (`object` as Fragment).view == view
    }

    override fun getCount(): Int =
        (qrCodes.size * NUMBER_OF_LOOPS).let {
            when (qrCodes.size) {
                1 -> 1
                else -> it
            }
        }

    @SuppressLint("CommitTransaction")
    fun detachItems() {
        qrCodes.forEach { code ->
            fm.findFragmentByTag(code.id)?.also {fragment ->
                currentTransaction ?: fm.beginTransaction().also { transaction ->
                    currentTransaction = transaction
                }.detach(fragment)
            }
        }
    }

    fun getCenterPosition(position: Int): Int = qrCodes.size * NUMBER_OF_LOOPS / 2 + position

    private fun getValueAt(position: Int): QRCode? =
        when (qrCodes.size) {
            0 -> null
            else -> qrCodes[position % qrCodes.size]
        }

    private fun getItem(position: Int): Fragment? =
        getValueAt(position)?.let {
            QRViewFragment.newInstance(qrCodeId = it.id)
        }

    private fun getItemId(position: Int): String? =
        getValueAt(position)?.id


    override fun saveState(): Parcelable? = null

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
    }

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