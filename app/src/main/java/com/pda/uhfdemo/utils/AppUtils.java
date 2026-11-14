package com.pda.uhfdemo.utils;

import android.app.Activity;
import android.content.Intent;


public class AppUtils {

    public static void reset(Activity activity) {
        Intent launchIntent = activity.getPackageManager()
                .getLaunchIntentForPackage(activity.getPackageName());
        if (launchIntent == null) {
            throw new IllegalStateException("Unable to find the app launch activity");
        }
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(launchIntent);
        activity.finish();
    }
}
