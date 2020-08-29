package com.patrickarungari.tukenyahub.Activities;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.volley.toolbox.NetworkImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.patrickarungari.sharedPrefManager.SharedPrefManager;
import com.patrickarungari.tukenyahub.Modules.AsyncClass;
import com.patrickarungari.tukenyahub.Modules.ScreenRotation;
import com.patrickarungari.tukenyahub.R;
import com.pushlink.android.PushLink;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "Main Activity Lottie: ";
    TextView username, regNo;
    NetworkImageView imageView;

    @BindView(R.id.chat_room)
    CardView chat_room;
    @BindView(R.id.logout)
    CardView logout;

    @BindView(R.id.academic_services)
    CardView academicServ;

    @BindView(R.id.exam_results)
    CardView examResults;
    AlertDialog.Builder builder;
    AlertDialog alert;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new ScreenRotation(getApplicationContext(), this);
        setTheme(R.style.BrandedLaunch);
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        if (!SharedPrefManager.getInstance(getApplicationContext()).isLoggedIn()) {
            SharedPrefManager.getInstance(this).loginBefore(false);
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            //Bungee.slideRight(this);
            finish();
        } else {
            Log.e(TAG, "Lottie error");
            setContentView(R.layout.activity_main);
            ButterKnife.bind(this);
            initUi();
            new AsyncClass(getApplicationContext(), imageView, username, regNo);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    private void initUi() {

        ArrayList<CardView> button = new ArrayList<>();
        button.add(logout);
        button.add(academicServ);
        button.add(chat_room);
        button.add(examResults);
        for (int i = 0; i < button.size(); i++) {
            button.get(i).setOnClickListener(this);
            // Do something with the value
        }
        username = findViewById(R.id.user_name);
        regNo = findViewById(R.id.user_id);
        imageView = findViewById(R.id.user_photo);

        Thread thread = new Thread(() -> {
            MainActivity.this.runOnUiThread(MainActivity.this::alertBuilder);
        });
        thread.start();


    }


    @Override
    protected void onResume() {
        super.onResume();
        PushLink.setCurrentActivity(this);
    }

    public void setAcademicServ() {
        Intent intent = new Intent(this, AcademicServicesActivity.class);
        startActivity(intent);
        //Bungee.swipeLeft(this);
    }

    @Override
    public void onBackPressed() {

        alert.show();

    }

    private void alertBuilder() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {

            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    SharedPrefManager.getInstance(getApplicationContext()).logout();
                    Intent intent = getIntent();
                    String value = "OK";
                    intent.putExtra("key", value);
                    //Bungee.fade(this);
                    setResult(RESULT_OK, intent);
                    finish();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };

        builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("LOG OUT")
                .setMessage("Are you sure you want to Log out?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener);
        alert = builder.create();
        alert.setOnShowListener(dialog -> {
            Button btnPositive = alert.getButton(Dialog.BUTTON_POSITIVE);
            btnPositive.setTextSize(22);

            Button btnNegative = alert.getButton(Dialog.BUTTON_NEGATIVE);
            btnNegative.setTextSize(22);
            Button neuButton = alert.getButton(DialogInterface.BUTTON_NEUTRAL);

            LinearLayout.LayoutParams posParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
            posParams.weight = 1;
            posParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
            posParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            posParams.setMargins(10, 0, 0, 0);
            btnNegative.setLayoutParams(posParams);

            LinearLayout.LayoutParams negParams = (LinearLayout.LayoutParams) btnNegative.getLayoutParams();
            negParams.weight = 1;
            negParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            posParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;


            LinearLayout.LayoutParams neuParams = (LinearLayout.LayoutParams) neuButton.getLayoutParams();
            neuParams.weight = 1;
            neuParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
            neuParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            neuButton.setVisibility(View.GONE);

        });
        alert.create();
        // alert.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chat_room:
                startActivity(new Intent(this, SplashActivity.class));
                ///animation.slideUp(this);
                break;
            case R.id.logout:
                SharedPrefManager.getInstance(getApplicationContext()).logout();
                SharedPrefManager.getInstance(MainActivity.this).loginBefore(true);
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.academic_services:
                setAcademicServ();
                break;
            case R.id.exam_results:
                startActivity(new Intent(MainActivity.this, ExaminationActivity.class));
                break;
            default:
                Toast.makeText(this, "Click a view", Toast.LENGTH_LONG).show();
                break;
        }
    }
}
