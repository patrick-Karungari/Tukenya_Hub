package com.patrickarungari.tukenyahub.Modules;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

public class ScreenRotation {
    private Context mContext;
    private Activity mactivity;

    public ScreenRotation(Context context, Activity activity) {
        mContext = context;
        mactivity = activity;
        lockDeviceRotation();
    }

    private void lockDeviceRotation() {

        int currentOrientation = mContext.getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            mactivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        } else {
            mactivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
    }
}
