package com.patrickarungari.tukenyahub.Modules;

import android.app.Activity;
import android.content.Context;

import com.patrickarungari.tukenyahub.R;

public class animation {
    public static void slideDown(Context context) {
        ((Activity) context).overridePendingTransition(R.anim.slide_enter_down, R.anim.slide_exit_down);
    }

    public static void slideUp(Context context) {
        ((Activity) context).overridePendingTransition(R.anim.slide_enter_up, R.anim.slide_exit_up);
    }
}
