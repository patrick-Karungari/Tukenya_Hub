package com.kcbbankgroup.tukenyahub.Activities;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.kcbbankgroup.tukenyahub.Fragments.SignUp;
import com.kcbbankgroup.tukenyahub.R;

public class SignupActivity extends AppCompatActivity {


    AlertDialog.Builder builder;
    AlertDialog alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_signup);
        Thread thread = new Thread(() -> {
            SignupActivity.this.runOnUiThread(this::alertBuilder);
        });
        thread.start();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.containerFrame,new SignUp());
        fragmentTransaction.commit();
    }

    private void alertBuilder() {
        android.content.DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case android.content.DialogInterface.BUTTON_POSITIVE:
                    //SharedPrefManager.getInstance(getApplicationContext()).logout();
                    finishAndRemoveTask();
                    System.exit(0);

                    break;

                case android.content.DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };
        builder = new AlertDialog.Builder(SignupActivity.this);
        builder.setTitle("EXIT")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener);
        alert = builder.create();
        alert.setOnShowListener(dialog -> {

            Button btnPositive = alert.getButton(Dialog.BUTTON_POSITIVE);
            btnPositive.setTextSize(22);

            Button btnNegative = alert.getButton(Dialog.BUTTON_NEGATIVE);
            btnNegative.setTextSize(22);
            Button neuButton = alert.getButton(android.content.DialogInterface.BUTTON_NEUTRAL);

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
            neuParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            neuParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            neuButton.setVisibility(View.GONE);

        });
        alert.create();
    }


    @Override
    public void onBackPressed() {
        alert.show();
    }
}
