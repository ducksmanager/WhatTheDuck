package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.app.Application;
import android.content.res.AssetManager;

import org.piwik.sdk.Piwik;
import org.piwik.sdk.Tracker;
import org.piwik.sdk.TrackerConfig;
import org.piwik.sdk.extra.TrackHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class WhatTheDuckApplication extends Application {
    public static Properties config = null;

    private static final String CONFIG = "config.properties";
    public static final String CONFIG_KEY_API_ENDPOINT_URL = "api_endpoint_url";
    private static final String CONFIG_KEY_PIWIK_URL = "piwik_url";
    static final String CONFIG_KEY_SECURITY_PASSWORD = "security_password";

    private Tracker mPiwikTracker;

    public void onCreate() {
        super.onCreate();
        loadConfig(getAssets());
    }

    private static void loadConfig(AssetManager assets) {
        InputStream reader = null;
        try {
            reader = assets.open(CONFIG);
            config = new Properties();
            config.load(reader);
        } catch (IOException e) {
            WhatTheDuck.wtd.alert(WhatTheDuck.wtd, R.string.internal_error);
            System.err.println("Config file not found, aborting");
            System.exit(-1);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    WhatTheDuck.wtd.alert(WhatTheDuck.wtd, R.string.internal_error);
                    System.err.println("Error while reading config file, aborting");
                    System.exit(-1);
                }
            }
        }
    }

    private synchronized Tracker getTracker() {
        if (mPiwikTracker != null) {
            return mPiwikTracker;
        }
        String piwikUrl = config.getProperty(CONFIG_KEY_PIWIK_URL);
        mPiwikTracker = Piwik.getInstance(this).newTracker(new TrackerConfig(piwikUrl, 2, "WhatTheDuck"));
        return mPiwikTracker;
    }

    public void trackActivity(Activity activity) {
        TrackHelper.track()
            .screen(activity.getClass().getSimpleName())
            .title(activity.getTitle().toString())
            .with(getTracker());
    }

    public void trackEvent(String name) {
        TrackHelper.track()
            .event("category", "action")
            .name(name)
            .value(1.0f)
            .with(getTracker());
    }
}
