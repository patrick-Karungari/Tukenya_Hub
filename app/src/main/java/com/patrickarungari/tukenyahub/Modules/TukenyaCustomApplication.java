package com.patrickarungari.tukenyahub.Modules;

import android.app.Application;
import android.os.Build;
import android.provider.Settings;

import com.patrickarungari.tukenyahub.R;
import com.pushlink.android.PushLink;
import com.pushlink.android.StatusBarStrategy;
import com.pushlink.android.StrategyEnum;

public class TukenyaCustomApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        String yourDeviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        PushLink.start(this, R.raw.logo, "gqdgbnsgekv2vqel", yourDeviceID);
        PushLink.setCurrentStrategy(StrategyEnum.STATUS_BAR);
        StatusBarStrategy sbs = (StatusBarStrategy) PushLink.getCurrentStrategy();
        sbs.setStatusBarTitle("Hello, New version available!");
        sbs.setStatusBarDescription("Tap to discover more.");

        //This information will be shown in two places: "Installations" and "Exceptions" tabs of the web administration
        PushLink.addMetadata("Brand", Build.BRAND);
        PushLink.addMetadata("Model", Build.MODEL);
        PushLink.addMetadata("OS Version", Build.VERSION.RELEASE);
    }

}
