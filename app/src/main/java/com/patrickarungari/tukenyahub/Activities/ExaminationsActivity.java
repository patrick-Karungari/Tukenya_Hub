package com.patrickarungari.tukenyahub.Activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.patrickarungari.tukenyahub.Activities.ui.main.SectionsPagerAdapter;
import com.patrickarungari.tukenyahub.Fragments.ProvisionalFragment;
import com.patrickarungari.tukenyahub.Fragments.RegistrationFragment;
import com.patrickarungari.tukenyahub.Fragments.ResultslFragment;
import com.patrickarungari.tukenyahub.Modules.ScreenRotation;
import com.patrickarungari.tukenyahub.R;

import java.util.Objects;

public class ExaminationsActivity extends AppCompatActivity {
    private static final int[] TAB_TITLES = new int[]{R.string.tab_provResults, R.string.tab_regExam, R.string.tab_examResults};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ScreenRotation(getApplicationContext(), this);
        setContentView(R.layout.examinations_activity);

        setupViewpager();
    }

    private void setupViewpager() {
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), getApplicationContext());
        sectionsPagerAdapter.addFragment(new ProvisionalFragment());
        sectionsPagerAdapter.addFragment(new RegistrationFragment());
        sectionsPagerAdapter.addFragment(new ResultslFragment());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setCurrentItem(1, true);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        Objects.requireNonNull(tabs.getTabAt(1)).select();
    }
}