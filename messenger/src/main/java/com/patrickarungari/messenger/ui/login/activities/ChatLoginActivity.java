package com.patrickarungari.messenger.ui.login.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.patrickarungari.messenger.R;
import com.patrickarungari.messenger.core.ChatManager;
import com.patrickarungari.messenger.core.authentication.task.RefreshFirebaseInstanceIdTask;
import com.patrickarungari.messenger.core.exception.ChatFieldNotFoundException;
import com.patrickarungari.messenger.core.users.models.ChatUser;
import com.patrickarungari.messenger.core.users.models.IChatUser;
import com.patrickarungari.messenger.ui.ChatUI;
import com.patrickarungari.messenger.ui.contacts.activites.ContactListActivity;
import com.patrickarungari.messenger.ui.conversations.listeners.OnNewConversationClickListener;
import com.patrickarungari.messenger.ui.login.Fragments.ResetPassword;
import com.patrickarungari.messenger.utils.ChatUtils;
import com.patrickarungari.messenger.utils.DebugConstants;
import com.patrickarungari.messenger.utils.StringUtils;
import com.patrickarungari.sharedPrefManager.SharedPrefManager;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ir.alirezabdn.wp7progress.WP10ProgressBar;

import static com.patrickarungari.messenger.core.ChatManager.Configuration.appId;
import static com.patrickarungari.messenger.ui.ChatUI.BUNDLE_SIGNED_UP_USER_EMAIL;
import static com.patrickarungari.messenger.ui.ChatUI.BUNDLE_SIGNED_UP_USER_PASSWORD;
import static com.patrickarungari.messenger.ui.ChatUI.REQUEST_CODE_SIGNUP_ACTIVITY;

/**
 * Created by Patrick Karungari on 04/03/20.
 */

public class ChatLoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ChatLoginActivity";
    WP10ProgressBar wp10ProgressBar;
    private Toolbar toolbar;
    private EditText vEmail;
    private EditText vPassword;
    private Button vLogin, resend;
    private LinearLayout vSignUp;
    private FirebaseAuth mAuth;
    private AlertDialog dialog;
    private RelativeLayout progBar;
    private NestedScrollView cont;
    private ConstraintLayout contLottie;
//    private String email, username, password;

    private interface OnUserLookUpComplete {
        void onUserRetrievedSuccess(IChatUser loggedUser);

        void onUserRetrievedError(Exception e);
    }

    @VisibleForTesting
    public ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_login);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        dialog = new AlertDialog.Builder(this).setTitle("Sign Up").setMessage("Signing in...").create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        mAuth = FirebaseAuth.getInstance();
        progBar = findViewById(R.id.load);
        wp10ProgressBar = findViewById(R.id.myProgBar);
        wp10ProgressBar.setIndicatorRadius(5);
//        ChatAuthentication.getInstance().setTenant(ChatManager.getInstance().getTenant());
//        ChatAuthentication.getInstance().createAuthListener();

