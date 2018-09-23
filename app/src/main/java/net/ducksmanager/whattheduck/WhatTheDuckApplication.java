package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;

import net.ducksmanager.util.Settings;

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
    public static final String CONFIG_KEY_DM_URL = "dm_url";
    public static final String CONFIG_KEY_SECURITY_PASSWORD = "security_password";
    public static final String CONFIG_KEY_EDGES_URL = "edges_url";

    private static final String CONFIG_KEY_PIWIK_URL = "piwik_url";
    private static final Integer CONFIG_PIWIK_DIMENSION_USER = 1;
    private static final Integer CONFIG_PIWIK_DIMENSION_VERSION = 2;

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
            WhatTheDuck.wtd.alert(R.string.internal_error);
            System.err.println("Config file not found, aborting");
            System.exit(-1);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    WhatTheDuck.wtd.alert(R.string.internal_error);
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
        TrackHelper t = TrackHelper.track();

        String username = Settings.getUsername();
        if (username != null) {
            t.dimension(CONFIG_PIWIK_DIMENSION_USER, username);
            try {
                t.dimension(CONFIG_PIWIK_DIMENSION_VERSION, activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName);
            } catch (PackageManager.NameNotFoundException e) {
                t.dimension(CONFIG_PIWIK_DIMENSION_VERSION, "Unknown");
            }
        }

        t
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
