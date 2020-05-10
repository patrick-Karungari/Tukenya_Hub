package com.kcbbankgroup.tukenyahub.Activities;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.kcbbankgroup.tukenyahub.Modules.ScreenRotation;
import com.kcbbankgroup.tukenyahub.Modules.SharedPrefManager;
import com.kcbbankgroup.tukenyahub.R;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    TextView username, regNo;
    ImageView imageView;
    CardView academicServ,logout;
    AlertDialog.Builder builder;
    //Button logout;
    AlertDialog alert;
    String ImageData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new ScreenRotation(getApplicationContext(), this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initUi();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // land
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // port
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private void initUi() {
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).hide();
        username = findViewById(R.id.user_name);
        logout = findViewById(R.id.logout);
        regNo = findViewById(R.id.user_id);
        imageView = findViewById(R.id.user_photo);
        academicServ = findViewById(R.id.profile);
        regNo.setText(SharedPrefManager.getInstance(getApplicationContext()).getuniNum());
        username.setText(SharedPrefManager.getInstance(getApplicationContext()).getUsername());
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //  ImageData = SharedPrefManager.getInstance(MainActivity.this.getApplicationContext()).getimageData();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.alertBuilder();
                        // imageView.setImageBitmap(MainActivity.this.decodeImage(ImageData));
                    }
                });
            }
        });
        thread.start();
        academicServ.setOnClickListener(view -> MainActivity.this.quickLinks());
        logout.setOnClickListener(v -> {
            SharedPrefManager.getInstance(getApplicationContext()).logout();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });
    }

    private Bitmap decodeImage(String imageStr) {
        byte[] decodedString = Base64.decode(imageStr, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    public void quickLinks() {
        Intent intent = new Intent(this, AcademicServicesActivity.class);
        startActivity(intent);
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
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
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
}
