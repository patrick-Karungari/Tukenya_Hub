package com.patrickarungari.tukenyahub.Activities;


import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.JsonElement;
import com.patrickarungari.sharedPrefManager.SharedPrefManager;
import com.patrickarungari.tukenyahub.Modules.ScreenRotation;
import com.patrickarungari.tukenyahub.Modules.TukenyaCustomApplication;
import com.patrickarungari.tukenyahub.R;
import com.patrickarungari.tukenyahub.ViewModel.LoginViewModel;
import com.patrickarungari.tukenyahub.databinding.ActivityLoginBinding;
import com.patrickarungari.tukenyahub.utils.ApiResponse;
import com.patrickarungari.tukenyahub.utils.AppConstants;
import com.pushlink.android.PushLink;
import com.shreyaspatil.MaterialDialog.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import spencerstudios.com.bungeelib.Bungee;

import static android.view.View.VISIBLE;

@RuntimePermissions
public class LoginActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 1;
    String requiredValue;
    EditText uniNum;
    EditText password;
    String userReg, userPassword;
    RelativeLayout rootView;
    JSONObject obj;
    ActivityLoginBinding loginBinding;

    RelativeLayout afteranimationView;
    LinearLayout linear_layout;
    ImageView bookIconImageView;
    ProgressDialog progressDialog;
    TextView motto, signUp, welcomeback;
    ProgressBar loadingProgressBar;
    AlertDialog.Builder builder;
    AlertDialog alert, dialog;
    MaterialDialog mDialog, mDialog3, mDialog2;
    @BindView(android.R.id.content)
    View contentView;
    @Inject
    com.patrickarungari.tukenyahub.utils.viewModelFactory viewModelFactory;

    @BindView(R.id.username)
    EditText username;

    @BindView(R.id.password)
    EditText pass;

    LoginViewModel viewModel;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new ScreenRotation(getApplicationContext(), this);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        loginBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(loginBinding.getRoot());
        ButterKnife.bind(this);
        initializeUI();
        ((TukenyaCustomApplication) getApplication()).getAppComponent().doInjection(this);
        viewModel = new ViewModelProvider(this, viewModelFactory).get(LoginViewModel.class);
        viewModel.loginResponse().observe(this, this::consumeResponse);

        loginBinding.signin.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoginClicked();
            }
        });
    }


    void onLoginClicked() {
        if (isValid()) {
            if (!AppConstants.checkInternetConnection(this)) {
                mDialog2.show();
            } else {
                viewModel.hitLoginApi(uniNum.getText().toString(), pass.getText().toString());
            }
        }
    }

    private boolean isValid() {
        if (username.getText().toString().trim().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Enter registration number!", Toast.LENGTH_SHORT).show();
            uniNum.setError("Registration Number Required.");
            uniNum.setText("");
            password.setError("Password Required.");
            password.setText("");
            uniNum.requestFocus();
            return false;
        } else if (password.getText().toString().trim().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            uniNum.setError("Registration Number Required.");
            uniNum.setText("");
            password.setError("Password Required.");
            password.setText("");
            uniNum.requestFocus();
            return false;
        }

        return true;
    }


    private void initializeUI() {
        LoginActivity.this.alertBuilder();

        linear_layout = findViewById(R.id.linear_layout);
        welcomeback = findViewById(R.id.WelcomeTextView);
        uniNum = findViewById(R.id.username);
        motto = findViewById(R.id.motto);
        password = findViewById(R.id.password);
        afteranimationView = findViewById(R.id.afterAnimationView);
        rootView = findViewById(R.id.rootView);
        bookIconImageView = findViewById(R.id.bookIconImageView);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        signUp = findViewById(R.id.register_here);
        signUp.setOnClickListener(LoginActivity.this::OpenSignupPage);
        progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Authenticating...");
        boolean log = SharedPrefManager.getInstance(LoginActivity.this).isloginBefore();
        if (!log) {
            LoginActivity.this.preLogin();
        } else {
            loadingProgressBar.setVisibility(View.GONE);
            startAnimation(linear_layout);
        }

    }

    private void preLogin() {
        new CountDownTimer(3000, 1000) {
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
                getPermission();
            }

        });
        animatorSet.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PushLink.setCurrentActivity(this);
    }

    public void OpenSignupPage(View view) {
        startActivityForResult(new Intent(LoginActivity.this, SignupActivity.class), REQUEST_CODE);
        Bungee.fade(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            if (resultCode == RESULT_OK) {
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
                .setMessage("We probably haven't paid for Wi-Fi.\nWe'll be back in a few.\nPlease try again later.")
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

    void getPermission() {
        LoginActivityPermissionsDispatcher.needsPermissionWithPermissionCheck(this);
        //needsPermission();
    }

    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void needsPermission() {

    }

    @OnShowRationale({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void onShowRationale(final PermissionRequest request) {
        permissionsRationale(request);
    }

    @OnPermissionDenied({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void onPermissionDenied() {
        //permissionsDenied();
    }

    @OnNeverAskAgain({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void onNeverAskAgain() {
        permissionsDenied();

    }

    public void permissionsRationale(final PermissionRequest request) {
        dialog = new AlertDialog.Builder(this).setTitle(R.string.permission_rationale_title)
                .setMessage(R.string.all_permissions_denied_feedback)
                .setPositiveButton(R.string.permission_rationale_settings_button_text, (dialog, which) -> {
                    dialog.dismiss();
                    request.proceed();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, 101);
                    //token.continuePermissionRequest();
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        //token.cancelPermissionRequest();
                    }
                })
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    public void permissionsDenied() {
        dialog = new AlertDialog.Builder(this).setTitle(R.string.permission_rationale_title)
                .setMessage(R.string.permission_rationale_message)
                .setPositiveButton(R.string.permission_rationale_settings_button_text, (dialog, which) -> {
                    dialog.dismiss();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, 101);
                    //token.continuePermissionRequest();
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        //token.cancelPermissionRequest();
                    }
                })
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
    }

    private void consumeResponse(ApiResponse apiResponse) {

        switch (apiResponse.status) {

            case LOADING:
                progressDialog.show();
                break;

            case SUCCESS:
                renderSuccessResponse(apiResponse.data);
                break;

            case ERROR:
                progressDialog.dismiss();
                mDialog.show();
                Log.d("response=", apiResponse.error.getMessage());
                break;

            default:
                progressDialog.dismiss();
                mDialog.show();
                break;
        }
    }

    public void setError(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        uniNum.setError(message);
        uniNum.setText("");
        password.setError(message);
        password.setText("");
        uniNum.requestFocus();
    }

    private void renderSuccessResponse(JsonElement response) {
        if (!(response.isJsonNull())) {
            Log.d("Login Response=", response.toString());
            try {
                obj = new JSONObject(String.valueOf(response.getAsJsonObject()));
                if (!obj.getBoolean("error")) {
                    SharedPrefManager.getInstance(getApplicationContext())
                            .userLogin(
                                    obj.getInt("id"),
                                    obj.getString("username"),
                                    obj.getString("email"),
                                    obj.getString("uniNum"),
                                    obj.getString("imagePath"),
                                    pass.getText().toString().trim()
                            );
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    progressDialog.dismiss();

                    Bungee.card(this);
                } else {
                    String code = obj.getString("code");
                    progressDialog.dismiss();
                    switch (code) {
                        case "1":
                            setError(obj.getString("message"));
                            break;
                        case "2":
                            setError(obj.getString("message"));
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
        } else {
            progressDialog.dismiss();
            Log.d("response=", response.toString());
            mDialog.show();
        }
    }


}
