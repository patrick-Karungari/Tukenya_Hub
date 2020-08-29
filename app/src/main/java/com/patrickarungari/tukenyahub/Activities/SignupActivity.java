package com.patrickarungari.tukenyahub.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.anstrontechnologies.corehelper.AnstronCoreHelper;
import com.google.gson.JsonElement;
import com.iceteck.silicompressorr.FileUtils;
import com.iceteck.silicompressorr.SiliCompressor;
import com.nightlynexus.viewstatepageradapter.ViewStatePagerAdapter;
import com.patrickarungari.sharedPrefManager.SharedPrefManager;
import com.patrickarungari.tukenyahub.Modules.PasswordStrength;
import com.patrickarungari.tukenyahub.Modules.RegisterationNumberValidator;
import com.patrickarungari.tukenyahub.Modules.ScreenRotation;
import com.patrickarungari.tukenyahub.Modules.TukenyaCustomApplication;
import com.patrickarungari.tukenyahub.R;
import com.patrickarungari.tukenyahub.ViewModel.LoginViewModel;
import com.patrickarungari.tukenyahub.utils.ApiResponse;
import com.patrickarungari.tukenyahub.utils.AppConstants;
import com.pushlink.android.PushLink;
import com.shreyaspatil.MaterialDialog.MaterialDialog;
import com.yalantis.ucrop.UCrop;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;
import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;


@RuntimePermissions
public class SignupActivity extends AppCompatActivity {