//        Log.d(DEBUG_LOGIN, "ChatLoginActivity.onCreate: auth state listener created ");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        toolbar.setTitle("TUK Messenger: Sign In");
        vLogin = findViewById(R.id.login);
        vLogin.setOnClickListener(this);
        contLottie = findViewById(R.id.constraintLayout);
        cont = findViewById(R.id.nest);
        resend = findViewById(R.id.resend);
        vSignUp = findViewById(R.id.signup);
        vSignUp.setOnClickListener(this);

        vEmail = findViewById(R.id.email);
        vPassword = findViewById(R.id.password);
        initPasswordIMEAction();

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();

    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        if (viewId == R.id.login) {
            signIn(SharedPrefManager.getInstance(ChatLoginActivity.this).getUserEmail(), vPassword.getText().toString());
//            performLogin();
        } else if (viewId == R.id.signup) {
            startSignUpActivity();
        } else if (viewId == R.id.resetPassword) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ResetPassword dialog = new ResetPassword();

            dialog.show(ft, ResetPassword.class.getName());
        }
    }

    private void initPasswordIMEAction() {
        Log.d(DebugConstants.DEBUG_LOGIN, "initPasswordIMEAction");

        /**
         * on ime click
         * source:
         * http://developer.android.com/training/keyboard-input/style.html#Action
         */
        vPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Log.d(DebugConstants.DEBUG_LOGIN, "ChatLoginActivity.initPasswordIMEAction");

//                    performLogin();
                    signIn(SharedPrefManager.getInstance(ChatLoginActivity.this).getUserEmail(), vPassword.getText().toString());

                    handled = true;
                }
                return handled;
            }
        });
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);

        vEmail.setText(email);
        vPassword.setText(password);

        if (!validateForm()) {
            return;
        }

        vEmail.setEnabled(false);
        vPassword.setEnabled(false);
        //showProgressDialog();dialog.show();
        progBar.setVisibility(View.VISIBLE);
        wp10ProgressBar.showProgressBar();
        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");

                                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                                    @Override
                                    public void onSuccess(InstanceIdResult instanceIdResult) {
                                        String id = instanceIdResult.getToken();
                                        DatabaseReference tokenNode = FirebaseDatabase.getInstance()
                                                .getReference()
                                                .child("/apps/" + appId + "/users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/instances/" + id);
                                        Map<String, Object> device = new HashMap<>();
                                        device.put("device_model", ChatUtils.getDeviceModel());
                                        device.put("platform", "Android");
                                        device.put("platform_version", ChatUtils.getSystemVersion());
                                        device.put("language", ChatUtils.getSystemLanguage(getResources()));

                                        Log.i(DebugConstants.DEBUG_LOGIN, "SaveFirebaseInstanceIdService.onTokenRefresh: " +
                                                "saved with token: " + id +
                                                ", appId: " + appId + ", firebaseUsersPath: " + tokenNode);
                                        tokenNode.setValue(device).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                lookUpContactById(user.getUid(), email, password, new OnUserLookUpComplete() {
                                                    @Override
                                                    public void onUserRetrievedSuccess(IChatUser loggedUser) {
                                                        Log.d(TAG, "ChatLoginActivity.signInWithEmail.onUserRetrievedSuccess: loggedUser == " + loggedUser.toString());

                                                        ChatManager.Configuration mChatConfiguration =
                                                                new ChatManager.Configuration.Builder(ChatManager.Configuration.appId)
                                                                        .build();
                                                        ChatManager.start(ChatLoginActivity.this, mChatConfiguration, loggedUser);
                                                        Log.i(TAG, "chat has been initialized with success");

                                                        // get device token
                                                        new RefreshFirebaseInstanceIdTask().execute();

                                                        ChatUI.getInstance().setContext(ChatLoginActivity.this);
                                                        Log.i(TAG, "ChatUI has been initialized with success");

                                                        ChatUI.getInstance().enableGroups(true);

                                                        final IChatUser support = null;
                                                        ChatUI.getInstance().setOnNewConversationClickListener((OnNewConversationClickListener) () -> {
                                                            if (support != null) {
                                                                ChatUI.getInstance().openConversationMessagesActivity(support);
                                                            } else {
                                                                progBar.setVisibility(View.GONE);
                                                                wp10ProgressBar.hideProgressBar();
                                                                Intent intent = new Intent(getApplicationContext(),
                                                                        ContactListActivity.class);
                                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // start activity from context

                                                                startActivity(intent);
                                                            }
                                                        });
                                                        Log.i(TAG, "ChatUI has been initialized with success");
                                                        setResult(Activity.RESULT_OK);
                                                        finish();
                                                    }

                                                    @Override
                                                    public void onUserRetrievedError(Exception e) {
                                                        Log.d(TAG, "ChatLoginActivity.signInWithEmail.onUserRetrievedError: " + e.toString());
                                                    }
                                                });

                                                // enable persistence must be made before any other usage of FirebaseDatabase instance.
                                                try {
                                                    FirebaseDatabase.getInstance().setPersistenceEnabled(true);
                                                } catch (DatabaseException databaseException) {
                                                    Log.w(TAG, databaseException.toString());
                                                } catch (Exception e) {
                                                    Log.w(TAG, e.toString());
                                                }
                                            }
                                        });

                                    }
                                });
                            } else if (user != null && !user.isEmailVerified()) {
                                verifyEmail(SharedPrefManager.getInstance(ChatLoginActivity.this).getUserEmail(), password);
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            vEmail.setEnabled(true);
                            vPassword.setEnabled(true);
                            Toast.makeText(ChatLoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            progBar.setVisibility(View.GONE);
                            wp10ProgressBar.hideProgressBar();
//                            setResult(Activity.RESULT_CANCELED);
//                            finish();
//                            updateUI(null);
                        }

                        // [START_EXCLUDE]

                        //hideProgressDialog();
                        if (progBar != null) {
                            progBar.setVisibility(View.GONE);
                            wp10ProgressBar.hideProgressBar();
                        }

                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }

    private void verifyEmail(String userEmail, String password) {

        //hideProgressDialog();
        if (progBar != null) {
            progBar.setVisibility(View.GONE);
            wp10ProgressBar.hideProgressBar();
        }

        final FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        toolbar.setSubtitle("Verify email");
        TextView textView = findViewById(R.id.verifyText);
        TextView timer = findViewById(R.id.timer);
        textView.setText(MessageFormat.format("An email has been sent to {0}. Please verify your email to login.", userEmail));
        timer.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
        user.sendEmailVerification()
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {
                        cont.setVisibility(View.GONE);
                        contLottie.setVisibility(View.VISIBLE);
                        new CountDownTimer(120000, 1000) {
                            @Override
                            public void onTick(long Long) {
                                if (!user.isEmailVerified()) {
                                    user.reload();
                                    timer.setText(MessageFormat.format("Link expires in {0} seconds", Long / 1000));
                                } else {
                                    onFinish();
                                }
                            }

                            @Override
                            public void onFinish() {
                                if (user.isEmailVerified()) {
//                                    Log.d(DebugConstants.DEBUG_LOGIN, "ChatLoginActivity.lookUpContactById: dataSnapshot == " + dataSnapshot.toString());
                                    timer.setText("Successfully verified");
                                    timer.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.green_500));
                                    cancel();
                                    signIn(userEmail, password);
                                } else {
                                    timer.setText("Failed to verify Link. Try again");
                                    resend.setVisibility(View.VISIBLE);
                                }
                            }
                        }.start();

                    } else {
                        vEmail.setEnabled(true);
                        vPassword.setEnabled(true);
                        cont.setVisibility(View.VISIBLE);
                        contLottie.setVisibility(View.GONE);
                        Log.d(DebugConstants.DEBUG_LOGIN, "ChatLoginActivity.VerifyEmail: exception == " + task.getException().toString());
                        Toast.makeText(ChatLoginActivity.this,
                                "Failed to send verification email. \n Error: " + task.getException(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }


    private void startSignUpActivity() {
        Intent intent = new Intent(this, ChatSignUpActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SIGNUP_ACTIVITY);
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = vEmail.getText().toString();
        if (!StringUtils.isValid(email)) {
            vEmail.setError("Required.");
            valid = false;
        } else if (StringUtils.isValid(email) && !StringUtils.validateEmail(email)) {
            vEmail.setError("Not valid email.");
            valid = false;
        } else {
            vEmail.setError(null);
        }
        String password = vPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            vPassword.setError("Required.");
            valid = false;
        } else {
            vPassword.setError(null);
        }

        return valid;
    }


    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
//            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SIGNUP_ACTIVITY) {
            if (resultCode == RESULT_OK) {

                // set username
                String email = data.getStringExtra(BUNDLE_SIGNED_UP_USER_EMAIL);
//                vEmail.setText(email);

                // set password
                String password = data.getStringExtra(BUNDLE_SIGNED_UP_USER_PASSWORD);
//                vPassword.setText(password);

                signIn(SharedPrefManager.getInstance(ChatLoginActivity.this).getUserEmail(), password);
            }
        }
    }

    private void lookUpContactById(String userId, String email, String password, final OnUserLookUpComplete onUserLookUpComplete) {


        DatabaseReference contactsNode;
        if (StringUtils.isValid(ChatManager.Configuration.firebaseUrl)) {
            contactsNode = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(ChatManager.Configuration.firebaseUrl)
                    .child("/apps/" + ChatManager.Configuration.appId + "/contacts/" + userId);
        } else {
            contactsNode = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("/apps/" + ChatManager.Configuration.appId + "/contacts/" + userId);
        }

        contactsNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    try {
                        IChatUser loggedUser = decodeContactSnapShop(dataSnapshot);
                        Log.d(DebugConstants.DEBUG_LOGIN, "ChatLoginActivity.lookUpContactById.onDataChange: loggedUser == " + loggedUser.toString());
                        onUserLookUpComplete.onUserRetrievedSuccess(loggedUser);
                    } catch (ChatFieldNotFoundException e) {
                        Log.e(DebugConstants.DEBUG_LOGIN, "ChatLoginActivity.lookUpContactById.onDataChange: " + e.toString());
                        onUserLookUpComplete.onUserRetrievedError(e);
                    }
                } else {
                    final Map<String, Object> userMap = new HashMap<>();
                    userMap.put("email", SharedPrefManager.getInstance(getApplicationContext()).getUserEmail());
                    userMap.put("regNum", SharedPrefManager.getInstance(getApplicationContext()).getuniNum());
                    userMap.put("firstname", SharedPrefManager.getInstance(getApplicationContext()).getUserFirstName());
                    userMap.put("imageurl", SharedPrefManager.getInstance(getApplicationContext()).getimageData());
                    userMap.put("lastname", SharedPrefManager.getInstance(getApplicationContext()).getUserLastName());
                    userMap.put("timestamp", new Date().getTime());
                    userMap.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());

                    DatabaseReference contactsNode;
                    if (StringUtils.isValid(ChatManager.Configuration.firebaseUrl)) {
                        contactsNode = FirebaseDatabase.getInstance()
                                .getReferenceFromUrl(ChatManager.Configuration.firebaseUrl)
                                .child("/apps/" + ChatManager.Configuration.appId + "/contacts");
                    } else {
                        contactsNode = FirebaseDatabase.getInstance()
                                .getReference()
                                .child("/apps/" + ChatManager.Configuration.appId + "/contacts");
                    }

                    // save the user on contacts node
                    contactsNode.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(userMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                    if (error == null) {
                                        // all gone right
                                        signIn(email, password);
                                    } else {
                                        // errors
                                        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                                            @Override
                                            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                                                if (firebaseAuth.getCurrentUser() != null) {

                                                    FirebaseAuth.getInstance().signOut();
                                                }
                                            }
                                        });
                                        signIn(email, password);
                                    }
                                }
                            });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(DebugConstants.DEBUG_LOGIN, "ChatLoginActivity.lookUpContactById: " + databaseError.toString());
                onUserLookUpComplete.onUserRetrievedError(databaseError.toException());
            }
        });
    }

    private static IChatUser decodeContactSnapShop(DataSnapshot dataSnapshot) throws ChatFieldNotFoundException {
        Log.v(TAG, "decodeContactSnapShop called");

        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

//        String contactId = dataSnapshot.getKey();

        String uid = (String) map.get("uid");
        if (uid == null) {
            throw new ChatFieldNotFoundException("Required uid field is null for contact id : " + uid);
        }

//        String timestamp = (String) map.get("timestamp");

        String lastname = (String) map.get("lastname");
        String firstname = (String) map.get("firstname");
        String imageurl = (String) map.get("imageurl");
        String email = (String) map.get("email");
        String regNum = (String) map.get("regNum");

        IChatUser contact = new ChatUser();
        contact.setId(uid);
        contact.setEmail(email);
        contact.setFullName(firstname + " " + lastname);
        contact.setProfilePictureUrl(imageurl);
        contact.setRegNum(regNum);

        Log.v(TAG, "decodeContactSnapShop.contact : " + contact);

        return contact;
    }
}