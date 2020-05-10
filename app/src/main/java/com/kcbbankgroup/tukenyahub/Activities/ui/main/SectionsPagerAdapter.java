package com.kcbbankgroup.tukenyahub.Activities.ui.main;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.kcbbankgroup.tukenyahub.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
    //private static final int[] TAB_TITLES = new int[]{R.string.tab_provResults, R.string.tab_regExam, R.string.tab_examResults};
    private final List<Fragment> mFragment = new ArrayList<>();
    private Context context;
    private String[] TAB_TITLES;


    public SectionsPagerAdapter(FragmentManager fm, Context mContext) {
        super(fm);
        context = mContext;
        TAB_TITLES = new String[]{mContext.getResources().getString(R.string.tab_provResults),
                mContext.getResources().getString(R.string.tab_regExam),
                mContext.getResources().getString(R.string.tab_examResults)};
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return mFragment.get(position);
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return mFragment.size();
    }

    public void addFragment(Fragment fragment) {
        mFragment.add(fragment);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TAB_TITLES[position];
    }
}