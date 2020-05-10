package com.kcbbankgroup.tukenyahub.Activities;


import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.kcbbankgroup.tukenyahub.Modules.Constants;
import com.kcbbankgroup.tukenyahub.Modules.Permission.SampleErrorListener;
import com.kcbbankgroup.tukenyahub.Modules.Permission.SampleMultiplePermissionListener;
import com.kcbbankgroup.tukenyahub.Modules.RequestHandler;
import com.kcbbankgroup.tukenyahub.Modules.ScreenRotation;
import com.kcbbankgroup.tukenyahub.Modules.SharedPrefManager;
import com.kcbbankgroup.tukenyahub.R;
import com.shreyaspatil.MaterialDialog.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.os.Build.VERSION.SDK_INT;
import static android.view.View.VISIBLE;

public class LoginActivity extends AppCompatActivity {
    private static final int MY_SOCKET_TIMEOUT_MS = 15000;
    public static final int REQUEST_CODE = 1;
    String requiredValue;
    EditText uniNum;
    EditText password;
    String userReg, userPassword;
    RelativeLayout rootView;
    JSONObject obj;
    CardView btnLogin;
    RelativeLayout afteranimationView;
    Context mContext;
    LinearLayout linear_layout;
    boolean isConnected;
    ImageView bookIconImageView;
    ProgressDialog progressDialog;
    TextView motto, signUp, welcomeback;
    ProgressBar loadingProgressBar;
    AlertDialog.Builder builder;
    AlertDialog alert, dialog;
    MaterialDialog mDialog, mDialog3, mDialog2;


    private MultiplePermissionsListener allPermissionsListener;
    private PermissionRequestErrorListener errorListener;
    @BindView(android.R.id.content)
    View contentView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        new ScreenRotation(getApplicationContext(), this);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        createPermissionListeners();
        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void requestStoragePermission() {
        new Thread(Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA)
                .withListener(allPermissionsListener)
                .withErrorListener(errorListener)::check)
                .start();
    }

    private void createPermissionListeners() {
        MultiplePermissionsListener feedbackViewMultiplePermissionListener =
                new SampleMultiplePermissionListener(this);

        allPermissionsListener =
                new CompositeMultiplePermissionsListener(feedbackViewMultiplePermissionListener);
        errorListener = new SampleErrorListener();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void showPermissionRationale(final PermissionToken token) {
        dialog = new AlertDialog.Builder(this).setTitle(R.string.permission_rationale_title)
                .setMessage(R.string.permission_rationale_message)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        token.cancelPermissionRequest();
                    }
                })
                .setPositiveButton(R.string.permission_rationale_settings_button_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, 101);
                        token.continuePermissionRequest();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        token.cancelPermissionRequest();
                    }
                })
                .create();
    }

    public boolean showPermissionGranted(String permission) {

        //startActivityForResult(new Intent(LoginActivity.this, SignupActivity.class), REQUEST_CODE);
        return true;
    }

    public void showPermissionDenied(String permission, boolean isPermanentlyDenied) {
        dialog.show();
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
                if (requiredValue == null) {
                    preLogin();
                    requiredValue = null;
                }
            });
            isNetworkConnected();

        });
        thread.start();

        linear_layout = findViewById(R.id.linear_layout);
        welcomeback = findViewById(R.id.WelcomeTextView);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        uniNum = findViewById(R.id.username);
        motto = findViewById(R.id.motto);
        password = findViewById(R.id.password);
        afteranimationView = findViewById(R.id.afterAnimationView);
        rootView = findViewById(R.id.rootView);
        bookIconImageView = findViewById(R.id.bookIconImageView);
        btnLogin = findViewById(R.id.cardView);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        signUp = findViewById(R.id.register_here);
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
                welcomeback.setVisibility(View.GONE);

            }

            public void onFinish() {
                loadingProgressBar.setVisibility(View.GONE);
                startAnimation(linear_layout);
            }
        }.start();

    }

    private void startAnimation(View bookIconImageView) {
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(bookIconImageView, "y", 150f);

        animatorY.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animatorY);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                afteranimationView.setVisibility(VISIBLE);
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
        String permission = " ";
        if (showPermissionGranted(permission)) {
            startActivityForResult(new Intent(LoginActivity.this, SignupActivity.class), REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

                requiredValue = data.getStringExtra("key");
                userReg = data.getStringExtra("regNum");
                userPassword = data.getStringExtra("password");
                uniNum.setText(userReg);
                password.setText(userPassword);
            }
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.toString(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("RestrictedApi")
    public void OpenMainActivity() {
        try {

            boolean STATE = isNetworkConnected();
            if (STATE) {
                userReg = uniNum.getText().toString();
                userPassword = password.getText().toString();
                //login user
                if (TextUtils.isEmpty(userReg)) {
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
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
                //userLogin();

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
