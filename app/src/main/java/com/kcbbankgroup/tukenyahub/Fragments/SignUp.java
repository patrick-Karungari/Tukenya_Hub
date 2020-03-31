package com.kcbbankgroup.tukenyahub.Fragments;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kcbbankgroup.tukenyahub.Modules.Constants;
import com.kcbbankgroup.tukenyahub.Activities.LoginActivity;
import com.kcbbankgroup.tukenyahub.R;
import com.kcbbankgroup.tukenyahub.Modules.RequestHandler;
import com.kcbbankgroup.tukenyahub.Modules.SharedPrefManager;
import com.shreyaspatil.MaterialDialog.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static android.os.Build.VERSION.SDK_INT;
import static androidx.appcompat.app.AlertDialog.Builder;


/**
 * A simple {@link Fragment} subclass.
 */
public class SignUp extends Fragment {
    private static final int MY_SOCKET_TIMEOUT_MS = 15000;
    private EditText name, password, email, uniNum;
    private boolean isConnected;
    private ProgressDialog progressDialog;
    private Context mContext;
    private MaterialDialog mDialog2;
    private MaterialDialog mDialog;
    private AlertDialog alert;

    public SignUp() {
        // Required empty public constructor
    }

    private int PICK_IMAGE_REQUEST = 1;

    private ImageView imageView;
    private Bitmap bitmap;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        initUi(view);
        return view;
    }

    private void initUi(View view) {
        Thread thread = new Thread(() -> {
            Objects.requireNonNull(getActivity()).runOnUiThread(this::alertBuilder);
            isNetworkConnected();
        });
        thread.start();
        name = view.findViewById(R.id.name);
        password = view.findViewById(R.id.password);
        email = view.findViewById(R.id.email);
        imageView = view.findViewById(R.id.profilePic);
        TextView signIn = view.findViewById(R.id.signIn);
        uniNum = view.findViewById(R.id.uniNum);
        progressDialog = new ProgressDialog(getContext(),
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Registering ...");
        FloatingActionButton fab = view.findViewById(R.id.fablogin);
        fab.setOnClickListener(v -> {

            if (validateForm()) {
                registerUser();
            }
        });
        view.findViewById(R.id.profilePic).setOnClickListener(v -> showFileChooser());

        signIn.setOnClickListener(v -> startActivity(new Intent(getContext(), LoginActivity.class)));

    }

    private void alertBuilder() {
        android.content.DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case android.content.DialogInterface.BUTTON_POSITIVE:
                    SharedPrefManager.getInstance(Objects.requireNonNull(getActivity()).getApplicationContext()).logout();
                    //startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    android.os.Process.killProcess(android.os.Process.myPid());
                    getActivity().finish();
                    break;

                case android.content.DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };
        Builder builder = new Builder(Objects.requireNonNull(getActivity()));
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
        mDialog = new MaterialDialog.Builder(getActivity())
                .setMessage("Some Error Occurred. Please try again Later. ")
                .setTitle("Server Error")
                .setCancelable(true)
                .setAnimation("Timeout.json")
                .setPositiveButton("OK", (dialogInterface, which) -> dialogInterface.dismiss())
                .build();
        mDialog2 = new MaterialDialog.Builder(Objects.requireNonNull(getActivity()))
                .setMessage("Seems you are offline.\nPlease check your internet connection and try again.")
                .setCancelable(true)
                .setAnimation("no_internet_connection.json")
                .setPositiveButton("OK", (dialogInterface, which) -> dialogInterface.dismiss())
                .build();
    }

    private boolean isNetworkConnected() {
        fyl(Objects.requireNonNull(getActivity()).getApplicationContext());
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

    private void fyl(Context mContext) {
        this.mContext = mContext;
    }

    private void registerUser() {
        progressDialog.show();
        final String uEmail = email.getText().toString().trim();
        final String uName = name.getText().toString().trim();
        final String Upassword = password.getText().toString().trim();
        final String uniNumber = uniNum.getText().toString().trim();
        final String imageStr;
        if (bitmap == null) {

            Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Please select Image", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        } else {
            if (!isNetworkConnected()) {
                mDialog2.show();
                progressDialog.dismiss();
            } else {
                imageStr = getStringImage(bitmap);
                StringRequest stringRequest = new StringRequest(Request.Method.POST,
                        Constants.URL_REGISTER,
                        response -> {
                            progressDialog.dismiss();
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String code = jsonObject.getString("code");
                                switch (code) {
                                    case "0":
                                        Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(getActivity().getApplicationContext(), LoginActivity.class));
                                        break;
                                    case "2":
                                        Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                                        email.setError("Email in use by another user.");
                                        uniNum.setText("");
                                        uniNum.setError("Registration number in use by another user.");
                                        email.setText("");
                                        email.requestFocus();
                                    case "1":
                                        MaterialDialog mDialog3 = new MaterialDialog.Builder(Objects.requireNonNull(SignUp.this.getActivity()))
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
                        error -> {
                            progressDialog.hide();

                            mDialog.show();
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
                RequestHandler.getInstance(getContext()).addToRequestQueue(stringRequest);
            }

        }

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
        if (TextUtils.isEmpty(uniNum.getText().toString())) {
            uniNum.setError("Required");
            result = false;
        } else {
            uniNum.setError(null);
        }
        if (TextUtils.isEmpty(name.getText().toString())) {
            name.setError("Required");
            result = false;
        } else {
            name.setError(null);
        }
        return result;
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(getActivity()).getApplicationContext().getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
