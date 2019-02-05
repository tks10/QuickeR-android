//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.qrist.quicker.qrlist.widgets;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public abstract class QRViewFragmentStatePagerAdapter extends PagerAdapter {
	private static final String TAG = "FragmentPagerAdapter";
	private static final boolean DEBUG = false;
	private final FragmentManager mFragmentManager;
	private FragmentTransaction mCurTransaction = null;
	private Fragment mCurrentPrimaryItem = null;

	public QRViewFragmentStatePagerAdapter(FragmentManager fm) {
		this.mFragmentManager = fm;
	}

	public abstract Fragment getItem(int var1);

	public void startUpdate(@NonNull ViewGroup container) {
		if (container.getId() == -1) {
			throw new IllegalStateException("ViewPager with adapter " + this + " requires a view id");
		}
	}

	@NonNull
	public Object instantiateItem(@NonNull ViewGroup container, int position) {
		if (this.mCurTransaction == null) {
			this.mCurTransaction = this.mFragmentManager.beginTransaction();
		}

		String name = getItemId(position);
		Fragment fragment = this.mFragmentManager.findFragmentByTag(name);
		if (fragment != null) {
			this.mCurTransaction.attach(fragment);
		} else {
			fragment = this.getItem(position);
			this.mCurTransaction.add(container.getId(), fragment, name);
		}

		if (fragment != this.mCurrentPrimaryItem) {
			fragment.setMenuVisibility(false);
			fragment.setUserVisibleHint(false);
		}
        Log.d("fragment init item", fragment.toString());

		return fragment;
	}

	public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
		//if (this.mCurTransaction == null) {
		//	this.mCurTransaction = this.mFragmentManager.beginTransaction();
		//}

		//if (this.mCurrentPrimaryItem != object)
		//    this.mCurTransaction.detach((Fragment)object);
        //Log.d("delete fragment", object.toString());
	}

	public void deleteItems(String id) {
	    if (this.mCurTransaction == null) {
	        this.mCurTransaction = this.mFragmentManager.beginTransaction();
        }

        Fragment fragment = this.mFragmentManager.findFragmentByTag(id);
	    if (fragment != null) this.mCurTransaction.detach(fragment);
	    Log.e("delete fragment", "$id");
    }

    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
		Fragment fragment = (Fragment)object;
		if (fragment != this.mCurrentPrimaryItem) {
			if (this.mCurrentPrimaryItem != null) {
				this.mCurrentPrimaryItem.setMenuVisibility(false);
				this.mCurrentPrimaryItem.setUserVisibleHint(false);
			}

			fragment.setMenuVisibility(true);
			fragment.setUserVisibleHint(true);
			this.mCurrentPrimaryItem = fragment;
            Log.d("fragment primary item", fragment.toString());
		}

	}

	public void finishUpdate(@NonNull ViewGroup container) {
		if (this.mCurTransaction != null) {
			this.mCurTransaction.commitNowAllowingStateLoss();
			this.mCurTransaction = null;
			Log.d("fragment pager adapter", "update finished!");
		}

	}

	public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
		return ((Fragment)object).getView() == view;
	}

	public Parcelable saveState() {
		return null;
	}

	public void restoreState(Parcelable state, ClassLoader loader) {
	}

	public String getItemId(int position) {
		return Integer.toString(position);
	}
}

