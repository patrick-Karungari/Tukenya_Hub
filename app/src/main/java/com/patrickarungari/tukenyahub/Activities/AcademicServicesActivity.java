package com.patrickarungari.tukenyahub.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.patrickarungari.tukenyahub.Fragments.DeferFragment;
import com.patrickarungari.tukenyahub.Fragments.applyIDFragment;
import com.patrickarungari.tukenyahub.Fragments.graduation_clearance_Fragment;
import com.patrickarungari.tukenyahub.Modules.ScreenRotation;
import com.patrickarungari.tukenyahub.R;
import com.pushlink.android.PushLink;

public class AcademicServicesActivity extends AppCompatActivity {
    TextView textView;
    ImageView button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ScreenRotation(getApplicationContext(), this);
        setContentView(R.layout.activity_academic_services);
        initUi();

    }

    @Override
    protected void onResume() {
        super.onResume();
        PushLink.setCurrentActivity(this);
    }

    private void initUi() {
        textView = findViewById(R.id.dataTitle);
        button = findViewById(R.id.homeTo);
        button.setOnClickListener(v -> {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
            if (fragment != null) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.popBackStack();
                findViewById(R.id.grid).setVisibility(View.VISIBLE);
                textView.setText("ACADEMIC SERVICES");
            } else {
                startActivity(new Intent(this, MainActivity.class));
            }

        });
        findViewById(R.id.applyID).setOnClickListener(v -> {
            textView.setTextSize(24);
            textView.setText("APPLY / RENEW STUDENT ID");
            setFragment(new applyIDFragment());
        });
        findViewById(R.id.defer).setOnClickListener(v -> {
            textView.setTextSize(22);
            textView.setText("Apply For Deferment of Studies");
            setFragment(new DeferFragment());
        });
        findViewById(R.id.graduate).setOnClickListener(v -> {
            textView.setTextSize(22);
            textView.setText("Initiate Clearance Online");
            setFragment(new graduation_clearance_Fragment());

        });
        findViewById(R.id.exams).setOnClickListener(v -> startActivity(new Intent(AcademicServicesActivity.this, ExaminationsActivity.class)));
    }

    @Override
    public void onBackPressed() {
        button.performClick();
    }

    public void setFragment(Fragment fragment) {
        findViewById(R.id.grid).setVisibility(View.GONE);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.containerLayout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}

