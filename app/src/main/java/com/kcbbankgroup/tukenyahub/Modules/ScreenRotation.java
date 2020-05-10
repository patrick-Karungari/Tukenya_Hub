package com.kcbbankgroup.tukenyahub.Modules;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.view.WindowManager;

public class ScreenRotation {
    private Context mContext;
    private Activity mactivity;

    public ScreenRotation(Context context, Activity activity) {
        mContext = context;
        mactivity = activity;
        lockDeviceRotation(true);
    }

    private void lockDeviceRotation(boolean value) {
        if (value) {

            int currentOrientation = mContext.getResources().getConfiguration().orientation;
            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                mactivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            } else {
                mactivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            }
        } else {
            mactivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mactivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
            } else {
                mactivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
            }
        }
    }
}
