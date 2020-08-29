package com.patrickarungari.tukenyahub.Activities;



import android.util.Log;

import com.patrickarungari.messenger.ui.login.activities.ChatSplashActivity;
import com.patrickarungari.tukenyahub.chatApp.MessengerMain;

import static com.patrickarungari.messenger.utils.DebugConstants.DEBUG_LOGIN;

public class SplashActivity extends ChatSplashActivity {


    @Override
    protected Class<?> getTargetClass() {
        Log.d(DEBUG_LOGIN, "SplashActivity.getTargetClass");
        return MessengerMain.class;
    }
}
