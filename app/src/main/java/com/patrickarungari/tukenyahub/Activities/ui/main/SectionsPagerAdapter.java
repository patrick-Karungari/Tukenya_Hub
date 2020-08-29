package com.patrickarungari.tukenyahub.Activities.ui.main;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.patrickarungari.tukenyahub.Fragments.ProvisionalFragment;
import com.patrickarungari.tukenyahub.Fragments.RegistrationFragment;
import com.patrickarungari.tukenyahub.Fragments.ResultslFragment;
import com.patrickarungari.tukenyahub.R;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_provResults, R.string.tab_regExam, R.string.tab_examResults};
    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        Fragment fragment;
        switch (position){
            case 0:
                fragment =  ProvisionalFragment.newInstance(position +1);
                break;
            case 1:
                fragment =  RegistrationFragment.newInstance(position +1 );
                break;
            case 2:
                fragment = ResultslFragment.newInstance(position + 1);
                break;
            default:
                fragment = PlaceholderFragment.newInstance(position + 1);
                break;

        }
        return  fragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 3;
    }
}