package com.kcbbankgroup.tukenyahub.Activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.anstrontechnologies.corehelper.AnstronCoreHelper;
import com.iceteck.silicompressorr.FileUtils;
import com.iceteck.silicompressorr.SiliCompressor;
import com.kcbbankgroup.tukenyahub.Modules.Constants;
import com.kcbbankgroup.tukenyahub.Modules.RequestHandler;
import com.kcbbankgroup.tukenyahub.Modules.ScreenRotation;
import com.kcbbankgroup.tukenyahub.R;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.nightlynexus.viewstatepageradapter.ViewStatePagerAdapter;
import com.shreyaspatil.MaterialDialog.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import butterknife.ButterKnife;

import static android.os.Build.VERSION.SDK_INT;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity: ";
    EditText password, regNum, name, email;
    TextView button, greetings, signUp_title;
    private RelativeLayout frame;
    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    private CardView btnNext;
    AlertDialog.Builder builder;
    AlertDialog alert;
    private String keyPass, keyReg;
    private CircularImageView imageView;
    AnstronCoreHelper coreHelper;
    private Uri filepath;
    private Bitmap bitmap;
    private static final int MY_SOCKET_TIMEOUT_MS = 15000;
    private boolean isConnected;
    private ProgressDialog progressDialog;
    private MaterialDialog mDialog2;
    private MaterialDialog mDialog;
    String code;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ScreenRotation(getApplicationContext(), this);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
        Thread thread = new Thread(() -> {
            SignupActivity.this.runOnUiThread(this::alertBuilder);
            initUi();
        });
        thread.start();

    }

    private void initUi() {
        frame = findViewById(R.id.frame);
        viewPager = findViewById(R.id.view_pager);
        button = findViewById(R.id.button);
        dotsLayout = findViewById(R.id.layoutDots);
        btnNext = findViewById(R.id.card_button);
        // layouts of all welcome sliders
        // add few more layouts if you want
        layouts = new int[]{
                R.layout.personal_details1,
                R.layout.personal_details2};

        // adding bottom dots
        addBottomDots(0);
        coreHelper = new AnstronCoreHelper(this);
        myViewPagerAdapter = new MyViewPagerAdapter(getApplicationContext());
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        greetings = findViewById(R.id.greetings);
        signUp_title = findViewById(R.id.signUp_title);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking for last page
                // if last page home screen will be launched
                int current = getItem(+1);
                if (current < layouts.length) {
                    // move to next screen
                    viewPager.setCurrentItem(current, false);
                } else {
                    launchSignInScreen();
                }
            }
        });
        Calendar calendar = Calendar.getInstance();
        int time_of_day = calendar.get(Calendar.HOUR_OF_DAY);
        if (time_of_day < 12) {
            greetings.setText("Good Morning");
            frame.setBackground(getDrawable(R.drawable.good_morning_img));
        } else if (time_of_day >= 12 && time_of_day < 16) {
            greetings.setText("Good Afternoon");
        } else {
            greetings.setText("Good Evening");
            frame.setBackground(getDrawable(R.drawable.good_night_img));
        }

    }

    //	viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);
            if (position == 1) {
                greetings.setVisibility(View.GONE);
                signUp_title.setVisibility(View.GONE);

            } else {
                greetings.setVisibility(View.VISIBLE);
                signUp_title.setVisibility(View.VISIBLE);
            }
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
        dots = new TextView[layouts.length];

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

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    private boolean validateForm() {
        boolean result = true;
        if (TextUtils.isEmpty(email.getText().toString())) {
            email.setError("Required");
            result = false;
        } else {
            email.setError(null);
        }
        if (TextUtils.isEmpty(password.getText().toString())) {
            password.setError("Required");
            result = false;
        } else {
            password.setError(null);
        }
        if (TextUtils.isEmpty(regNum.getText().toString())) {
            regNum.setError("Required");
            result = false;
        } else {
            regNum.setError(null);
        }
        if (TextUtils.isEmpty(name.getText().toString())) {
            name.setError("Required");
            result = false;
        } else {
            name.setError(null);
        }
        if (TextUtils.isEmpty(name.getText().toString()) || TextUtils.isEmpty(email.getText().toString())) {
            viewPager.setCurrentItem(0, false);
            if (TextUtils.isEmpty(name.getText().toString())) {
                name.requestFocus();
            }
            if (TextUtils.isEmpty(email.getText().toString())) {
                email.requestFocus();
            }
        } else {
            if (TextUtils.isEmpty(regNum.getText().toString())) {
                regNum.requestFocus();
            }
            if (TextUtils.isEmpty(password.getText().toString())) {
                password.requestFocus();
            }
        }
        return result;
    }

    private void launchSignInScreen() {
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        regNum = findViewById(R.id.regNum);
        password = findViewById(R.id.password_reg);
        name.requestFocus();
        email.clearFocus();
        password.clearFocus();
        regNum.clearFocus();
        if (validateForm()) {
            registerUser();
        }

    }

    private void registerUser() {
        progressDialog.show();
        final String uEmail = email.getText().toString().trim();
        final String uName = name.getText().toString().trim();
        final String Upassword = password.getText().toString().trim();
        final String uniNumber = regNum.getText().toString().trim();
        final String imageStr;
        if (bitmap == null) {
            Toast.makeText(Objects.requireNonNull(this).getApplicationContext(), "Please select Image", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        } else {
            if (!isNetworkConnected()) {
                mDialog2.show();
                progressDialog.dismiss();
            } else {
                imageStr = getStringImage(bitmap);
                StringRequest stringRequest = new StringRequest(Request.Method.POST,
                        Constants.URL_REGISTER,
                        (String response) -> {
                            try {
                                progressDialog.dismiss();
                                JSONObject jsonObject = new JSONObject(response);
                                code = jsonObject.getString("code");
                                switch (code) {
                                    case "0":
                                        Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                                        keyPass = password.getText().toString();
                                        keyReg = regNum.getText().toString();
                                        Intent intent = getIntent();
                                        String value = "OK";
                                        intent.putExtra("key", value);
                                        intent.putExtra("regNum", keyReg);
                                        intent.putExtra("password", keyPass);
                                        setResult(RESULT_OK, intent);
                                        finish();
                                        break;
                                    case "2":
                                        Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                                        email.setError("Email in use by another user.");
                                        regNum.setText("");
                                        regNum.setError("Registration number in use by another user.");
                                        email.setText("");
                                        email.requestFocus();
                                    case "1":
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
                                e.printStackTrace();
                                Log.i("Server Error", "[" + response + "]");
                            }
                        },
                        (VolleyError error) -> {
                            progressDialog.hide();
                            mDialog.show();
                            Log.i("Server Error", "[" + error + "]");

                        }) {
                    @Override
                    protected Map<String, String> getParams() {

                        Map<String, String> params = new HashMap<>();
                        params.put("username", uName);
                        params.put("email", uEmail);
                        params.put("password", Upassword);
                        params.put("uniNum", uniNumber);
                        params.put("image", imageStr);

                        return params;
                    }
                };
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                        MY_SOCKET_TIMEOUT_MS,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
            }

        }
    }

    private boolean isNetworkConnected() {

        final ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
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
        mDialog = new MaterialDialog.Builder(this)
                .setMessage("Some Error Occurred. Please try again Later. ")
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
        alert.show();
    }

    private void showFileChooser() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
        builder.setTitle("Choose your profile picture");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);

                } else if (options[item].equals("Choose from Gallery")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickPhoto.setType("image/*");
                    startActivityForResult(pickPhoto, 1);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.create();
        builder.show();
    }

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
                        filepath = data.getData();
                        try {
                            bitmap = compressImage(filepath);
                            imageView.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                    break;
            }
        }
    }

    private Bitmap compressImage(Uri imageUri) throws IOException {
        Bitmap imageBitmap;
        if (imageUri != null) {

            imageBitmap = SiliCompressor.with(this)
                    .getCompressBitmap(FileUtils.getPath(this, imageUri), false);

            String size = AnstronCoreHelper.getReadableFileSize(imageBitmap.getAllocationByteCount());
            Toast.makeText(this, size, Toast.LENGTH_LONG).show();
            return imageBitmap;

        } else {
            return null;
        }
    }

    private String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends ViewStatePagerAdapter {
        private final Context context;
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter(Context context) {
            this.context = context;
        }

        @Override
        protected View createView(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //View view = LayoutInflater.from(context).inflate(layouts[position], container, false);
            View view = layoutInflater.inflate(layouts[position], container, false);
            if (position == 1) {
                imageView = view.findViewById(R.id.profile_image);
                imageView.setOnClickListener(v -> showFileChooser());
            } else {

            }
            //container.addView(view);
            return view;

        }

        @Override
        public int getCount() {
            return layouts.length;
        }


    }

}