    EditText password, regNum, name, email;
    TextView button, greetings, signUp_title;
    private ViewPager viewPager;
    private LinearLayout dotsLayout;
    private int[] layouts;
    JSONObject jsonObject;
    String code = "null";
    String strength;
    AlertDialog.Builder builder;
    AlertDialog alert;
    private CircleImageView imageView;
    AnstronCoreHelper coreHelper;
    private Bitmap bitmap;
    ConstraintLayout frame;
    private ProgressDialog progressDialog;
    private MaterialDialog mDialog2;
    private MaterialDialog mDialog;
    LoginViewModel viewModel;
    @Inject
    com.patrickarungari.tukenyahub.utils.viewModelFactory viewModelFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new ScreenRotation(getApplicationContext(), this);
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
        ((TukenyaCustomApplication) getApplication()).getAppComponent().doInjection(this);
        viewModel = new ViewModelProvider(this, viewModelFactory).get(LoginViewModel.class);
        viewModel.RegisterResponse().observe(this, this::consumeResponse);
        Thread thread = new Thread(() -> {
            SignupActivity.this.runOnUiThread(this::alertBuilder);
            initUi();
        });
        thread.start();

    }
    @Override
    protected void onResume() {
        super.onResume();
        PushLink.setCurrentActivity(this);
    }
    private void initUi() {
        frame = findViewById(R.id.frame);
        viewPager = findViewById(R.id.view_pager);
        button = findViewById(R.id.button);
        dotsLayout = findViewById(R.id.layoutDots);
        CardView btnNext = findViewById(R.id.card_button);
        layouts = new int[]{
                R.layout.personal_details1,
                R.layout.personal_details2};
        // adding bottom dots
        addBottomDots(0);
        coreHelper = new AnstronCoreHelper(this);
        MyViewPagerAdapter myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        signUp_title = findViewById(R.id.signUp_title);
        btnNext.setOnClickListener(v -> {
            int current = getItem();
            if (current < layouts.length) {
                // move to next screen
                viewPager.setCurrentItem(current, false);
            } else {
                launchSignInScreen();
            }
        });
    }

    //	viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);
            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.length - 1) {
                // last page. make button text to GOT IT
                button.setText(getString(R.string.start));

            } else {
                // still pages are left
                button.setText(getString(R.string.next));
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    private void addBottomDots(int currentPage) {
        TextView[] dots = new TextView[layouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

    private int getItem() {
        return viewPager.getCurrentItem() + 1;
    }

    private boolean validateForm() {
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        regNum = findViewById(R.id.regNum);
        password = findViewById(R.id.password_reg);
        name.requestFocus();
        email.clearFocus();
        password.clearFocus();
        regNum.clearFocus();
        boolean isValid = true;

        if (TextUtils.isEmpty(name.getText().toString()) || TextUtils.isEmpty(email.getText().toString())) {
            viewPager.setCurrentItem(0, false);
            if (TextUtils.isEmpty(name.getText().toString())) {
                name.requestFocus();
                isValid = false;
            }
            if (TextUtils.isEmpty(email.getText().toString())) {
                email.requestFocus();
                isValid = false;
            }
        } else {
            if (TextUtils.isEmpty(regNum.getText().toString())) {
                regNum.requestFocus();
                isValid = false;
            }
            if (TextUtils.isEmpty(password.getText().toString())) {
                password.requestFocus();
                isValid = false;
            }
        }
        if (bitmap == null) {
            Toast.makeText(Objects.requireNonNull(this).getApplicationContext(), "Please select Image", Toast.LENGTH_LONG).show();
            isValid = false;

        }
        if (!isValidEmailId(email.getText().toString().trim())) {
            Toast.makeText(getApplicationContext(), "Invalid Email Address.", Toast.LENGTH_SHORT).show();
            viewPager.setCurrentItem(0, false);
            email.requestFocus();
            isValid = false;
        }
        return isValid;
    }

    private boolean calculateRegistrationNumberStrength(String toString) {
        RegisterationNumberValidator passwordStrength = RegisterationNumberValidator.calculate(toString);
        boolean strength = false;
        int code = passwordStrength.code;
        if (code == 1){
            strength = true;
        }
        return strength;
    }

    private void launchSignInScreen() {
        if (validateForm()) {
            boolean connection = AppConstants.checkInternetConnection(this);
            if (!connection) {
                mDialog2.show();
            } else {
                if (strength == null || (strength.equals("weak") || strength.equals("medium"))) {
                    Toast.makeText(getApplicationContext(), "Choose a stronger password.", Toast.LENGTH_SHORT).show();
                }
                else  if (!calculateRegistrationNumberStrength(regNum.getText().toString().trim())) {
                    Toast.makeText(getApplicationContext(), "Choose a valid registration number", Toast.LENGTH_SHORT).show();
                }
                else{
                    final String imageStr = getStringImage(bitmap);
                    viewModel.hitRegisterApi(email.getText().toString().trim(),
                            password.getText().toString().trim(),
                            regNum.getText().toString().trim(),
                            name.getText().toString().trim(),
                            imageStr);
                }
            }
        }
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
                Toast.makeText(getApplicationContext(), apiResponse.error.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("response=", Objects.requireNonNull(apiResponse.error.getMessage()));
                break;

            default:
                mDialog.show();
                break;
        }
    }

    private void renderSuccessResponse(JsonElement response) {
        if (!(response.isJsonNull())) {
            Log.d("Login Response=", response.toString());
            try {
                jsonObject = new JSONObject(String.valueOf(response.getAsJsonObject()));
                code = jsonObject.getString("code");
                switch (code) {
                    case "0":
                        Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                        String keyPass = password.getText().toString();
                        String keyReg = regNum.getText().toString();
                        deleteCache(this);
                        Intent intent = getIntent();
                        intent.putExtra("regNum", keyReg);
                        intent.putExtra("password", keyPass);

                        SharedPrefManager.getInstance(this).loginBefore(true);
                        setResult(RESULT_OK, intent);
                        progressDialog.dismiss();
                        finish();
                        break;
                    case "2":
                        progressDialog.dismiss();
                        setError(jsonObject.getString("message"));
                        break;
                    case "1":
                        progressDialog.dismiss();
                        MaterialDialog mDialog3 = new MaterialDialog.Builder(this)
                                .setMessage(jsonObject.getString("message"))
                                .setTitle("ERROR")
                                .setCancelable(true)
                                .setAnimation("Timeout.json")
                                .setPositiveButton("OK", (dialogInterface, which) -> dialogInterface.dismiss())
                                .build();
                        mDialog3.show();
                        break;
                }
            } catch (JSONException e) {
                progressDialog.dismiss();
                e.printStackTrace();
                Log.i("Server Error", "[" + response + "]");
            }
        }
    }

    public void setError(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        email.setError("Email in use by another user.");
        regNum.setText("");
        regNum.setError("Registration number in use by another user.");
        email.setText("");
        email.requestFocus();

    }




    private void alertBuilder() {
        progressDialog = new ProgressDialog(this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Registering ...");
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
        builder = new AlertDialog.Builder(this);
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
        mDialog = new MaterialDialog.Builder(this)
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = getIntent();
        setResult(RESULT_OK, intent);

    }

    @SuppressLint("IntentReset")
    private void showFileChooser() {

        new Thread(() -> {
            final CharSequence[] options = {"Choose from Gallery", "Cancel"};
            SignupActivity.this.runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                builder.setTitle("Choose your profile picture");

                builder.setItems(options, (dialog, item) -> {

                    if (options[item].equals("Take Photo")) {
                        Intent takePicture = new Intent(ACTION_IMAGE_CAPTURE);
                        startActivityForResult(takePicture, 0);

                    } else if (options[item].equals("Choose from Gallery")) {
                        @SuppressLint("IntentReset") Intent pickPhoto = new Intent(Intent.ACTION_PICK, EXTERNAL_CONTENT_URI);
                        pickPhoto.setType("image/*");
                        startActivityForResult(pickPhoto, 1);

                    } else if (options[item].equals("Cancel")) {
                        dialog.dismiss();
                    }
                });
                builder.create();
                builder.show();
            });


        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        bitmap = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                        imageView.setImageBitmap(bitmap);

                    }

                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        try {
                            Uri sourceUri = data.getData(); // 1
                            File file = getImageFile(); // 2
                            Uri destinationUri = Uri.fromFile(file);  // 3
                            openCropActivity(sourceUri, destinationUri);  // 4

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = UCrop.getOutput(data);
                showImage(uri);
            }
        }

    }

    private void showImage(Uri imageUri) {
        try {
            compressImage(imageUri);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Please select different profile picture.", Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.Options options = new UCrop.Options();
        options.setCircleDimmedLayer(true);
        options.setCropFrameColor(this.getColor(R.color.colorAccent));
        UCrop.of(sourceUri, destinationUri)
                .start(this);

    }

    private File getImageFile() throws IOException {
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        File storageDir = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM
                ), "Camera"
        );
        System.out.println(storageDir.getAbsolutePath());
        if (storageDir.exists())
            System.out.println("File exists");
        else
            System.out.println("File not exists");
        return File.createTempFile(
                imageFileName, ".jpg", storageDir
        );
    }

    public static void deleteCache(Context context) {
        File dir = context.getCacheDir();
        deleteDir(dir);
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    private void compressImage(Uri imageUri) {

        final Bitmap imageBitmap;
        try {
            imageBitmap = SiliCompressor.with(SignupActivity.this)
                    .getCompressBitmap(FileUtils.getPath(SignupActivity.this, imageUri), false);
            String size = AnstronCoreHelper.getReadableFileSize(imageBitmap.getAllocationByteCount());
            bitmap = imageBitmap;
            Toast.makeText(SignupActivity.this, size, Toast.LENGTH_LONG).show();
            imageView.setImageBitmap(imageBitmap);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    public void onBackPressed(View view) {
        onBackPressed();
    }

    private boolean isValidEmailId(String email) {

        return Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$").matcher(email).matches();
    }

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showGallery() {
        showFileChooser();
    }

    void getPermission() {
        SignupActivityPermissionsDispatcher.showGalleryWithPermissionCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SignupActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showGalleryRationale(final PermissionRequest request) {
        permissionsRationale(request);
    }

    @OnPermissionDenied({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void galleryDenied() {
        permissionsDenied();
    }

    @OnNeverAskAgain({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void onNeverAskAgain() {
        permissionsDenied();
    }

    public void permissionsRationale(final PermissionRequest request) {
        AlertDialog mdialog = new AlertDialog.Builder(this).setTitle(R.string.permission_rationale_title)
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
                .setOnDismissListener(dialog -> {
                    //token.cancelPermissionRequest();
                })
                .create();
        mdialog.setCanceledOnTouchOutside(false);
        mdialog.setCancelable(false);
        mdialog.show();
    }

    public void permissionsDenied() {
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.permission_rationale_title)
                .setMessage(R.string.permission_rationale_message)
                .setPositiveButton(R.string.permission_rationale_settings_button_text, (mdialog, which) -> {
                    mdialog.dismiss();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, 101);
                    //token.continuePermissionRequest();
                })
                .setOnDismissListener(dialog1 -> {
                    //token.cancelPermissionRequest();
                })
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
    }

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends ViewStatePagerAdapter {
        TextView passwordStrengthXML;
        CardView cardsweak, strong, medium, very_strong;

        MyViewPagerAdapter() {
        }

        @Override
        protected View createView(ViewGroup container, int position) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //View view = LayoutInflater.from(context).inflate(layouts[position], container, false);
            View view = Objects.requireNonNull(layoutInflater).inflate(layouts[position], container, false);
            greetings = findViewById(R.id.greeting);
            LinearLayout root = view.findViewById(R.id.root);
            password = view.findViewById(R.id.password_reg);
            cardsweak = view.findViewById(R.id.weak);
            strong = view.findViewById(R.id.strong);
            medium = view.findViewById(R.id.medium);
            very_strong = view.findViewById(R.id.very_strong);
            Calendar calendar = Calendar.getInstance();
            int time_of_day = calendar.get(Calendar.HOUR_OF_DAY);

            if (position == 1) {
                if (time_of_day < 12) {
                    greetings.setText(R.string.good_morning);
                    frame.setBackground(getDrawable(R.drawable.good_morning_img));
                } else if (time_of_day < 16) {
                    greetings.setText(R.string.good_afternoon);
                } else {
                    greetings.setText(R.string.good_evening);
                    frame.setBackground(getDrawable(R.drawable.good_night_img));
                }
                passwordStrengthXML = view.findViewById(R.id.passwordStrength);
                imageView = view.findViewById(R.id.profile_image);
                password.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        root.setVisibility(View.VISIBLE);
                        calculatePasswordStrength(s.toString());
                        // calculateRegistrationNumberStrength(s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                imageView.setOnClickListener(v -> getPermission());
            }
            return view;
        }

        private void calculatePasswordStrength(String str) {
            PasswordStrength passwordStrength = PasswordStrength.calculate(str);
            passwordStrengthXML.setText(passwordStrength.msg);
            int code = passwordStrength.code;
            switch (code) {
                //weak
                case 1:
                    cardsweak.setBackgroundColor(passwordStrength.color);
                    medium.setBackgroundResource(R.color.cardBackground);
                    strong.setBackgroundResource(R.color.cardBackground);
                    very_strong.setBackgroundResource(R.color.cardBackground);
                    passwordStrengthXML.setTextColor(passwordStrength.color);
                    strength = "weak";
                    break;
                //medium
                case 2:
                    cardsweak.setBackgroundColor(passwordStrength.color);
                    medium.setBackgroundColor(passwordStrength.color);
                    strong.setBackgroundResource(R.color.cardBackground);
                    very_strong.setBackgroundResource(R.color.cardBackground);
                    passwordStrengthXML.setTextColor(passwordStrength.color);
                    strength = "medium";
                    break;
                //strong
                case 3:
                    very_strong.setBackgroundResource(R.color.cardBackground);
                    cardsweak.setBackgroundColor(passwordStrength.color);
                    medium.setBackgroundColor(passwordStrength.color);
                    strong.setBackgroundColor(passwordStrength.color);
                    passwordStrengthXML.setTextColor(passwordStrength.color);
                    strength = "strong";
                    break;
                //verStrong
                case 4:
                    cardsweak.setBackgroundColor(passwordStrength.color);
                    medium.setBackgroundColor(passwordStrength.color);
                    strong.setBackgroundColor(passwordStrength.color);
                    very_strong.setBackgroundColor(passwordStrength.color);
                    passwordStrengthXML.setTextColor(passwordStrength.color);
                    strength = "very strong";
                    break;
                default:
            }
        }

        @Override
        public int getCount() {
            return layouts.length;
        }
    }


}
