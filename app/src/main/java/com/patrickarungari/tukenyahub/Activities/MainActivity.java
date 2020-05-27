package com.patrickarungari.tukenyahub.Activities;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.patrickarungari.tukenyahub.Modules.ScreenRotation;
import com.patrickarungari.tukenyahub.Modules.SharedPrefManager;
import com.patrickarungari.tukenyahub.R;
import com.pushlink.android.PushLink;

import spencerstudios.com.bungeelib.Bungee;

public class MainActivity extends AppCompatActivity {
    TextView username, regNo;
    ImageView imageView;
    CardView academicServ, logout;
    AlertDialog.Builder builder;
    AlertDialog alert;
    CardView examResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new ScreenRotation(getApplicationContext(), this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initUi();

    }

    private void initUi() {
        examResults = findViewById(R.id.exam_results);
        username = findViewById(R.id.user_name);
        logout = findViewById(R.id.logout);
        regNo = findViewById(R.id.user_id);
        imageView = findViewById(R.id.user_photo);
        academicServ = findViewById(R.id.profile);
        regNo.setText(SharedPrefManager.getInstance(getApplicationContext()).getuniNum());
        username.setText(SharedPrefManager.getInstance(getApplicationContext()).getUsername());
        Thread thread = new Thread(() -> {
            //  ImageData = SharedPrefManager.getInstance(MainActivity.this.getApplicationContext()).getimageData();
            MainActivity.this.runOnUiThread(() -> {
                MainActivity.this.alertBuilder();
                imageView.setImageBitmap(decodeImage(SharedPrefManager.getInstance(getApplicationContext()).getimageData()));
                // imageView.setImageBitmap(MainActivity.this.decodeImage(ImageData));
            });
        });
        thread.start();
        examResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ExaminationsActivity.class));
            }
        });
        academicServ.setOnClickListener(view -> MainActivity.this.setAcademicServ());

        logout.setOnClickListener(v -> {
            SharedPrefManager.getInstance(getApplicationContext()).logout();
            String value = "OK";
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.putExtra("key", value);
            setResult(RESULT_OK, intent);
            finish();

        });
    }

    private Bitmap decodeImage(String imageStr) {
        byte[] decodedString = Base64.decode(imageStr, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PushLink.setCurrentActivity(this);
    }

    public void setAcademicServ() {
        Intent intent = new Intent(this, AcademicServicesActivity.class);
        startActivity(intent);
        Bungee.swipeLeft(this);
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
