package com.kcbbankgroup.tukenyahub.Activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.kcbbankgroup.tukenyahub.Fragments.DeferFragment;
import com.kcbbankgroup.tukenyahub.Fragments.applyIDFragment;
import com.kcbbankgroup.tukenyahub.Fragments.graduation_clearance_Fragment;
import com.kcbbankgroup.tukenyahub.R;

import static android.view.Window.FEATURE_NO_TITLE;

public class AcademicServicesActivity extends AppCompatActivity {

    ImageView button;
    Fragment fragment;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(FEATURE_NO_TITLE);//will hide the title
        setContentView(R.layout.activity_academic_services);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();
        initUi();

    }


    @SuppressLint("SetJavaScriptEnabled")
    private void initUi() {
        TextView textView =findViewById(R.id.dataTitle);
        button = findViewById(R.id.homeTo);
        button.setOnClickListener(v -> {
            fragment = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
            if(fragment != null) {
                fragmentManager = getSupportFragmentManager();
                fragmentManager.popBackStack();
                findViewById(R.id.grid).setVisibility(View.VISIBLE);
                textView.setText("ACADEMIC SERVICES");
            }else {
                AcademicServicesActivity.this.onBackPressed();
            }

        });
        findViewById(R.id.applyID).setOnClickListener(v -> {
            textView.setTextSize(24);
            textView.setText("APPLY / RENEW STUDENT ID");
            fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.containerLayout, new applyIDFragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            findViewById(R.id.grid).setVisibility(View.GONE);
        });
        findViewById(R.id.defer).setOnClickListener(v -> {
            textView.setTextSize(22);
            textView.setText("Apply For Deferment of Studies");
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.containerLayout, new DeferFragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            findViewById(R.id.grid).setVisibility(View.GONE);
        });
        findViewById(R.id.graduate).setOnClickListener(v -> {
            textView.setTextSize(22);
            textView.setText("Initiate Clearance Online");
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.containerLayout, new graduation_clearance_Fragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            findViewById(R.id.grid).setVisibility(View.GONE);
        });
    }


}

