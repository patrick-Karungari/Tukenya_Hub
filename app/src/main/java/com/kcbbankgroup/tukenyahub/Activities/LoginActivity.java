package com.kcbbankgroup.tukenyahub.Activities;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.kcbbankgroup.tukenyahub.Modules.Constants;
import com.kcbbankgroup.tukenyahub.R;
import com.kcbbankgroup.tukenyahub.Modules.RequestHandler;
import com.kcbbankgroup.tukenyahub.Modules.SharedPrefManager;
import com.shreyaspatil.MaterialDialog.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.os.Build.VERSION.SDK_INT;
import static android.view.View.VISIBLE;

public class LoginActivity extends AppCompatActivity {
    private static final int MY_SOCKET_TIMEOUT_MS = 15000;
    EditText uniNum;
    EditText password;
    String userEmail, userPassword;
    RelativeLayout rootView;
    JSONObject obj;
    Button btnLogin;
    RelativeLayout afteranimationView;
    Context mContext;
    LinearLayout progressBarHolder;
    boolean isConnected;
    ImageView bookIconImageView;
    ProgressDialog progressDialog;
    TextView bookITextView, signUp, welcomeback;
    ProgressBar loadingProgressBar;
    AlertDialog.Builder builder;
    AlertDialog alert;
    MaterialDialog mDialog, mDialog3, mDialog2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_login);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();
        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (!(hasFocus)) {
            return;
        }

        initializeUI();


    }

    private void initializeUI() {
        Thread thread = new Thread(() -> {
            LoginActivity.this.runOnUiThread(() -> {
                alertBuilder();
                preLogin();
            });
            isNetworkConnected();
        });
        thread.start();

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        uniNum = findViewById(R.id.uniNum);
        bookITextView = findViewById(R.id.bookITextView);
        password = findViewById(R.id.password);
        afteranimationView = findViewById(R.id.afterAnimationView);
        rootView = findViewById(R.id.rootView);
        bookIconImageView = findViewById(R.id.bookIconImageView);
        btnLogin = findViewById(R.id.loginButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        signUp = findViewById(R.id.signUpFragment);
        btnLogin.setOnClickListener(view -> LoginActivity.this.OpenMainActivity());
        signUp.setOnClickListener(this::OpenSignupPage);
        progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Authenticating...");
    }

    private void preLogin() {
        new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                bookITextView.setVisibility(View.GONE);
                loadingProgressBar.setVisibility(View.GONE);

                startAnimation(bookIconImageView);
            }
        }.start();

    }

    private void startAnimation(View bookIconImageView) {
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(bookIconImageView, "y", 100f);
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(bookIconImageView, "x", 50f);
        animatorY.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animatorY, animatorX);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                afteranimationView.setVisibility(VISIBLE);
                rootView.setBackgroundColor(ContextCompat.getColor(LoginActivity.this, R.color.colorSplashText));
                bookITextView = findViewById(R.id.unilogo);
                welcomeback = findViewById(R.id.WelcomeTextView);

            }

        });
        animatorSet.start();
    }


    public void fyl(Context mContext) {
        this.mContext = mContext;
    }

    public boolean isNetworkConnected() {
        fyl(getApplicationContext());
        final ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (cm != null) {
            if (SDK_INT < 23) {
                final NetworkInfo ni = cm.getActiveNetworkInfo();

                if (ni != null) {

                    return isConnected = (ni.isConnected() && (ni.getType() == ConnectivityManager.TYPE_WIFI || ni.getType() == ConnectivityManager.TYPE_MOBILE));

                } else {
                    final Network n = cm.getActiveNetwork();

                    if (n != null) {
                        final NetworkCapabilities nc = cm.getNetworkCapabilities(n);

                        assert nc != null;
                        return isConnected = (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI));

                    }
                }
            }
        }
        return isConnected;
    }

    public void OpenSignupPage(View view) {
        startActivity(new Intent(LoginActivity.this, SignupActivity.class));
    }

    @SuppressLint("RestrictedApi")
    public void OpenMainActivity() {
        try {

            boolean STATE = isNetworkConnected();
            if (STATE) {
                userEmail = uniNum.getText().toString();
                userPassword = password.getText().toString();
                //login user
                if (TextUtils.isEmpty(userEmail)) {
                    Toast.makeText(getApplicationContext(), "Enter registration number!", Toast.LENGTH_SHORT).show();
                    uniNum.setError("Registration Number Required.");
                    uniNum.setText("");
                    password.setError("Password Required.");
                    password.setText("");
                    uniNum.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(userPassword)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    uniNum.setError("Registration Number Required.");
                    uniNum.setText("");
                    password.setError("Password Required.");
                    password.setText("");
                    uniNum.requestFocus();
                    return;
                }
                userLogin();

            } else {

                mDialog2.show();
            }


        } catch (Exception e) {
            Log.e("YOUR_APP_LOG_TAG", "I got an error", e);

        }

    }

    private void userLogin() {

        final String username = uniNum.getText().toString().trim();
        final String pWord = password.getText().toString().trim();

        progressDialog.show();

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                Constants.URL_LOGIN,
                response -> {
                    progressDialog.dismiss();
                    try {
                        obj = new JSONObject(response);

                        if (!obj.getBoolean("error")) {
                            SharedPrefManager.getInstance(getApplicationContext())
                                    .userLogin(
                                            obj.getInt("id"),
                                            obj.getString("username"),
                                            obj.getString("email"),
                                            obj.getString("uniNum"),
                                            obj.getString("imageData")
                                    );
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        } else {
                            String code = obj.getString("code");
                            switch (code) {
                                case "1":
                                    Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_LONG).show();
                                    uniNum.setError(obj.getString("message"));
                                    uniNum.setText("");
                                    password.setError(obj.getString("message"));
                                    password.setText("");
                                    uniNum.requestFocus();
                                    break;
                                case "2":
                                    Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_LONG).show();
                                    uniNum.setError("Password Required.");
                                    uniNum.setText("");
                                    password.setError("Registration Number Required.");
                                    password.setText("");
                                    uniNum.requestFocus();
                                    break;
                                case "3":
                                    mDialog3 = new MaterialDialog.Builder(LoginActivity.this)
                                            .setMessage(obj.getString("message"))
                                            .setTitle(obj.getString("title"))
                                            .setCancelable(true)
                                            .setAnimation("Timeout.json")
                                            .setPositiveButton("OK", (dialogInterface, which) -> dialogInterface.dismiss())
                                            .build();
                                    mDialog3.show();
                                    break;
                            }
                        }
                    } catch (JSONException e) {
                        Log.i("Server Error", "[" + response + "]");
                        e.printStackTrace();
                        mDialog.show();
                    }
                },
                (VolleyError error) -> {
                    mDialog.show();
                    progressDialog.dismiss();


                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("uniNum", username);
                params.put("password", pWord);
                return params;
            }

        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    @Override
    public void onBackPressed() {
        alert.show();
    }

    private void alertBuilder() {
        android.content.DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case android.content.DialogInterface.BUTTON_POSITIVE:
                    SharedPrefManager.getInstance(getApplicationContext()).logout();

                    System.exit(0);
                    finishAndRemoveTask();
                    //android.os.Process.killProcess(android.os.Process.myPid());
                    break;

                case android.content.DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };
        builder = new AlertDialog.Builder(LoginActivity.this);
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
        mDialog = new MaterialDialog.Builder(LoginActivity.this)
                .setMessage("Some error occurred please try again later.")
                .setTitle("Server Error")
                .setCancelable(true)
                .setAnimation("Timeout.json")
                .setPositiveButton("OK", (dialogInterface, which) -> dialogInterface.dismiss())
                .build();

        mDialog2 = new MaterialDialog.Builder(this)
                .setMessage("Seems you are offline.\nPlease check your internet connection and try again.")
                .setCancelable(true)
                .setAnimation("no_internet_connection.json")
                .setPositiveButton("OK", (dialogInterface, which) -> dialogInterface.dismiss())
                .build();
    }


}
