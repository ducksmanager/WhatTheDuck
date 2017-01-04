package net.ducksmanager.util;

import android.app.Activity;

import net.ducksmanager.whattheduck.R;

public class FlagHelper {

    public static int getImageResource(Activity activity, String country) {
        String uri = "@drawable/flags_" + country;
        int imageResource = activity.getResources().getIdentifier(uri, null, activity.getPackageName());

        if (imageResource == 0) {
            imageResource = R.drawable.flags_unknown;
        }
        return imageResource;
    }
}
